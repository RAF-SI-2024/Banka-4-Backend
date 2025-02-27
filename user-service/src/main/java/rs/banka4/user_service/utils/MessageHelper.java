package rs.banka4.user_service.utils;

import rs.banka4.user_service.dto.NotificationTransferDto;

import java.util.HashMap;
import java.util.Map;

public class MessageHelper {

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
}
