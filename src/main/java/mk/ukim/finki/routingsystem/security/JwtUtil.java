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

import static mk.ukim.finki.routingsystem.security.JwtConstants.JWT_EXPIRATION_TIME_MS;

@Component
public class JwtUtil {

    private Key key() {
        byte[] keyBytes = Decoders.BASE64.decode(JwtConstants.JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long employeeId, Role role, EmployeeType employeeType, Long departmentId) {
        Map<String, Object> claims = Map.of(
                "role", role.name(),
                "employeeType", employeeType.name(),
                "departmentId", departmentId
        );
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(employeeId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME_MS))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims verifyToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    }

    public Long employeeIdFromToken(String token) {
        return Long.valueOf(verifyToken(token).getSubject());
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
