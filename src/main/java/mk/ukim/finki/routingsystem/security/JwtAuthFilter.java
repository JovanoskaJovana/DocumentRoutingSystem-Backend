package mk.ukim.finki.routingsystem.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static mk.ukim.finki.routingsystem.security.JwtProperties.TOKEN_PREFIX;
import static mk.ukim.finki.routingsystem.security.JwtProperties.HEADER_STRING;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER_STRING);
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(TOKEN_PREFIX.length());

        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            Long employeeId = jwtUtil.employeeIdFromToken(token);
            String firstName = jwtUtil.nameFromToken(token);
            Role role = jwtUtil.roleFromToken(token);
            EmployeeType employeeType = jwtUtil.employeeTypeFromToken(token);
            Long departmentId = jwtUtil.departmentIdFromToken(token);

            EmployeePrincipal employeePrincipal = new EmployeePrincipal(employeeId, firstName, role, employeeType, departmentId);
            var auth = new UsernamePasswordAuthenticationToken(employeePrincipal, null, employeePrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        catch (JwtException jwtException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().print("{\"error\":\"invalid_or_expired_token\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

}
