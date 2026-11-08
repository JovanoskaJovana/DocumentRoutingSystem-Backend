package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import mk.ukim.finki.routingsystem.model.exceptions.ErrorResponse;
import mk.ukim.finki.routingsystem.model.exceptions.GlobalExceptionHandler;
import mk.ukim.finki.routingsystem.repository.*;
import mk.ukim.finki.routingsystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that document titles are unique per company, not across all companies.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2Nzg=",
        "jwt.expiration=3600000"
})
class DocumentTitleIT {

  private static final String DUPLICATE_TITLE_MESSAGE = "A document with this title already exists.";

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private JwtUtil jwtUtil;
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
  @Autowired
  private GlobalExceptionHandler exceptionHandler;

  private Tenant companyA;
  private Tenant companyB;
  private String title;

  @BeforeEach
  void setUp() {
    companyA = tenant("A");
    companyB = tenant("B");
    title = "Annual report " + UUID.randomUUID();
  }

  @Test
  void anotherCompanyCanUseTheSameTitle() throws Exception {
    createDocument(companyA, title);

    upload(companyB, title)
            .andExpect(status().isCreated());
  }

  @Test
  void sameCompanyCannotUploadTheSameTitleTwice() throws Exception {
    upload(companyA, title)
            .andExpect(status().isCreated());

    upload(companyA, title)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(DUPLICATE_TITLE_MESSAGE));
  }

  @Test
  void editingToATitleThatExistsInTheSameCompanyIsAConflict() throws Exception {
    createDocument(companyA, title);
    Long otherDocumentId = createDocument(companyA, "Other report " + UUID.randomUUID());

    mockMvc.perform(multipart(HttpMethod.PUT, "/api/documents/{id}/editDocument", otherDocumentId)
                    .param("title", title)
                    .param("changeNote", "Renamed")
                    .header("Authorization", companyA.token()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(DUPLICATE_TITLE_MESSAGE));
  }

  @Test
  void titleConstraintViolationFromTheDatabaseIsAConflict() {
    // two requests that both passed the duplicate check: the database constraint is the last line of defence
    createDocument(companyA, title);
    DataIntegrityViolationException violation = catchThrowableOfType(DataIntegrityViolationException.class,
            () -> createDocument(companyA, title));

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(violation, new MockHttpServletRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().getMessage()).isEqualTo(DUPLICATE_TITLE_MESSAGE);
  }

  @Test
  void otherConstraintViolationsStayInternalServerErrors() {
    // company codes are unique too, but that is not the title constraint
    DataIntegrityViolationException violation = catchThrowableOfType(DataIntegrityViolationException.class,
            () -> companyRepository.save(new Company(null, "Copy", companyA.company().getCode(), true)));

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(violation, new MockHttpServletRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResultActions upload(Tenant tenant, String documentTitle) throws Exception {
    MockMultipartFile data = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE,
            ("{\"title\":\"" + documentTitle + "\"}").getBytes());
    MockMultipartFile file = new MockMultipartFile("file", "report.pdf", MediaType.APPLICATION_PDF_VALUE,
            new byte[]{1, 2, 3});

    return mockMvc.perform(multipart("/api/documents").file(data).file(file)
            .header("Authorization", tenant.token()));
  }

  private Long createDocument(Tenant tenant, String documentTitle) {
    return transactionTemplate.execute(tx -> {
      Document document = new Document();
      document.setTitle(documentTitle);
      document.setCompany(tenant.company());
      document.setUploadedByEmployee(tenant.employee());
      document.setUploadDateTime(LocalDateTime.now());
      document.setDocumentStatus(DocumentStatus.UPLOADED);
      documentRepository.save(document);

      DocumentVersion version = new DocumentVersion(document, 1, documentTitle, new byte[]{1, 2, 3},
              "No made changes.", tenant.employee(), LocalDateTime.now());
      documentVersionRepository.save(version);

      document.setCurrentDocumentVersion(version);
      documentRepository.save(document);
      return document.getId();
    });
  }

  private Tenant tenant(String name) {
    String suffix = name + "-" + UUID.randomUUID().toString().substring(0, 8);

    Company company = companyRepository.save(new Company(null, "Company " + suffix, "C-" + suffix, true));
    Department department = departmentRepository.save(new Department("Finance", "FN", company));

    Employee employee = new Employee("e-" + suffix + "@title.test", "{noop}password", "Erin", "Employee",
            department, Role.EMPLOYEE, EmployeeType.REGULAR);
    employee.setCompany(company);
    employee = employeeRepository.save(employee);

    String token = "Bearer " + jwtUtil.generateToken(employee.getId(), "Erin", Role.EMPLOYEE, EmployeeType.REGULAR,
            department.getId(), company.getId());
    return new Tenant(company, employee, token);
  }

  private record Tenant(Company company, Employee employee, String token) {
  }
}
