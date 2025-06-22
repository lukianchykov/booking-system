package com.lukianchykov.bookingsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.lukianchykov.bookingsystem.config.BookingSystemTestConfiguration;
import com.lukianchykov.bookingsystem.domain.AccommodationType;
import com.lukianchykov.bookingsystem.domain.Booking;
import com.lukianchykov.bookingsystem.domain.BookingStatus;
import com.lukianchykov.bookingsystem.domain.Unit;
import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.dto.BookingCreateRequest;
import com.lukianchykov.bookingsystem.dto.BookingResponse;
import com.lukianchykov.bookingsystem.dto.UnitCreateRequest;
import com.lukianchykov.bookingsystem.dto.UnitResponse;
import com.lukianchykov.bookingsystem.dto.UserCreateRequest;
import com.lukianchykov.bookingsystem.dto.UserResponse;
import com.lukianchykov.bookingsystem.repository.BookingRepository;
import com.lukianchykov.bookingsystem.repository.UnitRepository;
import com.lukianchykov.bookingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
@Import({BookingSystemTestConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RecordApplicationEvents
@Transactional
class BookingSystemIT {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    private User testOwner;

    private Unit testUnit;

    @BeforeEach
    void setUp() {
        UserCreateRequest userRequest = UserCreateRequest.builder()
            .email("user@test.com")
            .name("Test User")
            .build();
        UserResponse userResponse = userService.createUser(userRequest);
        testUser = userRepository.findById(userResponse.getId()).orElseThrow();

        UserCreateRequest ownerRequest = UserCreateRequest.builder()
            .email("owner@test.com")
            .name("Test Owner")
            .build();
        UserResponse ownerResponse = userService.createUser(ownerRequest);
        testOwner = userRepository.findById(ownerResponse.getId()).orElseThrow();

        UnitCreateRequest unitRequest = UnitCreateRequest.builder()
            .numberOfRooms(2)
            .accommodationType(AccommodationType.FLAT)
            .floor(3)
            .baseCost(new BigDecimal("100.00"))
            .description("Test apartment")
            .ownerId(testOwner.getId())
            .build();
        UnitResponse unitResponse = unitService.createUnit(unitRequest);
        testUnit = unitRepository.findById(unitResponse.getId()).orElseThrow();
    }

    @Test
    void testUserServiceIntegration() {
        UserCreateRequest request = UserCreateRequest.builder()
            .email("newuser@test.com")
            .name("New User")
            .build();

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("newuser@test.com");
        assertThat(response.getName()).isEqualTo("New User");

        Optional<User> savedUser = userRepository.findById(response.getId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("newuser@test.com");
    }

    @Test
    void testUserServiceDuplicateEmailThrowsException() {
        UserCreateRequest request = UserCreateRequest.builder()
            .email("user@test.com")
            .name("Duplicate User")
            .build();

        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User with email already exists");
    }

    @Test
    void testUserServiceGetUser() {
        UserResponse response = userService.getUser(testUser.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUser.getId());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getName()).isEqualTo(testUser.getName());
    }

    @Test
    void testUserServiceGetNonExistentUser() {
        assertThatThrownBy(() -> userService.getUser(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    void testUnitServiceIntegration() {
        UnitCreateRequest request = UnitCreateRequest.builder()
            .numberOfRooms(1)
            .accommodationType(AccommodationType.APARTMENTS)
            .floor(1)
            .baseCost(new BigDecimal("80.00"))
            .description("Test studio")
            .ownerId(testOwner.getId())
            .build();

        UnitResponse response = unitService.createUnit(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getNumberOfRooms()).isEqualTo(1);
        assertThat(response.getAccommodationType()).isEqualTo(AccommodationType.APARTMENTS);
        assertThat(response.getFloor()).isEqualTo(1);
        assertThat(response.getBaseCost()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(response.getDescription()).isEqualTo("Test studio");

        Optional<Unit> savedUnit = unitRepository.findById(response.getId());
        assertThat(savedUnit).isPresent();
        assertThat(savedUnit.get().getOwner().getId()).isEqualTo(testOwner.getId());
    }

    @Test
    void testUnitServiceCreateUnitWithNonExistentOwner() {
        UnitCreateRequest request = UnitCreateRequest.builder()
            .numberOfRooms(1)
            .accommodationType(AccommodationType.APARTMENTS)
            .floor(1)
            .baseCost(new BigDecimal("80.00"))
            .description("Test studio")
            .ownerId(999L)
            .build();

        assertThatThrownBy(() -> unitService.createUnit(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found with ID: 999");
    }

    @Test
    void testUnitServiceGetUnit() {
        UnitResponse response = unitService.getUnit(testUnit.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUnit.getId());
        assertThat(response.getNumberOfRooms()).isEqualTo(testUnit.getNumberOfRooms());
        assertThat(response.getAccommodationType()).isEqualTo(testUnit.getAccommodationType());
    }

    @Test
    void testBookingServiceCreateBooking() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest request = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(testUser.getId())
            .startDate(startDate)
            .endDate(endDate)
            .build();

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUnitId()).isEqualTo(testUnit.getId());
        assertThat(response.getUserId()).isEqualTo(testUser.getId());
        assertThat(response.getStartDate()).isEqualTo(startDate);
        assertThat(response.getEndDate()).isEqualTo(endDate);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);

        Optional<Booking> savedBooking = bookingRepository.findById(response.getId());
        assertThat(savedBooking).isPresent();
        assertThat(savedBooking.get().getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void testBookingServiceCreateBookingWithNonExistentUnit() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest request = BookingCreateRequest.builder()
            .unitId(999L)
            .userId(testUser.getId())
            .startDate(startDate)
            .endDate(endDate)
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Unit not found");
    }

    @Test
    void testBookingServiceCreateBookingWithNonExistentUser() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest request = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(999L)
            .startDate(startDate)
            .endDate(endDate)
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    void testBookingServiceCreateConflictingBooking() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest firstRequest = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(testUser.getId())
            .startDate(startDate)
            .endDate(endDate)
            .build();
        bookingService.createBooking(firstRequest);

        BookingCreateRequest conflictingRequest = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(testUser.getId())
            .startDate(startDate.plusDays(1))
            .endDate(endDate.plusDays(1))
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(conflictingRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Unit is not available for the selected dates");
    }

    @Test
    void testBookingServiceCancelBooking() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest request = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(testUser.getId())
            .startDate(startDate)
            .endDate(endDate)
            .build();
        BookingResponse createdBooking = bookingService.createBooking(request);

        BookingResponse cancelledBooking = bookingService.cancelBooking(createdBooking.getId());

        assertThat(cancelledBooking.getId()).isEqualTo(createdBooking.getId());
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        Optional<Booking> savedBooking = bookingRepository.findById(createdBooking.getId());
        assertThat(savedBooking).isPresent();
        assertThat(savedBooking.get().getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void testBookingServiceCancelNonExistentBooking() {
        assertThatThrownBy(() -> bookingService.cancelBooking(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Booking not found");
    }

    @Test
    void testBookingServiceCancelAlreadyCancelledBooking() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest request = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(testUser.getId())
            .startDate(startDate)
            .endDate(endDate)
            .build();
        BookingResponse booking = bookingService.createBooking(request);
        bookingService.cancelBooking(booking.getId());

        assertThatThrownBy(() -> bookingService.cancelBooking(booking.getId()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cannot cancel booking with status: CANCELLED");
    }

    @Test
    void testBookingServiceGetBooking() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest request = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(testUser.getId())
            .startDate(startDate)
            .endDate(endDate)
            .build();
        BookingResponse createdBooking = bookingService.createBooking(request);

        BookingResponse retrievedBooking = bookingService.getBooking(createdBooking.getId());

        assertThat(retrievedBooking).isNotNull();
        assertThat(retrievedBooking.getId()).isEqualTo(createdBooking.getId());
        assertThat(retrievedBooking.getUnitId()).isEqualTo(testUnit.getId());
        assertThat(retrievedBooking.getUserId()).isEqualTo(testUser.getId());
        assertThat(retrievedBooking.getStartDate()).isEqualTo(startDate);
        assertThat(retrievedBooking.getEndDate()).isEqualTo(endDate);
        assertThat(retrievedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void testBookingServiceGetNonExistentBooking() {
        assertThatThrownBy(() -> bookingService.getBooking(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Booking not found");
    }

    @Test
    void testTransactionalBehavior() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        BookingCreateRequest request = BookingCreateRequest.builder()
            .unitId(testUnit.getId())
            .userId(999L)
            .startDate(startDate)
            .endDate(endDate)
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(RuntimeException.class);

        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).isEmpty();
    }
}