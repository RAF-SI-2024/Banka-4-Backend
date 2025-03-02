package rs.banka4.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreateEmployeeDto;
import rs.banka4.user_service.dto.requests.UpdateEmployeeDto;
import rs.banka4.user_service.service.abstraction.EmployeeService;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
@Tag(name = "EmployeeController", description = "Endpoints for employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(
            summary = "Get Employee Privileges",
            description = "Retrieves the list of privileges for the authenticated employee. Requires Admin role.",
            security = @SecurityRequirement(name = "bearerAuth"), // Requires authentication
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved privileges",
                            content = @Content(schema = @Schema(implementation = PrivilegesDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin permissions required")
            }
    )
    @GetMapping("/privileges")
    public ResponseEntity<PrivilegesDto> getPrivileges() {
        return employeeService.getPrivileges();
    }

    @Operation(
            summary = "Get Employee Information",
            description = "Retrieves information about the authenticated employee using the token provided in the Authorization header.",
            security = @SecurityRequirement(name = "bearerAuth"), // Specifies the use of authentication
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved employee details",
                            content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied due to invalid token")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponseDto> me(Authentication auth) {
        return employeeService.getMe((String) auth.getCredentials());
    }

    @Operation(
            summary = "Get Employee Information",
            description = "Retrieves information about the authenticated employee using the id provided as the path param. Requires Admin role",
            security = @SecurityRequirement(name = "bearerAuth"), // Specifies the use of authentication
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved employee details",
                            content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied due to invalid token"),
                    @ApiResponse(responseCode = "404", description = "Bad Request - User id can't be found.")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployee(@PathVariable String id) {
        return employeeService.getEmployee(id);
    }

    @Operation(
            summary = "Create a new Employee",
            description = "Creates a new employee with the provided details. Requires admin privileges.",
            security = @SecurityRequirement(name = "bearerAuth"), // Specifies the use of authentication
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created new employee"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or errors such as duplicate email/username"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin privileges required")
            }
    )
    @PostMapping()
    public ResponseEntity<Void> createEmployee(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the new employee to create", required = true) @RequestBody @Valid CreateEmployeeDto createEmployeeDto) {
        return employeeService.createEmployee(createEmployeeDto);
    }

    @Operation(
            summary = "Search Employees",
            description = "Searches for employees based on the provided filters. Admin access required.",
            security = @SecurityRequirement(name = "bearerAuth"), // Specifies the use of authentication
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved employees list",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data for search filters"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin privileges required")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeDto>> getEmployees(
            @RequestParam(required = false) @Parameter(description = "First name of the employee") String firstName,
            @RequestParam(required = false) @Parameter(description = "Last name of the employee") String lastName,
            @RequestParam(required = false) @Parameter(description = "Email address of the employee") String email,
            @RequestParam(required = false) @Parameter(description = "Position of the employee") String position,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of employees per page") int size) {
        return employeeService.getAll(firstName, lastName, email, position, PageRequest.of(page, size));
    }

    @Operation(
            summary = "Update Employee",
            description = "Allows an admin to update the details of an employee, including personal and job information.",
            security = @SecurityRequirement(name = "bearerAuth"), // Specifies the use of authentication
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated employee"),
                    @ApiResponse(responseCode = "400", description = "Invalid data provided"),
                    @ApiResponse(responseCode = "409", description = "Duplicate email or username"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin privileges required")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateEmployee(@PathVariable String id,
                                               @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User details for change")
                                               @RequestBody @Valid UpdateEmployeeDto updateEmployeeDto){
        return employeeService.updateEmployee(id, updateEmployeeDto);
    }
}
