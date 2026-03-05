package mk.ukim.finki.routingsystem.service.routing.rules.implementation;

import mk.ukim.finki.routingsystem.model.dto.Routing.RoutingDecision;
import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;
import mk.ukim.finki.routingsystem.service.routing.rules.DocumentRouter;

import java.util.Map;

public class DocumentRouterImpl implements DocumentRouter {

    private final KeywordScorer keywordScorer;
    private final RoutingDecisionMaker decisionMaker;

    public DocumentRouterImpl(KeywordScorer keywordScorer, RoutingDecisionMaker decisionMaker) {
        this.keywordScorer = keywordScorer;
        this.decisionMaker = decisionMaker;
    }

    @Override
    public RoutingDecision route(String tenantCode, TitleAndBody document) {

        Map<String, Double> score = keywordScorer.score(tenantCode, document);

        return decisionMaker.decide(score);

    }
}
