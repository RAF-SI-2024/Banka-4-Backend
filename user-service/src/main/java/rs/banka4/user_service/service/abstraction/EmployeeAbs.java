package rs.banka4.user_service.service.abstraction;

import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeRequest;
import rs.banka4.user_service.domain.user.employee.db.Employee;

public interface EmployeeAbs {
    Employee createEmployee(CreateEmployeeRequest dto);
}
