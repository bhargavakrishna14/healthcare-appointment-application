package com.example.healthcare.repository.nosql;

import com.example.healthcare.model.nosql.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    Optional<Prescription> findByAppointmentId(Long appointmentId);

    List<Prescription> findByPatientId(Long patientId);

    List<Prescription> findByDoctorId(Long doctorId);
}
