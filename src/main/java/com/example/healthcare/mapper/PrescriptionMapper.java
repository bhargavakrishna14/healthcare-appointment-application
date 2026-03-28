package com.example.healthcare.mapper;

import com.example.healthcare.dto.response.PrescriptionResponse;
import com.example.healthcare.model.nosql.Prescription;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {

    public PrescriptionResponse toResponse(Prescription prescription) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .appointmentId(prescription.getAppointmentId())
                .patientName(prescription.getPatientName())
                .doctorName(prescription.getDoctorName())
                .prescriptionDate(prescription.getPrescriptionDate())
                .medicines(prescription.getMedicines())
                .diagnosis(prescription.getDiagnosis())
                .instructions(prescription.getInstructions())
                .build();
    }
}
