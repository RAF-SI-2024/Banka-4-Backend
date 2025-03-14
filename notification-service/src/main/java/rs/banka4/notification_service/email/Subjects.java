package rs.banka4.notification_service.email;

import lombok.Getter;

@Getter
public enum Subjects {
    ACTIVATION("Activate your account"),
    PASSWORD_RESET("Reset password"),
    LOAN_PENALTY("Loan penalty"),
    LOAN_INSTALLMENT_PAID("Loan installment paid"),
    LOAN_INSTALLMENT_PAYMENT_DENIED("Loan installment payment denied");


    private final String subject;

    Subjects(String subject) {
        this.subject = subject;
    }

}
