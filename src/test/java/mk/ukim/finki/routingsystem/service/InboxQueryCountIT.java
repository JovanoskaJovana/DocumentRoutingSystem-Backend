package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayDocumentDto;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import mk.ukim.finki.routingsystem.repository.*;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.tuple;

/**
 * Checks that loading the inbox costs a fixed number of SQL statements, no matter how many
 * documents or different uploaders are on the page, and that it does not load the PDF bytes.
 */
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "mk.ukim.finki.routingsystem.service.InboxQueryCountIT$SqlRecorder",
        "jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2Nzg=",
        "jwt.expiration=3600000"
})
class InboxQueryCountIT {

  private static final int DOCUMENTS = 10;
  private static final int MAX_STATEMENTS = 3;

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired
  private DocumentService documentService;
  @Autowired
  private CompanyRepository companyRepository;
  @Autowired
  private DepartmentRepository departmentRepository;
  @Autowired
  private EmployeeRepository employeeRepository;
  @Autowired
  private DocumentRepository documentRepository;
  @Autowired
  private DocumentVersionRepository documentVersionRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  private Company company;
  private Employee signatory;
  private final List<Tuple> expectedInbox = new ArrayList<>();

  @BeforeEach
  void setUp() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);

    company = companyRepository.save(new Company(null, "Inbox Co " + suffix, "INB-" + suffix, true));
    Department department = departmentRepository.save(new Department("Finance", "FN", company));

    signatory = employeeRepository.save(employee("sig-" + suffix, "Sam", "Signatory", department, EmployeeType.SIGNATORY));

    LocalDateTime firstUpload = LocalDateTime.now().minusDays(1);
    for (int i = 1; i <= DOCUMENTS; i++) {
      // a different uploader for every document, like a real inbox
      Employee uploader = employeeRepository.save(
              employee("up" + i + "-" + suffix, "Uploader" + i, "Test", department, EmployeeType.REGULAR));
      String title = "Inbox document " + i + " " + suffix;

      createRoutedDocument(title, uploader, department, firstUpload.plusMinutes(i));
      expectedInbox.add(tuple(title, "Uploader" + i + " Test", List.of("Sam Signatory")));
    }
  }

  @Test
  void inboxPageLoadsWithFewStatementsAndWithoutPdfBytes() {
    SqlRecorder.STATEMENTS.clear();

    List<DisplayDocumentDto> inbox = documentService.findAllByRoutedToEmployee(
            List.of(DocumentStatus.ROUTED), signatory.getId(), company.getId(), PageRequest.of(0, 20)).getContent();

    List<String> statements = List.copyOf(SqlRecorder.STATEMENTS);
    System.out.printf("[inbox] %d documents loaded with %d SQL statements%n", inbox.size(), statements.size());
    for (int i = 0; i < statements.size(); i++) {
      System.out.printf("[inbox]   %d. %s%n", i + 1, statements.get(i));
    }

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(statements.size()).as("SQL statements for one inbox page of %d documents", DOCUMENTS)
            .isLessThanOrEqualTo(MAX_STATEMENTS);
    softly.assertThat(statements.stream().filter(sql -> sql.contains("file_data")).count())
            .as("SQL statements that select file_data (the PDF bytes)")
            .isZero();
    softly.assertThat(inbox)
            .as("inbox DTOs (title, uploader, signatories)")
            .extracting(DisplayDocumentDto::title, DisplayDocumentDto::uploadedByEmployee, DisplayDocumentDto::routedToEmployees)
            .containsExactlyElementsOf(expectedInbox);
    softly.assertAll();
  }

  private void createRoutedDocument(String title, Employee uploader, Department department, LocalDateTime uploadedAt) {
    transactionTemplate.executeWithoutResult(tx -> {
      Document document = new Document();
      document.setTitle(title);
      document.setCompany(company);
      document.setUploadedByEmployee(uploader);
      document.setUploadDateTime(uploadedAt);
      document.setRoutedToDepartment(department);
      document.setRoutedToEmployees(new HashSet<>(Set.of(signatory)));
      document.setDocumentStatus(DocumentStatus.ROUTED);
      documentRepository.save(document);

      DocumentVersion version = new DocumentVersion(document, 1, "file.pdf", new byte[]{1, 2, 3},
              "No made changes.", uploader, LocalDateTime.now());
      documentVersionRepository.save(version);

      document.setCurrentDocumentVersion(version);
      documentRepository.save(document);
    });
  }

  private Employee employee(String login, String firstName, String lastName, Department department, EmployeeType type) {
    Employee employee = new Employee(login + "@inbox.test", "{noop}password", firstName, lastName, department, Role.EMPLOYEE, type);
    employee.setCompany(company);
    return employee;
  }

  /**
   * Hibernate calls this for every SQL statement it is about to send to the database.
   */
  public static class SqlRecorder implements StatementInspector {

    static final List<String> STATEMENTS = new CopyOnWriteArrayList<>();

    @Override
    public String inspect(String sql) {
      STATEMENTS.add(sql);
      return sql;
    }
  }
}
