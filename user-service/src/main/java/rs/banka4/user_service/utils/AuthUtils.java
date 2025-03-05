package rs.banka4.user_service.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.banka4.user_service.exceptions.AuthorizationException;
import rs.banka4.user_service.models.User;

@Slf4j
public class AuthUtils {

    public static User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (User) authentication.getPrincipal();
        }

        log.error("Not logged in!");
        throw new AuthorizationException();
    }

    public static String getLoggedUserId() {
        return getLoggedUser().getId();
    }
}
