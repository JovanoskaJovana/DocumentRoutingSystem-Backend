package mk.ukim.finki.routingsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false",
		"jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2Nzg=",
		"jwt.expiration=3600000"
})
class RoutingSystemApplicationTests {

	// its own database, so the test needs no environment variables and never touches the real one
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

	@Test
	void contextLoads() {
	}

}
