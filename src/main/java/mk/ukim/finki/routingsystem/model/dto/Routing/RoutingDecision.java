package mk.ukim.finki.routingsystem.model.dto.Routing;

import java.util.List;
import java.util.Map;

public record RoutingDecision(
        String winner,
        List<String> tiedDepartments,
        boolean requiresManualReview,
        Map<String, Double> scores
) {
}
