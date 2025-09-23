package mk.ukim.finki.routingsystem.service.rules;

import mk.ukim.finki.routingsystem.model.dto.TitleAndBody;

public interface RoutingRules {

    // decide department id from extracted text and uploader context.
    // must always return a department id

    Long routeToDepartmentId(TitleAndBody tab);

}
