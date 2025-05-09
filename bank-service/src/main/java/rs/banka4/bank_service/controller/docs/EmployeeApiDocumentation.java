package rs.banka4.bank_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.bank_service.domain.user.PrivilegesDto;
import rs.banka4.bank_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.bank_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.bank_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.bank_service.exceptions.user.DuplicateEmail;
import rs.banka4.bank_service.exceptions.user.DuplicateUsername;
import rs.banka4.bank_service.exceptions.user.UserNotFound;
import rs.banka4.rafeisen.common.dto.EmployeeResponseDto;

@Tag(
    name = "EmployeeController",
    description = "Endpoints for employees"
)
public interface EmployeeApiDocumentation {

    @Operation(
        summary = "Get Employee Privileges",
        description = "Retrieves the list of privileges for the authenticated employee. Requires Admin role.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved privileges",
                content = @Content(schema = @Schema(implementation = PrivilegesDto.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Token errors"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Admin permissions required"
            )
        }
    )
    ResponseEntity<PrivilegesDto> getPrivileges();

    @Operation(
        summary = "Get Employee Information",
        description = "Retrieves information about the authenticated employee using the token provided in the Authorization header.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved employee details",
                content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing token"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Access denied due to invalid token"
            )
        }
    )
    ResponseEntity<EmployeeResponseDto> me(Authentication auth);

    @Operation(
        summary = "Get Employee Information",
        description = "Retrieves information about an employee using the id provided as the path param. Requires Admin role.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved employee details",
                content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing token"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Access denied due to invalid token"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = UserNotFound.class))
            )
        }
    )
    ResponseEntity<EmployeeResponseDto> getEmployeeById(
        @Parameter(description = "Employee ID") UUID id
    );

    @Operation(
        summary = "Create a new Employee",
        description = "Creates a new employee with the provided details. Requires admin privileges.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successfully created new employee"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Admin privileges required"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Duplicate email",
                content = @Content(schema = @Schema(implementation = DuplicateEmail.class))
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Duplicate username",
                content = @Content(schema = @Schema(implementation = DuplicateUsername.class))
            ),
        }
    )
    ResponseEntity<Void> createEmployee(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Details of the new employee to create",
            required = true
        ) @Valid CreateEmployeeDto createEmployeeDto
    );

    @Operation(
        summary = "Search Employees",
        description = "Searches for employees based on the provided filters. Admin access required.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved employees list",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EmployeeDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid data for search filters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Admin privileges required"
            )
        }
    )
    ResponseEntity<Page<EmployeeDto>> getEmployees(
        @Parameter(description = "First name of the employee") String firstName,
        @Parameter(description = "Last name of the employee") String lastName,
        @Parameter(description = "Email address of the employee") String email,
        @Parameter(description = "Position of the employee") String position,
        @Parameter(description = "Page number") int page,
        @Parameter(description = "Number of employees per page") int size
    );

    @Operation(
        summary = "Update Employee",
        description = "Allows an admin to update the details of an employee, including personal and job information.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully updated employee"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid data provided"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = UserNotFound.class))
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Duplicate email",
                content = @Content(schema = @Schema(implementation = DuplicateEmail.class))
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Duplicate username",
                content = @Content(schema = @Schema(implementation = DuplicateUsername.class))
            )
        }
    )
    ResponseEntity<Void> updateEmployee(
        @Parameter(description = "Employee ID") UUID id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User details for change"
        ) @Valid UpdateEmployeeDto updateEmployeeDto
    );

    @Operation(
        summary = "Search Actuary Employees",
        description = """
            Returns a paginated list of actuary employees filtered by first name, last name, email, or position.

            - If the authenticated user has ADMIN privileges, they will see all SUPERVISORS.
            - If the authenticated user has SUPERVISOR privileges, they will see all AGENTS.

            Any other user role is forbidden from accessing this endpoint.
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved list of actuary employees",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EmployeeDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing token"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - You do not have permission to access this resource"
            )
        }
    )
    Page<EmployeeDto> getActuaryEmployees(
        @Parameter(description = "First name of the employee") String firstName,
        @Parameter(description = "Last name of the employee") String lastName,
        @Parameter(description = "Email of the employee") String email,
        @Parameter(description = "Position of the employee") String position,
        @Parameter(description = "Page number (starts from 0)") int page,
        @Parameter(description = "Number of records per page") int size
    );
}
