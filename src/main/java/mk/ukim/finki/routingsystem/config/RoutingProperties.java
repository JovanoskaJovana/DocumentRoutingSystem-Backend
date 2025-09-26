package mk.ukim.finki.routingsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.routing")
@Data
public class RoutingProperties {

    private Long adminDepartmentId = 1L;

    private Map<Long, List<String>> keywordRules = Map.of();

//    public Long getAdminDeptId() {
//        return adminDepartmentId;
//    }
//
//    public void setAdminDeptId(Long adminDeptId) {
//        this.adminDepartmentId = adminDeptId;
//    }
//
//    public Map<Long, List<String>> getKeywordRules() {
//        return keywordRules;
//    }
//
//    public void setKeywordRules(Map<Long, List<String>> keywordRules) {
//        this.keywordRules = keywordRules;
//    }

}
