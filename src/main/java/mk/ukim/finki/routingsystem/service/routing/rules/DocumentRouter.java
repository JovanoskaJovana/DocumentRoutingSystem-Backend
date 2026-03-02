package mk.ukim.finki.routingsystem.service.routing.rules;

import mk.ukim.finki.routingsystem.model.dto.Routing.RoutingDecision;
import mk.ukim.finki.routingsystem.model.dto.Routing.RoutingResultDto;
import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;

public interface DocumentRouter {

    RoutingDecision route(String tenantName, TitleAndBody document);

}
