package com.example.healthcare.service;

import com.example.healthcare.dto.request.LoginRequest;
import com.example.healthcare.dto.request.RegisterAdminRequest;
import com.example.healthcare.dto.request.RegisterDoctorRequest;
import com.example.healthcare.dto.request.RegisterPatientRequest;
import com.example.healthcare.dto.response.JwtResponse;
import com.example.healthcare.exception.DatabaseOperationException;
import com.example.healthcare.exception.DuplicateResourceException;
import com.example.healthcare.model.enums.Role;
import com.example.healthcare.model.sql.Admin;
import com.example.healthcare.model.sql.Doctor;
import com.example.healthcare.model.sql.Patient;
import com.example.healthcare.model.sql.User;
import com.example.healthcare.repository.sql.AdminRepository;
import com.example.healthcare.repository.sql.DoctorRepository;
import com.example.healthcare.repository.sql.PatientRepository;
import com.example.healthcare.repository.sql.UserRepository;
import com.example.healthcare.security.CustomUserDetails;
import com.example.healthcare.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ==================== LOGIN ====================

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ==================== REGISTER ADMIN ====================

    @Transactional
    public void registerAdmin(RegisterAdminRequest request) {
        validateNewUser(request.getUsername(), request.getEmail());
        User savedUser = createUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.ADMIN);

        try {
            adminRepository.save(Admin.builder()
                    .user(savedUser)
                    .name(request.getName())
                    .department(request.getDepartment())
                    .build());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to register admin: " + request.getUsername(), ex);
        }
    }

    // ==================== REGISTER DOCTOR ====================

    @Transactional
    public void registerDoctor(RegisterDoctorRequest request) {
        validateNewUser(request.getUsername(), request.getEmail());
        User savedUser = createUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.DOCTOR);

        try {
            doctorRepository.save(Doctor.builder()
                    .user(savedUser)
                    .name(request.getName())
                    .specialty(request.getSpecialty())
                    .build());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to register doctor: " + request.getUsername(), ex);
        }
    }

    // ==================== REGISTER PATIENT ====================

    @Transactional
    public void registerPatient(RegisterPatientRequest request) {
        validateNewUser(request.getUsername(), request.getEmail());
        User savedUser = createUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.PATIENT);

        try {
            patientRepository.save(Patient.builder()
                    .user(savedUser)
                    .name(request.getName())
                    .dateOfBirth(request.getDateOfBirth())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .build());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to register patient: " + request.getUsername(), ex);
        }
    }

    // ==================== HELPERS ====================

    private void validateNewUser(String username, String email) {
        try {
            if (userRepository.existsByUsername(username)) {
                throw new DuplicateResourceException("Username already exists");
            }
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("Email already exists");
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to validate user: " + username, ex);
        }
    }

    private User createUser(String username, String email, String password, Role role) {
        try {
            return userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .enabled(true)
                    .build());
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to create user: " + username, ex);
        }
    }
}