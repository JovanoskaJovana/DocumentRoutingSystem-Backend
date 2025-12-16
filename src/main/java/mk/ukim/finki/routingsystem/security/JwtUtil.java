package mk.ukim.finki.routingsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;


@Component
public class JwtUtil {

    private final JwtProperties props;
    private final Key key;

    public JwtUtil(JwtProperties props) {
        this.props = props;
        byte[] keyBytes = Decoders.BASE64.decode(props.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private Key key() {
        return this.key;
    }

    public String generateToken(Long employeeId, String fullName, Role role, EmployeeType employeeType, Long departmentId) {
        Map<String, Object> claims = Map.of(
                "firstName", fullName,
                "role", role.name(),
                "employeeType", employeeType.name(),
                "departmentId", departmentId
        );
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(employeeId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getExpiration()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims verifyToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    }

    public Long employeeIdFromToken(String token) {
        return Long.valueOf(verifyToken(token).getSubject());
    }

    public String nameFromToken(String token) {
        return verifyToken(token).get("firstName", String.class);
    }

    public Role roleFromToken(String token) {
        return Role.valueOf(verifyToken(token).get("role", String.class));
    }

    public EmployeeType employeeTypeFromToken(String token) {
        return EmployeeType.valueOf(verifyToken(token).get("employeeType", String.class));
    }
    public Long departmentIdFromToken(String token) {
        Object dept = verifyToken(token).get("departmentId");
        return dept == null ? null : Long.valueOf(dept.toString());
    }


}
