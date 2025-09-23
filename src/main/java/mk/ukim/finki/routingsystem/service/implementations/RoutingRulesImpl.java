package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.config.RoutingProperties;
import mk.ukim.finki.routingsystem.model.dto.TitleAndBody;
import mk.ukim.finki.routingsystem.service.rules.RoutingRules;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;


@Service
public class RoutingRulesImpl implements RoutingRules {

    private final RoutingProperties routingProperties;

    // precompiled regexes for department id
    private volatile Map<Long, List<Pattern>> compiled;

    public RoutingRulesImpl(RoutingProperties routingProperties) {
        this.routingProperties = routingProperties;
        this.compiled = compile(routingProperties.getKeywordRules());
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
    public Long routeToDepartmentId(TitleAndBody tab) {

        String title = Optional.ofNullable(tab.title()).orElse("");
        String body = Optional.ofNullable(tab.body()).orElse("");
        String text = (title + "\n" + body).toLowerCase(Locale.ROOT);


        Map<Long, List<Pattern>> rules = compiled; // snapshot

        List<Long> match = new ArrayList<>();

        for (var rule : rules.entrySet()) {
            Long deptId = rule.getKey();
            List<Pattern> patterns = rule.getValue();

            boolean matched = false;

            // find if a pattern object is a match to the title

            for (Pattern pattern : patterns) {
                if (pattern.matcher(title).find()) {
                    matched = true;
                    break;
                }
            }

            // add departmentId in the list in case more than one department is a match
            if (matched) {
                match.add(deptId);
            }
        }

        // if there's just one match, return that department's id
        if (match.size() == 1) {
            return match.getFirst();
        }

        // if there's more than one department that is a match, restrict scoring to just those
        Set<Long> allMatches = match.isEmpty() ? null : new HashSet<>(match);


        long matchDept = -1L;
        long bestScore = -1;
        boolean tie = false;

        for (var rule : rules.entrySet()) {

            Long deptId = rule.getKey();

            // if we have matches, skip the non-match IDs
            if (!allMatches.contains(deptId)) {

                int score = 0;

                 for (Pattern pattern : rule.getValue()) {

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
        }

        if (bestScore > 0 && matchDept != -1L && !tie) {
            return matchDept;
        }

        // fallback - if the document can't be routed to any of teh departments
        return routingProperties.getAdminDepartmentId();
    }
}
