package mk.ukim.finki.routingsystem.domain.rules;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DepartmentRules {

    private List<String> strong;

    private List<String> weak;

    private List<String> negative;

}
