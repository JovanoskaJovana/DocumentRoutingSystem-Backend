package mk.ukim.finki.routingsystem.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public class TenantProperties {

    private String adminDeptKey;

    private Map<String, DepartmentProperties> departments;

}
