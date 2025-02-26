package rs.banka4.notification_service.email;

import lombok.Getter;

@Getter
public enum Subjects {
    ACTIVATION("Activate your account"),
    PASSWORD_RESET("Reset password");

    private final String subject;

    Subjects(String subject) {
        this.subject = subject;
    }

}
