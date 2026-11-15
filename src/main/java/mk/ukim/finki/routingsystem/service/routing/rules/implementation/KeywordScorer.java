package mk.ukim.finki.routingsystem.service.routing.rules.implementation;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.routingsystem.domain.rules.DepartmentRules;
import mk.ukim.finki.routingsystem.domain.rules.RoutingRules;
import mk.ukim.finki.routingsystem.domain.rules.TenantRules;
import mk.ukim.finki.routingsystem.model.dto.Routing.CompiledRules;
import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;
import mk.ukim.finki.routingsystem.model.exceptions.RoutingRulesForTenantNotFoundException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scores documents against department routing rules using weighted keyword matching.
 * On initialization, routing rules are precompiled into regex patterns and cached per tenant
 * for efficient reuse. Each keyword is classified as strong, weak, or negative and
 * matches in the title are weighted more heavily than matches in the body.
 */

@Slf4j
public class KeywordScorer {

  private static final double TITLE_WEIGHT = 3.0;
  private static final double BODY_WEIGHT = 1.0;
  private static final double STRONG_WEIGHT = 1.0;
  private static final double WEAK_WEIGHT = 0.4;
  private static final double NEGATIVE_WEIGHT = -2.0;
  private static final int MAX_FREQ = 5;

  private final Map<String, Map<String, CompiledRules>> cachedRules;

  public KeywordScorer(RoutingRules rules) {
    this.cachedRules = buildCache(rules);
  }

  /**
   * Computes a keyword-based score for each department for a given document and tenant.
   * Each keyword match contributes to the score based on its type and location.
   *
   * @param tenantCode the code identifying the tenant whose routing rules are used
   * @param document   the extracted title and body of the document to score
   * @return a map of department keys to their computed scores
   * @throws RoutingRulesForTenantNotFoundException if no routing rules are found for the given tenant
   */
  public Map<String, Double> score(String tenantCode, TitleAndBody document) {

    Map<String, CompiledRules> deptRules = cachedRules.get(tenantCode);

    if (deptRules == null) {
      throw new RoutingRulesForTenantNotFoundException("No routing rules found for tenant: " + tenantCode);
    }

    String title = normalize(document.title());
    String body = normalize(document.body());

    Map<String, Double> scores = new LinkedHashMap<>();

    for (Map.Entry<String, CompiledRules> entry : deptRules.entrySet()) {

      String deptName = entry.getKey();
      CompiledRules deptRule = entry.getValue();

      double score = 0.0;

      for (Pattern pattern : deptRule.strong()) {

        int titleCount = countMatches(pattern, title);
        int bodyCount = countMatches(pattern, body);

        score += titleCount * TITLE_WEIGHT * STRONG_WEIGHT;
        score += bodyCount * BODY_WEIGHT * STRONG_WEIGHT;
      }

      for (Pattern pattern : deptRule.weak()) {

        int titleCount = countMatches(pattern, title);
        int bodyCount = countMatches(pattern, body);

        score += titleCount * TITLE_WEIGHT * WEAK_WEIGHT;
        score += bodyCount * BODY_WEIGHT * WEAK_WEIGHT;
      }

      for (Pattern pattern : deptRule.negative()) {

        int titleCount = countMatches(pattern, title);
        int bodyCount = countMatches(pattern, body);

        score += titleCount * TITLE_WEIGHT * NEGATIVE_WEIGHT;
        score += bodyCount * BODY_WEIGHT * NEGATIVE_WEIGHT;
      }

      scores.put(deptName, score);
    }
    return scores;
  }

  /**
   * Precompiles routing rules for all tenants and departments into regex patterns
   * and stores them in a cache for efficient lookup during scoring.
   *
   * @param rules the full set of routing rules
   * @return a nested map of tenant code to department key to compiled rules
   * @throws IllegalStateException if a tenant has no departments
   */
  private static Map<String, Map<String, CompiledRules>> buildCache(RoutingRules rules) {

    Map<String, Map<String, CompiledRules>> cache = new HashMap<>();

    Map<String, TenantRules> tenants = rules.getTenants();

    if (tenants == null || tenants.isEmpty()) {
      return cache;
    }

    for (Map.Entry<String, TenantRules> tenantEntry : tenants.entrySet()) {

      String tenantId = tenantEntry.getKey();
      TenantRules tenantRules = tenantEntry.getValue();

      Map<String, CompiledRules> perDepartment = new HashMap<>();

      Map<String, DepartmentRules> departments = tenantRules.getDepartments();

      // without departments every routing of this company would fail, so refuse to start
      if (departments == null || departments.isEmpty()) {
        throw new IllegalStateException("Routing rules for company " + tenantId
                + " have no departments. Add at least one department under app.routing.tenants." + tenantId + ".departments.");
      }

      for (Map.Entry<String, DepartmentRules> departmentEntry : departments.entrySet()) {

        String departmentId = departmentEntry.getKey();
        DepartmentRules departmentRules = departmentEntry.getValue();

        CompiledRules compiledRules = new CompiledRules(
                compileKeywords(departmentRules.getStrong()),
                compileKeywords(departmentRules.getWeak()),
                compileKeywords(departmentRules.getNegative())
        );

        // such a department always scores 0: allowed, but most likely a configuration mistake
        if (compiledRules.strong().isEmpty() && compiledRules.weak().isEmpty() && compiledRules.negative().isEmpty()) {
          log.warn("Routing rules for company {}: department {} has no keywords, so documents are never routed to it.",
                  tenantId, departmentId);
        }

        perDepartment.put(departmentId, compiledRules);

      }
      cache.put(tenantId, perDepartment);
    }
    return cache;
  }

  /**
   * Compiles a list of keyword strings into regex {@link Pattern} objects.
   *
   * @param keywords the list of keywords to compile
   * @return a list of compiled patterns, or an empty list if the input is null or empty
   */
  private static List<Pattern> compileKeywords(List<String> keywords) {

    if (keywords == null || keywords.isEmpty()) {
      return List.of();
    }

    return keywords.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .map(KeywordScorer::toPattern)
            .toList();
  }

  /**
   * Converts a keyword string into a word-boundary regex pattern.
   * Phrase keywords are joined with a flexible separator matching spaces, hyphens or slashes.
   *
   * @param keyword the keyword to convert
   * @return a case-insensitive {@link Pattern} matching the keyword
   */
  private static Pattern toPattern(String keyword) {

    boolean isPhrase = keyword.contains(" ");
    String regex;

    if (isPhrase) {
      String[] parts = keyword.split("\\s+");
      String separator = "(?:\\s|[-/])+";
      String joined = String.join(separator, Arrays.stream(parts).map(Pattern::quote).toList());
      regex = "\\b" + joined + "\\b";
    } else {
      regex = "\\b" + Pattern.quote(keyword) + "\\b";
    }

    return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  /**
   * Normalizes text by converting to lowercase and replacing non-word characters with spaces.
   *
   * @param text the text to normalize
   * @return the normalized text or an empty string if the input is null
   */
  private static String normalize(String text) {
    if (text == null) {
      return "";
    }

    return text.toLowerCase().replaceAll("[^\\w\\s]", " ");
  }

  /**
   * Counts the number of times a pattern matches in the given text, capped at {@value MAX_FREQ}.
   *
   * @param pattern the pattern to match
   * @param text    the text to search in
   * @return the number of matches, capped at {@value MAX_FREQ}
   */
  private static int countMatches(Pattern pattern, String text) {
    int count = 0;
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      count++;
    }
    return Math.min(count, MAX_FREQ);
  }

}
