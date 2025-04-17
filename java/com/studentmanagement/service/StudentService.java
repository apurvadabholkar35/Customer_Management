package com.studentmanagement.service;

import java.util.List;
import java.util.Optional;
import com.studentmanagement.entity.Student;


public interface StudentService {

	List<Student> getAllStudents();

	Optional<Student> getStudentById(Long id);

	void deleteStudent(Long id);

	Student createStudent(Student student);

	Student updateStudent(Long id, Student student);
}