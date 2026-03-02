package mk.ukim.finki.routingsystem.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DepartmentProperties {

    private List<String> strong;
    private List<String> weak;
    private List<String> negative;

}
