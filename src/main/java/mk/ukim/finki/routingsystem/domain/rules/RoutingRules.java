package mk.ukim.finki.routingsystem.domain.rules;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RoutingRules {

    private Map<String, TenantRules> tenants;

}
