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

    private final RoutingProperties routingProperties;
    private final EmployeeRepository employeeRepository;

    // precompiled regexes for department id
    private final Map<Long, List<Pattern>> compiled;

    public RoutingRulesImpl(RoutingProperties routingProperties, EmployeeRepository employeeRepository) {
        this.routingProperties = routingProperties;
        this.compiled = Collections.unmodifiableMap(compile(routingProperties.getKeywordRules()));
        this.employeeRepository = employeeRepository;
    }


    // returns a map -> key:list of pattern objects
    // uses Pattern objects for compiling once at startup and then just reusing them

    private Map<Long, List<Pattern>> compile(Map<Long, List<String>> source) {

        Map<Long, List<Pattern>> regexPattern = new LinkedHashMap<>();

        for (var e : source.entrySet()) {

            List<Pattern> patterns = new ArrayList<>();

            for (String keyWord : e.getValue()) {

                if (keyWord == null || keyWord.isBlank()) {
                    continue;
                }

                patterns.add(word(keyWord));
            }
            regexPattern.put(e.getKey(), patterns);
        }
        return regexPattern;
    }


    // regex pattern - avoids false matches

    private static Pattern word(String s) {

        String w = s.toLowerCase(Locale.ROOT);
        return Pattern.compile("\\b" + Pattern.quote(w) + "\\b", Pattern.CASE_INSENSITIVE);

    }


    @Override
    public RoutingResultDto routeToDepartmentAndEmployees(TitleAndBody tab) {

        String title = Optional.ofNullable(tab.title()).orElse("");
        String body = Optional.ofNullable(tab.body()).orElse("");
        String text = (title + "\n" + body).toLowerCase(Locale.ROOT);


        Map<Long, List<Pattern>> rules = compiled; // snapshot

        List<Long> matchByTitle = new ArrayList<>();

        for (var rule : rules.entrySet()) {
            Long deptId = rule.getKey();
            List<Pattern> patterns = rule.getValue();


            // find if a pattern object is a match to the title

            for (Pattern pattern : patterns) {
                if (pattern.matcher(title).find()) {
                    matchByTitle.add(deptId);
                    break;
                }
            }
        }

        if (matchByTitle.size() == 1) {
            Long departmentId = matchByTitle.getFirst();

            List<Employee> signatories = employeeRepository
                    .findAllByDepartment_IdAndType(departmentId, EmployeeType.SIGNATORY);
            return new RoutingResultDto(departmentId, signatories);
        }


        // if there's more than one department that is a match, restrict scoring to just those
        Collection<Long> candidates = matchByTitle.isEmpty() ? rules.keySet() : matchByTitle;

        long matchDept = -1L;
        long bestScore = -1;
        boolean tie = false;

        // if we have matches, skip the non-match IDs
        for (Long deptId : candidates) {

            int score = 0;

            for (Pattern pattern : rules.get(deptId)) {

                var matched = pattern.matcher(text);
                // loop the whole text
                while (matched.find()) {
                    score++;
                }
            }

            if (score > bestScore) {

                bestScore = score;
                matchDept = deptId;
                tie = false;

            } else if (score == bestScore && score > 0) {
                tie = true; // multiple departments with the same best score
            }
        }

        Long finalDeptMatch = bestScore > 0 && matchDept != -1L && !tie ? matchDept : routingProperties.getAdminDepartmentId();

        List<Employee> signatories2 = employeeRepository
                .findAllByDepartment_IdAndType(finalDeptMatch, EmployeeType.SIGNATORY);

        // fallback - if the document can't be routed to any of teh departments
        return new RoutingResultDto(finalDeptMatch, signatories2);


    }

}
