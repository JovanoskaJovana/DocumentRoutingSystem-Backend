package mk.ukim.finki.routingsystem.service.rules;

import mk.ukim.finki.routingsystem.model.dto.Document.RoutingResultDto;
import mk.ukim.finki.routingsystem.model.dto.Document.TitleAndBody;

public interface RoutingRules {

    // decide department id from extracted text and uploader context.

    RoutingResultDto routeToDepartmentAndEmployees(TitleAndBody text);

}
