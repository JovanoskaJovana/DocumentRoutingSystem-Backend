package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentAlreadyDecidedException;
import mk.ukim.finki.routingsystem.repository.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Reproduces the lost-update race between {@link DocumentService#approveDocument} and
 * {@link DocumentService#rejectDocument} when two signatories act on the same ROUTED document at once.
 */
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2Nzg=",
        "jwt.expiration=3600000"
})
class DocumentApproveRejectRaceIT {

  private static final int ITERATIONS = 20;

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
  private DocumentActionRepository documentActionRepository;
  @Autowired
  private TransactionTemplate transactionTemplate;

  private Company company;
  private Department department;
  private Employee signatoryA;
  private Employee signatoryB;

  @BeforeEach
  void setUp() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);

    company = companyRepository.save(new Company(null, "Race Co " + suffix, "RACE-" + suffix, true));

    department = new Department("Finance", "FN", company);
    department = departmentRepository.save(department);

    signatoryA = employeeRepository.save(signatory("a-" + suffix + "@race.test", "Alice"));
    signatoryB = employeeRepository.save(signatory("b-" + suffix + "@race.test", "Bob"));
  }

  @Test
  void concurrentApproveAndRejectShouldLetExactlyOneWin() throws Exception {
    SoftAssertions softly = new SoftAssertions();
    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      for (int i = 1; i <= ITERATIONS; i++) {
        Long documentId = createRoutedDocument();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<Outcome> approve = executor.submit(() -> race(ready, start,
                () -> documentService.approveDocument(documentId, signatoryA.getId(), company.getId())));
        Future<Outcome> reject = executor.submit(() -> race(ready, start,
                () -> documentService.rejectDocument(documentId, signatoryB.getId(), company.getId())));

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();

        Outcome approveOutcome = approve.get(30, TimeUnit.SECONDS);
        Outcome rejectOutcome = reject.get(30, TimeUnit.SECONDS);

        int successes = (approveOutcome.succeeded() ? 1 : 0) + (rejectOutcome.succeeded() ? 1 : 0);
        int conflicts = (approveOutcome.conflict() ? 1 : 0) + (rejectOutcome.conflict() ? 1 : 0);

        // The action listener is @Async and AFTER_COMMIT: wait for every successful call to be recorded,
        // then hold briefly so a late extra record is not missed.
        await().pollDelay(Duration.ofMillis(500)).atMost(Duration.ofSeconds(10))
                .until(() -> decisionActionCount(documentId) >= successes);
        long actionCount = decisionActionCount(documentId);

        DocumentStatus finalStatus = documentRepository.findById(documentId).orElseThrow().getDocumentStatus();
        DocumentStatus expectedStatus = approveOutcome.succeeded() && !rejectOutcome.succeeded() ? DocumentStatus.APPROVED
                : rejectOutcome.succeeded() && !approveOutcome.succeeded() ? DocumentStatus.REJECTED
                : null;

        String tag = "iteration " + i + " (approve=" + approveOutcome + ", reject=" + rejectOutcome + ")";
        System.out.printf("[race] %s -> successes=%d, conflicts=%d, finalStatus=%s, approvedOrRejectedActions=%d%n",
                tag, successes, conflicts, finalStatus, actionCount);

        softly.assertThat(successes).as("%s: number of successful calls", tag).isEqualTo(1);
        softly.assertThat(conflicts).as("%s: number of calls rejected as a conflict", tag).isEqualTo(1);
        softly.assertThat(finalStatus).as("%s: final status must match the single winner", tag).isEqualTo(expectedStatus);
        softly.assertThat(actionCount).as("%s: APPROVED/REJECTED action records", tag).isEqualTo(1L);
      }
    } finally {
      executor.shutdownNow();
    }

    softly.assertAll();
  }

  private Outcome race(CountDownLatch ready, CountDownLatch start, Runnable call) throws InterruptedException {
    ready.countDown();
    start.await();
    try {
      call.run();
      return new Outcome(true, null);
    } catch (Exception e) {
      return new Outcome(null, e);
    }
  }

  private Long createRoutedDocument() {
    return transactionTemplate.execute(status -> {
      Document document = new Document();
      document.setTitle("Race document " + UUID.randomUUID());
      document.setCompany(company);
      document.setUploadedByEmployee(signatoryA);
      document.setUploadDateTime(LocalDateTime.now());
      document.setRoutedToDepartment(department);
      document.setRoutedToEmployees(new HashSet<>(Set.of(signatoryA, signatoryB)));
      document.setDocumentStatus(DocumentStatus.ROUTED);
      documentRepository.save(document);

      DocumentVersion version = new DocumentVersion(document, 1, "race.pdf", new byte[]{1, 2, 3},
              "No made changes.", signatoryA, LocalDateTime.now());
      documentVersionRepository.save(version);

      document.setCurrentDocumentVersion(version);
      documentRepository.save(document);
      return document.getId();
    });
  }

  private long decisionActionCount(Long documentId) {
    return documentActionRepository.findByDocument_IdAndDocument_Company_IdOrderByActionDateTime(documentId, company.getId())
            .stream()
            .filter(a -> List.of(ActionType.APPROVED, ActionType.REJECTED).contains(a.getPerformedAction()))
            .count();
  }

  private Employee signatory(String email, String firstName) {
    Employee employee = new Employee(email, "{noop}password", firstName, "Signatory", department, Role.EMPLOYEE, EmployeeType.SIGNATORY);
    employee.setCompany(company);
    return employee;
  }

  private record Outcome(Boolean result, Exception error) {

    boolean succeeded() {
      return error == null && Boolean.TRUE.equals(result);
    }

    // The loser either collides on the write (optimistic lock) or arrives after the winner committed
    // and sees the decided status. Both end as 409 for the user, so both count.
    boolean conflict() {
      return error instanceof OptimisticLockingFailureException
              || error instanceof DocumentAlreadyDecidedException;
    }

    @Override
    public String toString() {
      return error != null ? error.getClass().getSimpleName() : String.valueOf(result);
    }
  }
}
