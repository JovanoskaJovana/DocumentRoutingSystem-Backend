package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import mk.ukim.finki.routingsystem.repository.*;
import mk.ukim.finki.routingsystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks how unexpected errors and unreadable PDFs are reported.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2Nzg=",
        "jwt.expiration=3600000"
})
class ErrorHandlingIT {

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

  private Company company;
  private Department department;
  private Employee signatory;
  private String token;

  @BeforeEach
  void setUp() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);

    company = companyRepository.save(new Company(null, "Error Co " + suffix, "ERR-" + suffix, true));
    department = departmentRepository.save(new Department("Finance", "FN", company));

    Employee employee = new Employee("s-" + suffix + "@error.test", "{noop}password", "Sam", "Signatory",
            department, Role.EMPLOYEE, EmployeeType.SIGNATORY);
    employee.setCompany(company);
    signatory = employeeRepository.save(employee);

    token = "Bearer " + jwtUtil.generateToken(signatory.getId(), "Sam", Role.EMPLOYEE, EmployeeType.SIGNATORY,
            department.getId(), company.getId());
  }

  @Test
  void unexpectedErrorIsLoggedWithMethodPathAndStackTrace(CapturedOutput output) throws Exception {
    mockMvc.perform(get("/test/boom").header("Authorization", token))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

    assertThat(output.getOut())
            .contains("ERROR")
            .contains("Unexpected error on GET /test/boom")
            .contains("java.lang.IllegalStateException: boom")
            .contains("at mk.ukim.finki.routingsystem.web.ErrorHandlingIT$BoomController.boom");
  }

  @Test
  void routingADamagedPdfReturns422AndKeepsTheDocumentUploaded() throws Exception {
    Long documentId = createDocument(DocumentStatus.UPLOADED, "this is not a pdf".getBytes());

    mockMvc.perform(put("/api/documents/{id}/route", documentId).header("Authorization", token))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("The PDF file is damaged."));

    assertThat(documentRepository.findById(documentId).orElseThrow().getDocumentStatus())
            .isEqualTo(DocumentStatus.UPLOADED);
  }

  private Long createDocument(DocumentStatus status, byte[] fileData) {
    return transactionTemplate.execute(tx -> {
      Document document = new Document();
      document.setTitle("Error document " + UUID.randomUUID());
      document.setCompany(company);
      document.setUploadedByEmployee(signatory);
      document.setUploadDateTime(LocalDateTime.now());
      document.setRoutedToDepartment(department);
      document.setRoutedToEmployees(new HashSet<>(Set.of(signatory)));
      document.setDocumentStatus(status);
      documentRepository.save(document);

      DocumentVersion version = new DocumentVersion(document, 1, "file.pdf", fileData,
              "No made changes.", signatory, LocalDateTime.now());
      documentVersionRepository.save(version);

      document.setCurrentDocumentVersion(version);
      documentRepository.save(document);
      return document.getId();
    });
  }

  @TestConfiguration
  static class BoomConfig {

    @Bean
    BoomController boomController() {
      return new BoomController();
    }
  }

  @RestController
  static class BoomController {

    @GetMapping("/test/boom")
    String boom() {
      throw new IllegalStateException("boom");
    }
  }
}
