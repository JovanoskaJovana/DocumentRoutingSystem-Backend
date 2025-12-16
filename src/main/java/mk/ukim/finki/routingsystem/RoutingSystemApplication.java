package mk.ukim.finki.routingsystem;

import mk.ukim.finki.routingsystem.config.RoutingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RoutingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutingSystemApplication.class, args);
	}

	@Bean
	public RoutingProperties routingProperties() { return new RoutingProperties(); }


}
