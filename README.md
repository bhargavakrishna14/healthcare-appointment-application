# 🏥 Healthcare Appointment Application

<div align="center">

A full-featured **Spring Boot 4** healthcare system for managing patients, doctors, appointments, prescriptions, and medical records — with **JWT authentication**, **role-based authorization**, **dual database architecture** (MySQL + MongoDB), **AOP logging**, **Hibernate caching**, and comprehensive **unit testing**.

</div>

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Design Patterns & Strategies](#-design-patterns--strategies)
- [Spring Concepts Used](#-spring-concepts-used)
- [Database Design](#-database-design)
- [API Endpoints](#-api-endpoints)
- [Authorization Matrix](#-authorization-matrix)
- [Security Architecture](#-security-architecture)
- [Caching Strategy](#-caching-strategy)
- [AOP Logging](#-aop-logging)
- [Exception Handling](#-exception-handling)
- [Testing Strategy](#-testing-strategy)
- [Running the Application](#-running-the-application)
- [Postman Collection](#-postman-collection)
- [Screenshots](#-screenshots)

---

## 🏗 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT (Postman)                     │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP Requests (JSON)
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY LAYER                           │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │ JWT Filter   │→ │ SecurityConfig│→ │ Role-Based Access│  │
│  │ (extracts    │  │ (URL rules)  │  │ ADMIN / DOCTOR /  │  │
│  │  token)      │  │              │  │ PATIENT           │  │
│  └──────────────┘  └──────────────┘  └───────────────────┘  │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     AOP LAYER (Logging)                     │
│  @LogAppointment  → logs booking/cancellation/completion    │
│  @LogPrescription → logs prescription create/update         │
│  @LogDoctor       → logs doctor CRUD + cache miss/evict     │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER (REST)                   │
│  AuthController │ DoctorController │ AppointmentController  │
│  AdminController│ PatientController│ PrescriptionController │
│                 │ AvailabilityCtrl │ MedicalRecordController│
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER (Business Logic)           │
│  AuthService    │ DoctorService    │ AppointmentService     │
│  AdminService   │ PatientService   │ PrescriptionService    │
│                 │ AvailabilityServ │ MedicalRecordService   │
└────────┬────────────────────────────────────┬───────────────┘
         │                                    │
         ▼                                    ▼
┌────────────────────────┐    ┌───────────────────────────────┐
│     MySQL (JPA)        │    │       MongoDB (NoSQL)         │
│  ┌──────────────────┐  │    │  ┌─────────────────────────┐  │
│  │ User             │  │    │  │ Prescription (document) │  │
│  │ Doctor           │  │    │  │ MedicalRecord (document)│  │
│  │ Patient          │  │    │  └─────────────────────────┘  │
│  │ Appointment      │  │    └───────────────────────────────┘
│  │ DoctorAvailability│ │
│  └──────────────────┘  │
│                        │
│  ┌──────────────────┐  │
│  │ Ehcache (L2)     │  │
│  │ Doctor queries   │  │
│  └──────────────────┘  │
└────────────────────────┘
```

---

## 🧠 Design Patterns & Strategies

### 1. **Layered Architecture (N-Tier)**
```
Controller → Service → Repository → Database
```
Each layer has a single responsibility. Controllers handle HTTP, services handle business logic, repositories handle data access. No layer skips another.

### 2. **Repository Pattern**
```java
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);
}
```
Abstracts database access behind interfaces. Spring Data JPA auto-generates implementations at runtime.

### 3. **DTO Pattern (Data Transfer Object)**
```
Client ←→ Controller ←→ DTO ←→ Service ←→ Entity ←→ Database
```
- **Request DTOs**: validate and accept client input (`DoctorRequest`)
- **Response DTOs**: shape what the client sees (`DoctorResponse`)
- **Entities are never exposed** to the client directly

### 4. **Mapper Pattern**
```java
public class DoctorMapper {
    public DoctorResponse toResponse(Doctor doctor) { ... }
}
```
Dedicated classes convert between entities and DTOs. Keeps conversion logic out of services.

### 5. **Builder Pattern (via Lombok)**
```java
Doctor.builder()
    .name("Dr. Smith")
    .specialty("Cardiology")
    .build();
```
Lombok's `@Builder` generates fluent builder classes for clean object construction.

### 6. **Singleton Pattern (Spring Beans)**
All Spring beans (`@Service`, `@Repository`, `@Controller`) are singletons by default — one instance shared across the entire application.

### 7. **Proxy Pattern (AOP)**
```java
@LogAppointment
public AppointmentResponse bookAppointment(...) { }
```
Spring AOP creates proxy objects around annotated methods to inject logging behavior without modifying business logic.

### 8. **Filter Chain Pattern (Security)**
```
Request → JwtFilter → SecurityFilterChain → Controller
```
Each filter processes the request and passes it to the next. JWT extraction happens before authorization checks.

### 9. **Strategy Pattern (Cache Providers)**
Hibernate's caching uses JCache (JSR-107) as an abstraction. Ehcache is the concrete strategy — swappable without code changes.

### 10. **Template Method Pattern (Spring Data)**
`JpaRepository` and `MongoRepository` provide template methods (`save`, `findById`, `deleteAll`) — you override by defining query method signatures.

---

## 🌱 Spring Concepts Used

| Concept | Where Used | Explanation |
|---------|-----------|-------------|
| **IoC (Inversion of Control)** | Entire application | Spring manages object creation — you never use `new Service()` |
| **DI (Dependency Injection)** | `@RequiredArgsConstructor` | Spring injects dependencies via constructor automatically |
| **Bean Lifecycle** | `DataSeeder` | `@PostConstruct` / `CommandLineRunner` seeds admin on startup |
| **Bean Scopes** | All `@Service`, `@Repository` | Default singleton scope — one instance per bean |
| **AOP** | `LoggingAspect.java` | Cross-cutting logging via `@Around` advice |
| **Spring Security** | `SecurityConfig` | URL-based role authorization + JWT filter |
| **Spring Data JPA** | SQL repositories | Auto-generated CRUD for MySQL entities |
| **Spring Data MongoDB** | NoSQL repositories | Auto-generated CRUD for MongoDB documents |
| **Spring Cache** | `@Cacheable`, `@CacheEvict` | Method-level caching on DoctorService |
| **Hibernate L2 Cache** | `ehcache.xml` | Entity-level caching for Doctor queries |
| **Validation** | `@Valid` on controllers | Automatic DTO validation before processing |
| **Exception Handling** | `@RestControllerAdvice` | Global error handling with consistent JSON responses |
| **Profiles** | `application.yml` | Environment-specific configuration |

---

---

## 🗄 Database Design

### MySQL (Relational — JPA/Hibernate)

```
┌──────────────────┐       ┌──────────────────┐
│      User        │       │     Doctor       │
├──────────────────┤       ├──────────────────┤
│ id (PK)          │──┐    │ id (PK)          │
│ username (unique)│  │    │ name             │
│ email (unique)   │  ├───→│ specialty        │
│ password (hash)  │  │    │ user_id (FK→User)│
│ role (enum)      │  │    │ created_at       │
│ enabled          │  │    │ updated_at       │
└──────────────────┘  │    └──────┬───────────┘
                      │           │
                      │    ┌──────┴───────────────┐
                      │    │ DoctorAvailability   │
                      │    ├──────────────────────┤
                      │    │ id (PK)              │
                      │    │ doctor_id (FK→Doctor)│
                      │    │ day_of_week (enum)   │
                      │    │ start_time           │
                      │    │ end_time             │
                      │    │ slot_duration_minutes│
                      │    └──────────────────────┘
                      │
┌──────────────────┐  │    ┌───────────────────────┐
│     Patient      │  │    │    Appointment        │
├──────────────────┤  │    ├───────────────────────┤
│ id (PK)          │←─┤    │ id (PK)               │
│ name             │  │    │ patient_id (FK→Patient)│
│ date_of_birth    │  ├───→│ doctor_id (FK→Doctor)  │
│ phone            │       │ appointment_date       │
│ address          │       │ start_time             │
│ user_id (FK→User)│       │ end_time               │
│ created_at       │       │ status (enum)          │
│ updated_at       │       │ reason                 │
└──────────────────┘       │ notes                  │
                           └────────────────────────┘
```

### MongoDB (NoSQL — Documents)

```json
// Prescription Document
{
    "_id": "ObjectId",
    "appointmentId": 1,
    "patientId": 1,
    "patientName": "John Doe",
    "doctorId": 1,
    "doctorName": "Dr. Smith",
    "medicines": ["Aspirin 100mg", "Vitamin D"],
    "diagnosis": "Mild chest pain",
    "instructions": "Take after meals for 2 weeks",
    "createdAt": "2026-02-18T10:30:00",
    "updatedAt": "2026-02-18T10:30:00"
}

// MedicalRecord Document
{
    "_id": "ObjectId",
    "patientId": 1,
    "patientName": "John Doe",
    "recordDate": "2026-02-18",
    "title": "Cardiology Consultation",
    "description": "Patient presented with chest pain. ECG normal.",
    "prescriptionIds": ["ObjectId"],
    "labReports": ["ECG Report - Normal", "Blood Test - Normal"],
    "createdAt": "2026-02-18T11:00:00"
}
```

### Why Dual Database?

| Data | Database | Reason |
|------|----------|--------|
| Users, Doctors, Patients, Appointments | **MySQL** | Structured, relational, FK constraints, ACID transactions |
| Prescriptions, Medical Records | **MongoDB** | Flexible schema, nested arrays, varying fields per record |

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/auth/login` | Login with username/password, returns JWT | Public |
| `POST` | `/api/auth/register/admin` | Register new admin | Admin |
| `POST` | `/api/auth/register/doctor` | Register new doctor | Admin |
| `POST` | `/api/auth/register/patient` | Register new patient | Admin |

### Admin

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/admin` | List all admins | Admin |
| `GET` | `/api/admin/search?id=1` | Get admin by ID | Admin |
| `DELETE` | `/api/admin/reset` | Reset database (keeps admin) | Admin |

### Doctors

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/doctors` | List all doctors | All authenticated |
| `GET` | `/api/doctors/search?id=1` | Get doctor by ID | All authenticated |
| `GET` | `/api/doctors/specialty?specialty=Cardiology` | Search by specialty | All authenticated |
| `PUT` | `/api/doctors/{id}` | Update doctor | Admin |
| `DELETE` | `/api/doctors/{id}` | Delete doctor | Admin |

### Patients

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/patients` | List all patients | Admin, Doctor |
| `GET` | `/api/patients/search?id=1` | Get patient by ID | Admin, Doctor |
| `PUT` | `/api/patients/{id}` | Update patient | Admin, Patient |
| `DELETE` | `/api/patients/{id}` | Delete patient | Admin |

### Doctor Availability

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/availability/doctor/{doctorId}` | Set weekly availability | Doctor |
| `GET` | `/api/availability/doctor/{doctorId}` | Get doctor's schedule | All authenticated |
| `DELETE` | `/api/availability/{id}` | Remove availability slot | Doctor |

### Appointments

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/appointments/patient/{patientId}` | Book appointment | Patient |
| `GET` | `/api/appointments/search?id=1` | Get appointment by ID | All authenticated |
| `GET` | `/api/appointments/patient/{patientId}` | Get patient appointments | All authenticated |
| `GET` | `/api/appointments/doctor/{doctorId}` | Get doctor appointments | All authenticated |
| `GET` | `/api/appointments/available-slots?doctorId=1&date=2026-02-18` | Get available time slots | All authenticated |
| `PATCH` | `/api/appointments/{id}/complete?notes=...` | Mark as completed | Doctor |
| `PATCH` | `/api/appointments/{id}/cancel` | Cancel appointment | Patient |

### Prescriptions (MongoDB)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/prescriptions` | Create prescription | Doctor |
| `GET` | `/api/prescriptions/search?id=abc123` | Get by ID | Doctor, Patient |
| `GET` | `/api/prescriptions/appointment?appointmentId=1` | Get by appointment | Doctor, Patient |
| `GET` | `/api/prescriptions/patient/{patientId}` | Get patient's prescriptions | Doctor, Patient |
| `GET` | `/api/prescriptions/doctor/{doctorId}` | Get doctor's prescriptions | Doctor, Patient |
| `PUT` | `/api/prescriptions/{id}` | Update prescription | Doctor |

### Medical Records (MongoDB)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/medical-records` | Create record | Doctor |
| `GET` | `/api/medical-records/search?id=abc123` | Get by ID | Doctor, Patient |
| `GET` | `/api/medical-records/patient/{patientId}` | Get patient's records | Doctor, Patient |
| `PUT` | `/api/medical-records/{id}` | Update record | Doctor |
| `DELETE` | `/api/medical-records/{id}` | Delete record | Doctor |

---

## 🔒 Authorization Matrix

| Endpoint | Method | 🔴 ADMIN | 🔵 DOCTOR | 🟢 PATIENT | 🔓 No Token |
|----------|--------|----------|-----------|------------|-------------|
| `/api/auth/login` | POST | ✅ | ✅ | ✅ | ✅ |
| `/api/auth/register/**` | POST | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/admin/reset` | DELETE | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/doctors` | GET | ✅ | ✅ | ✅ | ❌ 401 |
| `/api/doctors/{id}` | PUT | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/doctors/{id}` | DELETE | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/patients` | GET | ✅ | ✅ | ❌ 403 | ❌ 401 |
| `/api/patients/{id}` | PUT | ✅ | ❌ 403 | ✅ | ❌ 401 |
| `/api/patients/{id}` | DELETE | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/availability/**` | POST | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/availability/**` | GET | ✅ | ✅ | ✅ | ❌ 401 |
| `/api/appointments/**/book` | POST | ❌ 403 | ❌ 403 | ✅ | ❌ 401 |
| `/api/appointments/**/complete` | PATCH | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/appointments/**/cancel` | PATCH | ❌ 403 | ❌ 403 | ✅ | ❌ 401 |
| `/api/appointments/**` | GET | ✅ | ✅ | ✅ | ❌ 401 |
| `/api/prescriptions` | POST | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/prescriptions/**` | GET | ❌ 403 | ✅ | ✅ | ❌ 401 |
| `/api/medical-records` | POST | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/medical-records/**` | GET | ❌ 403 | ✅ | ✅ | ❌ 401 |
| `/api/medical-records/{id}` | DELETE | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |

---

## 🔐 Security Architecture

### Authentication Flow

```
1. Client sends POST /api/auth/login { username, password }
                    │
2. AuthService validates credentials against MySQL
                    │
3. JwtService generates JWT token (HS256, 24h expiry)
                    │
4. Client receives { token: "eyJhb...", role: "PATIENT" }
                    │
5. Client includes token in all subsequent requests:
   Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
                    │
6. JwtAuthenticationFilter extracts & validates token
                    │
7. SecurityConfig checks role against endpoint rules
                    │
8. Request proceeds or is rejected (401/403)
```

### Password Security

- Passwords are hashed using **BCrypt** (never stored in plain text)
- BCrypt includes a salt automatically — same password produces different hashes
- `PasswordEncoder` bean handles encoding and verification

---

## ⚡ Caching Strategy

### Three Levels of Caching

```
┌────────────────────────────────────────────────────────┐
│  Level 1: Hibernate First-Level Cache (Session)        │
│  ├── Automatic, per-transaction                        │
│  ├── Same entity queried twice in one request → 1 SQL  │
│  └── No configuration needed                           │
├────────────────────────────────────────────────────────┤
│  Level 2: Hibernate Second-Level Cache (Ehcache)       │
│  ├── @Cache on Doctor entity                           │
│  ├── Shared across all sessions/transactions           │
│  ├── Configured in ehcache.xml (TTL, heap size)        │
│  └── Same doctor queried by different users → 0 SQL    │
├────────────────────────────────────────────────────────┤
│  Level 3: Spring Cache (@Cacheable)                    │
│  ├── Method-level caching on DoctorService             │
│  ├── getAllDoctors() → cached, skips service logic     │
│  ├── @CacheEvict on update/delete → refreshes cache    │
│  └── Uses Ehcache as provider                          │
└────────────────────────────────────────────────────────┘
```

### Cache Annotations Used

```java
@Cacheable("allDoctors")
@LogDoctor(action = "GET_ALL", cacheAction = "MISS")
public List<DoctorResponse> getAllDoctors() { ... }

@CacheEvict(value = "allDoctors", allEntries = true)
@LogDoctor(action = "UPDATE", cacheAction = "EVICT")
public DoctorResponse updateDoctor(Long id, ...) { ... }
```

### Ehcache Configuration

| Cache Region | TTL | Heap Size | Purpose |
|-------------|-----|-----------|---------|
| `allDoctors` | 15 min | 1 entry | List of all doctors |
| `doctorById` | 30 min | 200 entries | Individual doctor lookups |
| `doctorsBySpecialty` | 30 min | 50 entries | Specialty search results |
| `default-update-timestamps-region` | ∞ | 1000 entries | Hibernate query cache timestamps |
| `default-query-results-region` | 30 min | 100 entries | Hibernate query results |

---

## 📋 AOP Logging

### Custom Annotations

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAppointment { String action(); }

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogPrescription { String action(); }

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogDoctor {
    String action();                      // "GET_ALL", "GET_BY_ID", "UPDATE", "DELETE"
    String cacheAction() default "NONE";  // "MISS", "EVICT", "NONE"
}
```

### What Gets Logged

| Event | Log Level | Example Message |
|-------|-----------|-----------------|
| Appointment booking attempt | INFO | `[APPOINTMENT] Attempting to BOOK \| Args: [1, AppointmentRequest(...)]` |
| Appointment booked | INFO | `[APPOINTMENT] BOOK successful \| Result: AppointmentResponse(...)` |
| Appointment booking failed | ERROR | `[APPOINTMENT] BOOK failed \| Error: Time slot already booked for this doctor` |
| Appointment cancelled | INFO | `[APPOINTMENT] CANCEL successful \| Result: Appointment cancelled` |
| Appointment completed | INFO | `[APPOINTMENT] COMPLETE successful \| Result: AppointmentResponse(...)` |
| Prescription created | INFO | `[PRESCRIPTION] CREATE successful \| Result: PrescriptionResponse(...)` |
| Prescription updated | INFO | `[PRESCRIPTION] UPDATE successful \| Result: PrescriptionResponse(...)` |
| Prescription failed | ERROR | `[PRESCRIPTION] CREATE failed \| Error: Appointment not found with id: 99` |
| Doctor list fetched (cache miss) | INFO | `[DOCTOR] [CACHE MISS] GET_ALL — fetching from database \| Args: []` |
| Doctor fetched by ID (cache miss) | INFO | `[DOCTOR] [CACHE MISS] GET_BY_ID — fetching from database \| Args: [1]` |
| Doctor updated (cache evict) | INFO | `[DOCTOR] [CACHE EVICT] UPDATE — cache will be cleared \| Args: [5, DoctorRequest(...)]` |
| Doctor deleted (cache evict) | INFO | `[DOCTOR] [CACHE EVICT] DELETE — cache will be cleared \| Args: [5]` |
| Doctor operation failed | ERROR | `[DOCTOR] UPDATE failed \| Error: Doctor not found with id: 99` |
| Any service method > 500ms | WARN | `[PERFORMANCE] DoctorService.getAllDoctors() took 750ms (SLOW)` |
| Any service method ≤ 500ms | DEBUG | `[PERFORMANCE] DoctorService.getDoctorById() took 12ms` |

---

## 🛡 Exception Handling

All exceptions are handled globally via `@RestControllerAdvice` in `GlobalExceptionHandler`. Every repository operation is wrapped in `try-catch DataAccessException` at the service layer.

### Exception Handling Matrix

| Exception | HTTP Status | When It Fires |
|-----------|-------------|---------------|
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `DuplicateResourceException` | 409 | Duplicate username/email on registration |
| `DoubleBookingException` | 409 | Time slot conflict or doctor unavailable |
| `DatabaseOperationException` | 500 | Save/delete/find fails (connection, timeout) |
| `DataIntegrityViolationException` | 409 | FK constraint or unique violation on save |
| `DataAccessException` | 503 | Database unreachable or connection lost |
| `IllegalArgumentException` | 400 | Invalid state transition (cancel completed) |
| `BadCredentialsException` | 401 | Wrong username or password |
| `MethodArgumentNotValidException` | 400 | `@Valid` DTO validation fails |
| `MissingServletRequestParameterException` | 400 | Required `@RequestParam` missing |
| `MethodArgumentTypeMismatchException` | 400 | Wrong type for parameter (e.g. `?id=abc`) |
| `Exception` (catch-all) | 500 | Any other unexpected error |

---

## 🧪 Testing Strategy

### Unit Tests (JUnit 5 + Mockito)

| Test Class | Tests | What's Tested |
|-----------|-------|---------------|
| `DoctorServiceTest` | 8 | CRUD operations, not-found exceptions, specialty search |
| `PatientServiceTest` | 7 | CRUD operations, not-found exceptions |
| `AppointmentServiceTest` | 10 | Booking, double-booking prevention, cancellation, completion |
| **Total** | **25** | |

### Test Architecture

```
Test Class (thin)                    Helper (logic)
┌─────────────────────┐            ┌────────────────────────────┐
│ @Test               │            │                            │
│ @DisplayName("...") │───calls───→│ Sets up mocks (when/then)  │
│ void testName() {   │            │ Calls service method       │
│   helper.scenario() │            │ Asserts results            │
│ }                   │            │ Verifies interactions      │
└─────────────────────┘            └────────────────────────────┘
                                              │
                                    ┌─────────┴─────────┐
                                    │  TestDataHelper   │
                                    │  (shared factory) │
                                    │  createDoctor()   │
                                    │  createPatient()  │
                                    │  createAppointment()│
                                    └───────────────────┘
```

### Running Tests

```bash
# Unit tests (no DB required)
mvn test

# API tests (requires running app + MySQL + MongoDB)
# Import Postman-Collection into Postman
# Click "Run Collection" → runs all 35 requests in order
```


## 🚀 Running the Application

### Start Required Services

```bash
# Start MySQL
# Windows: services.msc → MySQL → Start
# Mac: brew services start mysql
# Linux: sudo systemctl start mysql

# Start MongoDB
# Windows: services.msc → MongoDB → Start
# Mac: brew services start mongodb-community
# Linux: sudo systemctl start mongod
```

### Run the Application

```bash
mvn spring-boot:run
```

### Default Admin Credentials

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |
| Role | `ADMIN` |

---

---

## 📸 Screenshots

### 1. Application Startup
![App Startup](screenshots/startup.png)

### 2. Postman - Full Collection Run
![Postman Run](screenshots/postman-run.png)

### 3. Postman - Appointment Booking
![Book Appointment](screenshots/book-appointment.png)

### 4. Postman - Double Booking Rejected
![Double Booking](screenshots/double-booking.png)

### 5. Postman - Security (401 & 403)
![Security 401](screenshots/security-401.png)
![Security 403](screenshots/security-403.png)

### 6. Unit Tests Passing
![Unit Tests](screenshots/unit-tests.png)

### 7. MySQL Database
![MySQL](screenshots/mysql-tables.png)

### 8. MongoDB Collections
![MongoDB](screenshots/mongodb-collections.png)

### 9. AOP Logging
![AOP Logs](screenshots/aop-logging.png)

### 10. Cache Behavior
![Cache](screenshots/cache-logs.png)

---
