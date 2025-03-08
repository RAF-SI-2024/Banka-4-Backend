package rs.banka4.user_service.domain.user.employee.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    Employee toEntity(CreateEmployeeDto dto);

    EmployeeDto toDto(Employee employee);
    EmployeeResponseDto toResponseDto(Employee employee);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGender")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(@MappingTarget Employee employee, UpdateEmployeeDto dto);

    @Named("mapGender")
    default User.Gender mapGender(String gender) {
        return gender != null ? User.Gender.valueOf(gender.toUpperCase()) : null;
    }

    @AfterMapping
    default void mapPrivileges(CreateEmployeeDto dto, @MappingTarget Employee employee) {
        if (dto.privilege() != null) {
            employee.setPrivileges(dto.privilege());
        }
    }
}