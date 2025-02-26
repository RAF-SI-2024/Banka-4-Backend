package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.service.abstraction.EmployeeService;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/privileges")
    public ResponseEntity<PrivilegesDto> getPrivileges() {
        return employeeService.getPrivileges();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDto> me(@RequestHeader("Authorization") String authorization) {
        return employeeService.getMe(authorization);
    }

    @PostMapping()
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid CreateEmployeeDto createEmployeeDto) {
        return employeeService.createEmployee(createEmployeeDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeDto>> getEmployees(@RequestParam(required = false) String firstName,
                                                          @RequestParam(required = false) String lastName,
                                                          @RequestParam(required = false) String email,
                                                          @RequestParam(required = false) String position,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return employeeService.getAll(firstName, lastName, email, position, PageRequest.of(page, size));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateEmployee(@PathVariable String id, @RequestBody @Valid EmployeeUpdateDto employeeUpdateDto){
        return employeeService.updateEmployee(id,employeeUpdateDto);
    }
}
