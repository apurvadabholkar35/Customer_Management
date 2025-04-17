package com.studentmanagement.service;

import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.StudentNotFoundException;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {

	private final StudentRepository studentRepository;

	@Autowired
	public StudentServiceImpl(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	 @Override
	    public List<Student> getAllStudents() {
	        List<Student> students = studentRepository.findAll();
	        for (Student student : students) {
	            calculateGrade(student.getMarkobtained(),student.getAdmissiondate(),student); 
	        }
	        return students;
	    }

	    @Override
	    public Optional<Student> getStudentById(Long id) {
	        Optional<Student> studentOptional = studentRepository.findById(id);
	        return studentOptional;
	    }

	    @Override
	    public Student createStudent(Student student) {
	        return studentRepository.save(student);
	    }

	    @Override
	    public Student updateStudent(Long id, Student student) {
	        Optional<Student> existingStudent = studentRepository.findById(id);
	        if (existingStudent.isPresent()) {
	            Student studentToUpdate = existingStudent.get();
	            studentToUpdate.setName(student.getName());
	            studentToUpdate.setEmail(student.getEmail());
	            studentToUpdate.setMarkobtained(student.getMarkobtained());
	            studentToUpdate.setAdmissiondate(student.getAdmissiondate());
	            calculateGrade(studentToUpdate.getMarkobtained(),studentToUpdate.getAdmissiondate(),student); 
	            return studentRepository.save(studentToUpdate);
	        } else {
	            throw new StudentNotFoundException("Student not found with id: " + id);
	        }
	    }

	    @Override
	    public void deleteStudent(Long id) {
	        if (studentRepository.existsById(id)) {
	            studentRepository.deleteById(id);
	        } else {
	            throw new StudentNotFoundException("Student not found with id: " + id);
	        }
	    }
	

	    public String calculateGrade(int marksObtained, LocalDate admissionDate,Student student) {
	        if (marksObtained > 40) {
	            LocalDate now = LocalDate.now(java.time.Clock.system(java.time.ZoneId.of("Asia/Kolkata"))); 
	            long monthsSinceAdmission = ChronoUnit.MONTHS.between(admissionDate, now);

	            if (marksObtained >= 90 && monthsSinceAdmission <= 6) {
	            	student.setGrade("Platinum");
	            } else if (marksObtained >= 80 && marksObtained < 90 && monthsSinceAdmission <= 12) {
	            	student.setGrade("Merit");
	            } else {
	            	student.setGrade("Pass");
	            }
	        }
	        return null; 
	    }
	    
	    

	
}