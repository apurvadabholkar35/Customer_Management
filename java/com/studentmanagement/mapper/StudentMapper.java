package com.studentmanagement.mapper;

import com.studentmanagement.dto.StudentDto;
import com.studentmanagement.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDto studentToStudentDto(Student student);

    @Mapping(target = "grade", ignore = true) // Corrected: grade is in StudentDto (source)
    Student studentDtoToStudent(StudentDto studentDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "grade", ignore = true) // Corrected: grade is in StudentDto (source)
    void updateStudentFromDto(StudentDto studentDto, @MappingTarget Student student);
}