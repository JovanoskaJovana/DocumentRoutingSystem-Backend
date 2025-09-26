package mk.ukim.finki.routingsystem.service.rules;

import mk.ukim.finki.routingsystem.model.dto.RoutingResultDto;
import mk.ukim.finki.routingsystem.model.dto.TitleAndBody;

public interface RoutingRules {

    // decide department id from extracted text and uploader context.

    RoutingResultDto routeToDepartmentAndEmployees(TitleAndBody text);

}
