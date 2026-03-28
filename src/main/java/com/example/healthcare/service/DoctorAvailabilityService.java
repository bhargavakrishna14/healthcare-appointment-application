package com.example.healthcare.service;

import com.example.healthcare.dto.request.DoctorAvailabilityRequest;
import com.example.healthcare.dto.response.DoctorAvailabilityResponse;
import com.example.healthcare.exception.DatabaseOperationException;
import com.example.healthcare.exception.ResourceNotFoundException;
import com.example.healthcare.mapper.DoctorAvailabilityMapper;
import com.example.healthcare.model.sql.Doctor;
import com.example.healthcare.model.sql.DoctorAvailability;
import com.example.healthcare.repository.sql.DoctorAvailabilityRepository;
import com.example.healthcare.repository.sql.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityMapper availabilityMapper;

    // ==================== SET AVAILABILITY (Doctor sets own) ====================

    @Transactional
    public DoctorAvailabilityResponse setAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        Doctor doctor = findDoctorOrThrow(doctorId);

        DoctorAvailability availability;
        try {
            availability = availabilityRepository
                    .findByDoctorIdAndDayOfWeek(doctorId, request.getDayOfWeek())
                    .orElse(new DoctorAvailability());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to check existing availability for doctor id: " + doctorId, ex);
        }

        availability.setDoctor(doctor);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotDurationMinutes(request.getSlotDurationMinutes());

        try {
            DoctorAvailability saved = availabilityRepository.save(availability);
            return availabilityMapper.toResponse(saved);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to save availability for doctor id: " + doctorId, ex);
        }
    }

    // ==================== GET ====================

    public List<DoctorAvailabilityResponse> getDoctorAvailability(Long doctorId) {
        try {
            if (!doctorRepository.existsById(doctorId)) {
                throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
            }

            return availabilityRepository.findByDoctorId(doctorId).stream()
                    .map(availabilityMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch availability for doctor id: " + doctorId, ex);
        }
    }

    // ==================== DELETE ====================

    @Transactional
    public void deleteAvailability(Long id) {
        try {
            if (!availabilityRepository.existsById(id)) {
                throw new ResourceNotFoundException("Availability not found with id: " + id);
            }
            availabilityRepository.deleteById(id);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete availability with id: " + id, ex);
        }
    }

    // ==================== HELPER ====================

    private Doctor findDoctorOrThrow(Long id) {
        try {
            return doctorRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to fetch doctor with id: " + id, ex);
        }
    }
}
