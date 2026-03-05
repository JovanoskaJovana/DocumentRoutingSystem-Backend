package mk.ukim.finki.routingsystem.domain.mapper;

import mk.ukim.finki.routingsystem.config.DepartmentProperties;
import mk.ukim.finki.routingsystem.config.RoutingTenantProperties;
import mk.ukim.finki.routingsystem.config.TenantProperties;
import mk.ukim.finki.routingsystem.domain.rules.RoutingRules;
import mk.ukim.finki.routingsystem.domain.rules.DepartmentRules;
import mk.ukim.finki.routingsystem.domain.rules.TenantRules;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoutingRulesMapper {


    DepartmentRules toDomain(DepartmentProperties departmentProperties);

    RoutingRules toDomain(RoutingTenantProperties routingTenantProperties);

}
