package rs.banka4.user_service.dto;

import java.util.Map;

public record NotificationTransferDto(
        String topic,
        String recipient,
        Map<String, Object> params
) {}