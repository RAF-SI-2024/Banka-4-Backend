package rs.banka4.user_service.config;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class WhiteListConfig {

    public static final String[] WHITE_LIST_URL = {
            "/auth/employee/login",
            "/auth/client/login",
            "/auth/refresh-token",
            "/auth/forgot-password/*",
            "/auth/verify",
            "/docs/**"
    };

    private static final List<Pattern> WHITE_LIST_URL_PATTERNS = List.of(
            Pattern.compile("/docs/.*"),
            Pattern.compile("/auth/forgot-password/.*")
    );

    public static boolean isWhitelisted(String requestURI) {
        return Arrays.stream(WHITE_LIST_URL).anyMatch(requestURI::startsWith) ||
                WHITE_LIST_URL_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(requestURI).matches());
    }

}