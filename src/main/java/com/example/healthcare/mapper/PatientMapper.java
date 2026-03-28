package com.example.healthcare.mapper;

import com.example.healthcare.dto.response.PatientResponse;
import com.example.healthcare.model.sql.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .name(patient.getName())
                .dateOfBirth(patient.getDateOfBirth())
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .username(patient.getUser().getUsername())
                .email(patient.getUser().getEmail())
                .build();
    }
}
