package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class EmployeeNotFound extends BaseApiException{
    public EmployeeNotFound(String employeeId) {
        super(HttpStatus.NOT_FOUND,Map.of("employeeId",employeeId));
    }
}
