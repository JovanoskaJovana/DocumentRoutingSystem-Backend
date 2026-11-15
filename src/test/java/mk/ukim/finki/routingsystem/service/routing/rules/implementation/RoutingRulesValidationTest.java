package mk.ukim.finki.routingsystem.service.routing.rules.implementation;

import mk.ukim.finki.routingsystem.domain.rules.DepartmentRules;
import mk.ukim.finki.routingsystem.domain.rules.RoutingRules;
import mk.ukim.finki.routingsystem.domain.rules.TenantRules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks that a routing configuration without departments is rejected, instead of failing every routing later.
 */
class RoutingRulesValidationTest {

  @ParameterizedTest
  @NullAndEmptySource
  void companyWithoutDepartmentsFailsAtStartup(Map<String, DepartmentRules> departments) {
    Map<String, TenantRules> tenants = new LinkedHashMap<>();
    tenants.put("JFS", tenant(Map.of("FN", department(List.of("invoice number")))));
    tenants.put("ZPP", tenant(departments));

    RoutingRules rules = new RoutingRules();
    rules.setTenants(tenants);

    assertThatThrownBy(() -> new KeywordScorer(rules))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ZPP");
  }

  @Test
  @ExtendWith(OutputCaptureExtension.class)
  void departmentWithoutKeywordsOnlyLogsAWarning(CapturedOutput output) {
    RoutingRules rules = new RoutingRules();
    rules.setTenants(Map.of("JFS", tenant(Map.of(
            "FN", department(List.of("invoice number")),
            "HR", department(List.of(" ", ""))))));

    assertThatCode(() -> new KeywordScorer(rules)).doesNotThrowAnyException();
    assertThat(output.getOut())
            .contains("WARN")
            .contains("company JFS: department HR has no keywords");
  }

  @Test
  void decidingWithoutAnyScoresIsAClearError() {
    assertThatThrownBy(() -> new RoutingDecisionMaker().decide(Map.of()))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no department scores");
  }

  private static TenantRules tenant(Map<String, DepartmentRules> departments) {
    TenantRules tenant = new TenantRules();
    tenant.setAdminDeptId("ADMIN");
    tenant.setDepartments(departments);
    return tenant;
  }

  private static DepartmentRules department(List<String> strong) {
    DepartmentRules department = new DepartmentRules();
    department.setStrong(strong);
    return department;
  }
}
