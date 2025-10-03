package mk.ukim.finki.routingsystem.security;

public final class JwtConstants {

    private JwtConstants() {}

    public static final String JWT_SECRET = "96fad2fdc8b75912fa4e32c1ca4a763f447b7c1ff76fc1a07a9ef1d43d017b53";

    public static final long JWT_EXPIRATION_TIME_MS = 24 * 60 * 60 * 1000L; //24h

    public static String HEADER_STRING = "Authorization";

    public static String TOKEN_PREFIX = "Bearer ";
}
