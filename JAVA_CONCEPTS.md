# Java Concepts Demonstrated - Viva Preparation Guide

This document is specifically crafted for your **College Java Mini-Project Viva Voce**. It maps each core Java syllabus requirement to its exact location in this codebase with a clear, student-friendly explanation you can speak during your viva.

---

## Quick Reference Table for Viva

| Concept | Where It Is Used | File / Class | Code Example & Explanation |
| :--- | :--- | :--- | :--- |
| **Classes & Objects** | Entity representations | [Movie.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Movie.java)<br>[Seat.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Seat.java) | Real-world domain entities like Movies, Theatres, Seats, and Bookings are modeled as Java classes with state (fields) and behavior (methods). |
| **Encapsulation** | All domain models & DTOs | [Movie.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Movie.java)<br>[BaseEntity.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/BaseEntity.java) | Fields are marked `private` (data hiding) and accessed solely through `public` getters and setters with validation rules. |
| **Inheritance** | Entity & Person hierarchy | [BaseEntity.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/BaseEntity.java)<br>[Person.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Person.java)<br>[Customer.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Customer.java) | `Customer` extends abstract class `Person`, which extends `BaseEntity`. Promotes code reuse of common fields (`id`, `createdAt`, `name`, `email`, `phone`). |
| **Abstraction** | Abstract classes & Service contracts | [BaseEntity.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/BaseEntity.java)<br>[Person.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Person.java) | `BaseEntity` and `Person` are `abstract`. Subclasses must implement abstract method `getRoleDescription()`. |
| **Interfaces & Loose Coupling** | Service & Repository architecture | [BookingService.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/BookingService.java)<br>[MovieRepository.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/repository/MovieRepository.java) | Interfaces define public contracts (`BookingService`, `MovieService`) while implementation classes (`BookingServiceImpl`, `MovieServiceImpl`) provide actual logic, decoupling layers. |
| **Polymorphism (Dynamic)** | Interface dependency injection | Controllers injecting Service interfaces | The Controller depends on interface `MovieService`, but at runtime Spring injects `MovieServiceImpl`. |
| **Method Overriding (`@Override`)** | Base class & Object methods | [Customer.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Customer.java)<br>[Movie.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Movie.java) | `@Override public String getRoleDescription()` in `Customer` overrides `Person`. `@Override public String toString()` customizes object printing. |
| **Method Overloading** | Search & Constructor variations | [MovieService.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/MovieService.java)<br>[Movie.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Movie.java) | `searchMovies(String keyword)` vs `searchMovies(String keyword, MovieGenre genre)`: same method name with different parameter signatures. Multiple constructors in `Movie`. |
| **Constructors & `this` keyword** | Field initialization & chaining | [Movie.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Movie.java)<br>[BaseEntity.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/BaseEntity.java) | `this.title = title;` disambiguates instance variable from parameter. `this();` invokes no-arg constructor from an overloaded constructor. |
| **`super` keyword** | Invoking parent constructors | [Customer.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Customer.java)<br>[Person.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Person.java) | `super(customerId, name, email, phone);` passes initialization values up to `Person` and `BaseEntity` constructors. |
| **Enums** | Categorical domain values | [MovieGenre.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/enums/MovieGenre.java)<br>[SeatStatus.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/enums/SeatStatus.java)<br>[SeatType.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/enums/SeatType.java)<br>[BookingStatus.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/enums/BookingStatus.java) | `SeatStatus` (`AVAILABLE`, `BOOKED`), `SeatType` (`REGULAR`, `PREMIUM`). `SeatType` contains custom methods and price multiplier (`1.2x`). |
| **Collections (`List` / `ArrayList`)** | Ordered collections | [MovieServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/MovieServiceImpl.java)<br>[Booking.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/model/Booking.java) | `List<Movie>`, `List<Seat>`, `List<String> seatNumbers` for dynamic ordered sequences of data. |
| **Collections (`Set` / `HashSet`)** | Unique constraint validation | [BookingServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/BookingServiceImpl.java) | `Set<Long> uniqueSeatIds = new HashSet<>(request.getSeatIds());` verifies no duplicate seat IDs exist in a single customer booking request. |
| **Collections (`Map` / `HashMap` / `LinkedHashMap`)** | Grouping & Lookups | [SeatServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/SeatServiceImpl.java) | `Map<String, List<Seat>>` groups seats by their row letters ("A", "B", "C") preserving alphabetical insertion order with `LinkedHashMap`. |
| **Java Streams (`filter`)** | Searching and filtering | [MovieServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/MovieServiceImpl.java) | `allMovies.stream().filter(m -> m.getTitle().toLowerCase().contains(term)).collect(Collectors.toList());` filters movies dynamically without loops. |
| **Java Streams (`sorted`)** | Sorting movies | [MovieServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/MovieServiceImpl.java) | `.sorted(Comparator.comparing(Movie::getRating, Comparator.reverseOrder()))` sorts catalog by rating descending. |
| **Java Streams (`mapToDouble`)** | Price summation | [BookingServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/BookingServiceImpl.java) | `seats.stream().mapToDouble(seat -> seat.getSeatType().calculateSeatPrice(showPrice)).sum();` calculates ticket total. |
| **Java Streams (`groupingBy`)** | Partitioning seats into cinema rows | [SeatServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/SeatServiceImpl.java) | `seats.stream().collect(Collectors.groupingBy(s -> s.getSeatNumber().substring(0, 1), ...));` partitions seats into rows A..E. |
| **Lambda Expressions** | Functional parameters | Used throughout Service layer | `m -> m.getGenre() == genre`, `(rs, rowNum) -> new Movie(...)` concise functional expressions passed to Stream operators and RowMappers. |
| **Exception Handling (Custom)** | Business error scenarios | [MovieNotFoundException.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/exception/MovieNotFoundException.java)<br>[SeatAlreadyBookedException.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/exception/SeatAlreadyBookedException.java) | Specific, informative unchecked exceptions (`SeatAlreadyBookedException`, `ShowNotFoundException`, `InvalidBookingException`). |
| **Centralized Exception Handling** | `@RestControllerAdvice` | [GlobalExceptionHandler.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/exception/GlobalExceptionHandler.java) | Intercepts exceptions application-wide and maps them to HTTP status codes (`404 NOT FOUND`, `409 CONFLICT`, `400 BAD REQUEST`) in structured JSON. |
| **Multithreading & Concurrency** | Race condition prevention | [ConcurrencyBookingService.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/ConcurrencyBookingService.java)<br>[BookingServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/BookingServiceImpl.java) | `ExecutorService`, `CountDownLatch` fire two customer threads simultaneously for the same seat. Shared JVM lock and atomic MySQL UPDATE ensure exactly 1 succeeds and 1 is rejected. |
| **JDBC Connectivity** | Prepared Statements & Queries | [MovieRepositoryImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/repository/MovieRepositoryImpl.java)<br>[SeatRepositoryImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/repository/SeatRepositoryImpl.java) | Spring `JdbcTemplate` with parameterized placeholders (`?`) to prevent SQL injection, `GeneratedKeyHolder` for auto-generated IDs, and `RowMapper`. |
| **Transactions (`@Transactional`)** | ACID compliance in booking | [BookingServiceImpl.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/BookingServiceImpl.java) | The 11-step ticket booking workflow runs within a database transaction. If any step fails, all changes are rolled back automatically. |
| **REST APIs** | Client-server communication | [MovieController.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/controller/MovieController.java)<br>[BookingController.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/controller/BookingController.java) | Exposes standard REST endpoints (`GET`, `POST`, `PUT`, `DELETE`) returning JSON payloads with appropriate HTTP status codes (200, 201, 204, 400, 404, 409). |

---

## Detailed Viva Questions & Answers

### 1. How is OOP demonstrated in the project?
- **Classes & Objects**: We modeled cinema domain concepts as classes (`Movie`, `Theatre`, `Show`, `Seat`, `Customer`, `Booking`).
- **Encapsulation**: Private instance variables with public accessors (`getters`) and mutators (`setters`).
- **Inheritance**: `BaseEntity` contains common fields (`id`, `createdAt`). `Person` extends `BaseEntity`, and `Customer` extends `Person`.
- **Abstraction**: `BaseEntity` and `Person` are abstract classes that cannot be instantiated directly.
- **Polymorphism**: The `Customer` class overrides `getRoleDescription()`. Services are accessed via interfaces (`MovieService`).

### 2. Why did you use interfaces for Services and Repositories?
To achieve **loose coupling** and the **Dependency Inversion Principle** (SOLID). The controllers depend on `MovieService` (interface) rather than concrete `MovieServiceImpl`. If we ever change the database implementation or service logic, controllers remain untouched.

### 3. How do you handle concurrency when two users book the same seat?
We use a two-tier thread-safety strategy:
1. **JVM Synchronization**: A shared lock object in `BookingServiceImpl` synchronizes critical sections so that competing threads cannot simultaneously execute the read-and-write sequence.
2. **Atomic Database Update**: In MySQL, we execute:
   ```sql
   UPDATE seats SET status = 'BOOKED' WHERE seat_id IN (?) AND status = 'AVAILABLE';
   ```
   If another thread has already updated the row, the returned rows-affected count is `0`, causing the service to immediately throw a `SeatAlreadyBookedException` and roll back the transaction.

### 4. How did you test multithreading?
We built a dedicated simulation service in [ConcurrencyBookingService.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/service/ConcurrencyBookingService.java) that initializes a thread pool with `ExecutorService` and uses a `CountDownLatch` to fire two threads (Customer A and Customer B) at the exact same millisecond. The UI displays the race outcome, proving thread safety.

### 5. Where are Java Streams and Lambdas used?
- **Filtering**: Searching movie titles or descriptions, filtering by genre or language (`.filter(m -> ...)`).
- **Sorting**: Ranking movies by rating descending using `Comparator.comparing(Movie::getRating).reversed()`.
- **Aggregation**: Calculating total booking price across multiple regular and premium seats using `.mapToDouble(...).sum()`.
- **Grouping**: Grouping seat layouts into cinema rows ("A", "B", "C") using `Collectors.groupingBy()`.

### 6. How does your exception handling work?
We created custom domain exceptions extending `RuntimeException` (`MovieNotFoundException`, `SeatAlreadyBookedException`, etc.). The `@RestControllerAdvice` class [GlobalExceptionHandler.java](file:///d:/Movie%20Ticket%20Booking%20System/src/main/java/com/movieticketbooking/exception/GlobalExceptionHandler.java) intercepts these exceptions and formats them into a clean JSON response with HTTP status codes:
- 404: Not Found
- 409: Conflict (seat already booked)
- 400: Bad Request (validation errors)
- 500: Internal Server Error
