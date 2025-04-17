package com.studentmanagement.test;

import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.StudentNotFoundException;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.StudentServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private final LocalDate now = LocalDate.now();
    private final Student student1 = new Student(1L, "John Doe", "john.doe@example.com", 85, now.minusMonths(5), "Merit");
    private final Student student2 = new Student(2L, "Jane Smith", "jane.smith@example.com", 92, now.minusYears(1), "Pass");

    @Test
    void getAllStudents_shouldReturnAllStudentsWithCalculatedGrade() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(
                new Student(1L, "John Doe", "john.doe@example.com", 85, now.minusMonths(5), null),
                new Student(2L, "Jane Smith", "jane.smith@example.com", 92, now.minusYears(1), null)
        ));
        List<Student> students = studentService.getAllStudents();
        Assertions.assertEquals(2, students.size());
        Assertions.assertEquals("Merit", students.get(0).getGrade());
        Assertions.assertEquals("Pass", students.get(1).getGrade());
    }

    @Test
    void getStudentById_shouldReturnStudent_whenIdExists() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        Optional<Student> studentOptional = studentService.getStudentById(1L);
        Assertions.assertTrue(studentOptional.isPresent());
        Assertions.assertEquals("John Doe", studentOptional.get().getName());
    }

    @Test
    void getStudentById_shouldReturnEmptyOptional_whenIdDoesNotExist() {
        when(studentRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<Student> studentOptional = studentService.getStudentById(3L);
        Assertions.assertTrue(studentOptional.isEmpty());
    }

    @Test
    void createStudent_shouldSaveAndReturnNewStudentWithCalculatedGrade() {
        Student newStudent = new Student(null, "Peter Pan", "peter.pan@example.com", 78, now.minusMonths(2), null);
        Student savedStudent = new Student(3L, "Peter Pan", "peter.pan@example.com", 78, now.minusMonths(2), "Pass");
        when(studentRepository.save(newStudent)).thenReturn(savedStudent);
        Student createdStudent = studentService.createStudent(newStudent);
        Assertions.assertEquals(3L, createdStudent.getId());
        Assertions.assertEquals("Pass", createdStudent.getGrade());
    }

    @Test
    void updateStudent_shouldUpdateAndReturnExistingStudentWithCalculatedGrade_whenIdExists() {
        Student existingStudent = new Student(1L, "John Doe", "john.doe@example.com", 85, now.minusMonths(5), null);
        Student updatedDetails = new Student(1L, "John Updated", "john.updated@example.com", 88, now.minusMonths(3), null);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(Mockito.any(Student.class))).thenReturn(new Student(1L, "John Updated", "john.updated@example.com", 88, now.minusMonths(3), "Merit"));

        Student updatedStudent = studentService.updateStudent(1L, updatedDetails);
        Assertions.assertEquals("John Updated", updatedStudent.getName());
        Assertions.assertEquals("Merit", updatedStudent.getGrade());
    }

    @Test
    void updateStudent_shouldThrowNotFoundException_whenIdDoesNotExist() {
        Student updatedDetails = new Student(4L, "Non Existent", "non.existent@example.com", 90, now.minusMonths(1), null);
        when(studentRepository.findById(4L)).thenReturn(Optional.empty());
        Assertions.assertThrows(StudentNotFoundException.class, () -> studentService.updateStudent(4L, updatedDetails));
    }

    @Test
    void deleteStudent_shouldDeleteStudent_whenIdExists() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(1L);
        studentService.deleteStudent(1L);
        verify(studentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteStudent_shouldThrowNotFoundException_whenIdDoesNotExist() {
        when(studentRepository.existsById(5L)).thenReturn(false);
        Assertions.assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudent(5L));
        verify(studentRepository, never()).deleteById(5L);
    }

    @Test
    void calculateGrade_shouldReturnPlatinum_whenMarksAbove90AndAdmissionWithin6Months() {
        LocalDate admissionDate = now.minusMonths(3);
        String grade = studentService.calculateGrade(92, admissionDate, student1);
        Assertions.assertEquals("Platinum", grade);
    }

    @Test
    void calculateGrade_shouldReturnMerit_whenMarksBetween80And90AndAdmissionWithin12Months() {
        LocalDate admissionDate = now.minusMonths(8);
        String grade = studentService.calculateGrade(85, admissionDate, student1);
        Assertions.assertEquals("Merit", grade);
    }

    @Test
    void calculateGrade_shouldReturnPass_whenMarksAbove40AndOtherConditionsNotMet() {
        LocalDate admissionDate = now.minusYears(2);
        String grade = studentService.calculateGrade(70, admissionDate, student1);
        Assertions.assertEquals("Pass", grade);
    }

    @Test
    void calculateGrade_shouldReturnNull_whenMarksBelowOrEqual40() {
        LocalDate admissionDate = now.minusMonths(1);
        String grade = studentService.calculateGrade(35, admissionDate, student1);
        Assertions.assertNull(grade);
    }
}