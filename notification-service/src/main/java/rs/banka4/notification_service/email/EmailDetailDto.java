package rs.banka4.notification_service.email;

import java.util.Map;

public record EmailDetailDto(
     String to,
     String subject,
     Map<String, Object> dynamicValue,
     String templateName
) {
}
