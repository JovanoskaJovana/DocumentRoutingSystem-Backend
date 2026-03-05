package mk.ukim.finki.routingsystem;


import mk.ukim.finki.routingsystem.config.RoutingTenantProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RoutingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutingSystemApplication.class, args);
	}

}
