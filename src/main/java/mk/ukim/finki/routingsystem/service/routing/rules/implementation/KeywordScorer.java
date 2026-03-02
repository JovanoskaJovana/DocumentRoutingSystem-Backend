package mk.ukim.finki.routingsystem.service.routing.rules.implementation;

import mk.ukim.finki.routingsystem.domain.rules.DepartmentRules;
import mk.ukim.finki.routingsystem.domain.rules.RoutingRules;
import mk.ukim.finki.routingsystem.domain.rules.TenantRules;
import mk.ukim.finki.routingsystem.model.dto.Routing.CompiledRules;
import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;
import mk.ukim.finki.routingsystem.model.exceptions.RoutingRulesForTenantNotFound;


import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordScorer {

    private static final double TITLE_WEIGHT    = 3.0;
    private static final double BODY_WEIGHT     = 1.0;
    private static final double STRONG_WEIGHT   = 1.0;
    private static final double WEAK_WEIGHT     = 0.4;
    private static final double NEGATIVE_WEIGHT = -2.0;
    private static final int    MAX_FREQ        = 5;

    private final Map<String, Map<String, CompiledRules>> cachedRules;

    public KeywordScorer(RoutingRules rules) {
        this.cachedRules = buildCache(rules);
    }

    public Map<String, Double> score (String tenantCode, TitleAndBody document) {

        Map<String, CompiledRules> deptRules = cachedRules.get(tenantCode);

        if (deptRules == null) {
            throw new RoutingRulesForTenantNotFound("No routing rules found for tenant: " + tenantCode);
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
                int bodyCount  = countMatches(pattern, body);

                score += titleCount * TITLE_WEIGHT * STRONG_WEIGHT;
                score += bodyCount  * BODY_WEIGHT  * STRONG_WEIGHT;
            }

            for (Pattern pattern : deptRule.weak()) {

                int titleCount = countMatches(pattern, title);
                int bodyCount  = countMatches(pattern, body);

                score += titleCount * TITLE_WEIGHT * WEAK_WEIGHT;
                score += bodyCount  * BODY_WEIGHT  * WEAK_WEIGHT;
            }

            for (Pattern pattern : deptRule.negative()) {

                int titleCount = countMatches(pattern, title);
                int bodyCount  = countMatches(pattern, body);

                score += titleCount * TITLE_WEIGHT * NEGATIVE_WEIGHT;
                score += bodyCount  * BODY_WEIGHT  * NEGATIVE_WEIGHT;
            }

            scores.put(deptName, score);
        }
        return scores;
    }


    private static Map<String, Map<String, CompiledRules>> buildCache (RoutingRules rules) {

        Map<String, Map<String, CompiledRules>> cache = new HashMap<>();

        Map<String, TenantRules> tenants = rules.getTenants();

        if (tenants == null || tenants.isEmpty()) {
            return cache;
        }

        for (Map.Entry<String, TenantRules> tenantEntry: tenants.entrySet()) {

            String tenantID = tenantEntry.getKey();
            TenantRules tenantRules = tenantEntry.getValue();

            Map<String, CompiledRules> perDepartment = new HashMap<>();

            Map<String, DepartmentRules> departments = tenantRules.getDepartments();

            for (Map.Entry<String, DepartmentRules> departmentEntry: departments.entrySet()) {

                String departmentID = departmentEntry.getKey();
                DepartmentRules departmentRules = departmentEntry.getValue();

                CompiledRules compiledRules = new CompiledRules(
                        compileKeywords(departmentRules.getStrong()),
                        compileKeywords(departmentRules.getWeak()),
                        compileKeywords(departmentRules.getNegative())
                );
                perDepartment.put(departmentID, compiledRules);

            }
            cache.put(tenantID, perDepartment);
        }
        return cache;
    }

    private static List<Pattern> compileKeywords(List<String> keywords) {

        if (keywords == null || keywords.isEmpty()) return List.of();

        return keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(KeywordScorer::toPattern)
                .toList();
    }


    private static Pattern toPattern(String keyword) {

        boolean isPhrase = keyword.contains(" ");
        String regex;

        if (isPhrase) {
            String []parts = keyword.split("\\s+");
            String separator = "(?:\\s|[-/])+";
            String joined = String.join(separator, Arrays.stream(parts).map(Pattern::quote).toList());
             regex = "\\b" + joined + "\\b";
        } else {
             regex = "\\b" + Pattern.quote(keyword) + "\\b";
        }

        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("[^\\w\\s]", " ");
    }

    private int countMatches(Pattern pattern, String text) {
        int count = 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return Math.min(count, MAX_FREQ);
    }

}
