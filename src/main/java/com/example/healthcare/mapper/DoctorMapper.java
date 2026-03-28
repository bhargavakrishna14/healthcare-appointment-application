package com.example.healthcare.mapper;

import com.example.healthcare.dto.response.DoctorResponse;
import com.example.healthcare.model.sql.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .specialty(doctor.getSpecialty())
                .username(doctor.getUser().getUsername())
                .email(doctor.getUser().getEmail())
                .build();
    }
}
