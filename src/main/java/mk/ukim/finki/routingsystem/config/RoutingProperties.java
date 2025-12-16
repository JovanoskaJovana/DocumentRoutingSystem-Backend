package mk.ukim.finki.routingsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.routing")
@Data
public class RoutingProperties {

    private Long adminDepartmentId = 1L;

    private Map<Long, Integer> minMatches = Map.of();

    private Map<Long, List<String>> keywordRules = Map.of();

}
