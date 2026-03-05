package mk.ukim.finki.routingsystem.config;

import mk.ukim.finki.routingsystem.domain.mapper.RoutingRulesMapper;
import mk.ukim.finki.routingsystem.domain.rules.RoutingRules;
import mk.ukim.finki.routingsystem.service.routing.rules.DocumentRouter;
import mk.ukim.finki.routingsystem.service.routing.rules.implementation.DocumentRouterImpl;
import mk.ukim.finki.routingsystem.service.routing.rules.implementation.KeywordScorer;
import mk.ukim.finki.routingsystem.service.routing.rules.implementation.RoutingDecisionMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfig {

    @Bean
    public KeywordScorer keywordScorer(RoutingTenantProperties properties,
                                       RoutingRulesMapper mapper) {
        RoutingRules rules = mapper.toDomain(properties);

        return new KeywordScorer(rules);
    }

    @Bean
    public RoutingDecisionMaker routingDecisionMaker() {
        return new RoutingDecisionMaker();
    }

    @Bean
    public DocumentRouter documentRouter(KeywordScorer scorer,
                                         RoutingDecisionMaker decisionMaker) {
        return new DocumentRouterImpl(scorer, decisionMaker);
    }

}
