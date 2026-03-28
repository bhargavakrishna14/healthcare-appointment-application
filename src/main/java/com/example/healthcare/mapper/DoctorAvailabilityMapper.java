package com.example.healthcare.mapper;

import com.example.healthcare.dto.response.DoctorAvailabilityResponse;
import com.example.healthcare.model.sql.DoctorAvailability;
import org.springframework.stereotype.Component;

@Component
public class DoctorAvailabilityMapper {

    public DoctorAvailabilityResponse toResponse(DoctorAvailability availability) {
        return DoctorAvailabilityResponse.builder()
                .id(availability.getId())
                .doctorId(availability.getDoctor().getId())
                .doctorName(availability.getDoctor().getName())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .slotDurationMinutes(availability.getSlotDurationMinutes())
                .build();
    }
}
