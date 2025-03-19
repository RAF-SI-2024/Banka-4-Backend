package rs.banka4.user_service.exceptions.user.employee;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class EmployeeNotFound extends BaseApiException {
    public EmployeeNotFound(String employeeId) {
        super(HttpStatus.NOT_FOUND, Map.of("employeeId", employeeId));
    }
}
