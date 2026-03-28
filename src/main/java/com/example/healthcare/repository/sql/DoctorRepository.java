package com.example.healthcare.repository.sql;

import com.example.healthcare.model.sql.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);

    Optional<Doctor> findByUserId(Long userId);
}
