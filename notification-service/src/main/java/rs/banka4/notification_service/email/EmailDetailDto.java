package rs.banka4.notification_service.email;

import java.util.Map;

public record EmailDetailDto(
     String recipient,
     Map<String, Object> params,
     String topic
) {
}
