package rs.banka4.user_service.utils;

import rs.banka4.user_service.domain.auth.dtos.NotificationTransferDto;
import rs.banka4.user_service.domain.currency.db.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class MessageHelper {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static NotificationTransferDto createForgotPasswordMessage(String emailReceiver, String firstName, String verificationCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("verificationCode", verificationCode);
        return new NotificationTransferDto("password-reset", emailReceiver, params);
    }

    public static NotificationTransferDto createAccountActivationMessage(String emailReceiver, String firstName, String verificationCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("verificationCode", verificationCode);
        return new NotificationTransferDto("account-activation", emailReceiver, params);
    }

    public static NotificationTransferDto createLoanInstallmentPaidMessage(String emailReceiver,
                                                                           String firstName,
                                                                           Long loanNumber,
                                                                           BigDecimal installmentAmount,
                                                                           Currency.Code code,
                                                                           LocalDate date) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("loanNumber", loanNumber);
        params.put("installmentAmount", installmentAmount);
        params.put("currency_code", code);
        params.put("date", date.format(formatter));
        return new NotificationTransferDto("loan-installment-paid", emailReceiver, params);
    }

    public static NotificationTransferDto createLoanInstallmentPaymentDeniedMessage(String emailReceiver,
                                                                                    String firstName,
                                                                                    Long loanNumber,
                                                                                    BigDecimal installmentAmount,
                                                                                    Currency.Code code,
                                                                                    LocalDate date) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("loanNumber", loanNumber);
        params.put("installmentAmount", installmentAmount);
        params.put("currency_code", code);
        params.put("date", date.format(formatter));;
        return new NotificationTransferDto("loan-installment-payment-denied", emailReceiver, params);
    }

    public static NotificationTransferDto createLoanInstallmentPenaltyMessage(String emailReceiver,
                                                                              String firstName,
                                                                              Long loanNumber,
                                                                              BigDecimal penalty,
                                                                              LocalDate date) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("loanNumber", loanNumber);
        params.put("penalty", penalty);
        params.put("date", date.format(formatter));
        return new NotificationTransferDto("loan-penalty", emailReceiver, params);
    }
}
