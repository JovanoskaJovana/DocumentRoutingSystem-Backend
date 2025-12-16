package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.config.RoutingProperties;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.dto.Document.RoutingResultDto;
import mk.ukim.finki.routingsystem.model.dto.Document.TitleAndBody;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import mk.ukim.finki.routingsystem.service.rules.RoutingRules;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class RoutingRulesImpl implements RoutingRules {

    private static final int DEFAULT_MIN_MATCHES = 2;
    private final RoutingProperties routingProperties;
    private final EmployeeRepository employeeRepository;

    private final Map<Long, List<Pattern>> compiled;

    private final Map<Long, Integer> minMatches;

    public RoutingRulesImpl(RoutingProperties routingProperties,
                            EmployeeRepository employeeRepository) {
        this.routingProperties = routingProperties;
        this.employeeRepository = employeeRepository;
        this.compiled = Collections.unmodifiableMap(compile(routingProperties.getKeywordRules()));

        Map<Long, Integer> mm = routingProperties.getMinMatches();
        this.minMatches = mm != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(mm))
                : Collections.emptyMap();
    }

    private Map<Long, List<Pattern>> compile(Map<Long, List<String>> source) {
        Map<Long, List<Pattern>> regexPattern = new LinkedHashMap<>();

        if (source == null) {
            return regexPattern;
        }

        for (var e : source.entrySet()) {
            Long deptId = e.getKey();
            List<String> keywords = e.getValue();

            if (keywords == null) {
                continue;
            }

            List<Pattern> patterns = new ArrayList<>();

            for (String keyWord : keywords) {
                if (keyWord == null || keyWord.isBlank()) {
                    continue;
                }
                patterns.add(word(keyWord));
            }

            regexPattern.put(deptId, patterns);
        }

        return regexPattern;
    }

    private static Pattern word(String s) {
        String w = s.toLowerCase(Locale.ROOT);
        return Pattern.compile("\\b" + Pattern.quote(w) + "\\b", Pattern.CASE_INSENSITIVE);
    }

    private int getThresholdForDept(Long deptId) {
        return Optional.ofNullable(minMatches.get(deptId))
                .orElse(DEFAULT_MIN_MATCHES);
    }

    private Optional<Long> pickUniqueWinner(Map<Long, Integer> scores) {
        if (scores.isEmpty()) {
            return Optional.empty();
        }

        int maxScore = scores.values().stream().max(Integer::compareTo).orElse(0);

        List<Long> winners = new ArrayList<>();
        for (var e : scores.entrySet()) {
            if (e.getValue() == maxScore) {
                winners.add(e.getKey());
            }
        }

        if (winners.size() == 1) {
            return Optional.of(winners.getFirst());
        }

        // tie
        return Optional.empty();
    }

    @Override
    public RoutingResultDto routeToDepartmentAndEmployees(TitleAndBody tab) {

        String title = Optional.ofNullable(tab.title()).orElse("");
        String body = Optional.ofNullable(tab.body()).orElse("");
        String text = title + "\n" + body;

        Map<Long, List<Pattern>> rules = compiled; // snapshot


        Map<Long, Integer> titleScores = new HashMap<>();

        for (var entry : rules.entrySet()) {
            Long deptId = entry.getKey();
            List<Pattern> patterns = entry.getValue();

            int score = 0;
            for (Pattern pattern : patterns) {
                var matcher = pattern.matcher(title);
                while (matcher.find()) {
                    score++;
                }
            }

            int threshold = getThresholdForDept(deptId);
            if (score >= threshold) {
                titleScores.put(deptId, score);
            }
        }

        if (!titleScores.isEmpty()) {
            Optional<Long> winnerOpt = pickUniqueWinner(titleScores);
            if (winnerOpt.isPresent()) {
                Long winnerDeptId = winnerOpt.get();
                List<Employee> signatories = employeeRepository
                        .findAllByDepartment_IdAndType(winnerDeptId, EmployeeType.SIGNATORY);
                return new RoutingResultDto(winnerDeptId, signatories);
            }
        }

        Map<Long, Integer> textScores = new HashMap<>();

        for (var entry : rules.entrySet()) {
            Long deptId = entry.getKey();
            List<Pattern> patterns = entry.getValue();

            int score = 0;
            for (Pattern pattern : patterns) {
                var matcher = pattern.matcher(text);
                while (matcher.find()) {
                    score++;
                }
            }

            int threshold = getThresholdForDept(deptId);
            if (score >= threshold) {
                textScores.put(deptId, score);
            }
        }

        Long finalDeptMatch;

        if (textScores.isEmpty()) {
            finalDeptMatch = routingProperties.getAdminDepartmentId();
        } else {
            Optional<Long> winnerOpt = pickUniqueWinner(textScores);
            if (winnerOpt.isPresent()) {
                finalDeptMatch = winnerOpt.get();
            } else {
                finalDeptMatch = routingProperties.getAdminDepartmentId();
            }
        }

        List<Employee> signatories = employeeRepository
                .findAllByDepartment_IdAndType(finalDeptMatch, EmployeeType.SIGNATORY);

        return new RoutingResultDto(finalDeptMatch, signatories);
    }
}
