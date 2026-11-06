# 🎬 CinePass - Movie Ticket Booking System

A full-stack, production-grade **Movie Ticket Booking System** created as a Java mini-project. Designed to demonstrate practical **Core Java concepts**, clean layered architecture, and robust database persistence with MySQL.

---

## 🌟 Features

### 🍿 Customer Flow
- **Browse & Search Movies**: Real-time keyword search and genre filtering (Action, Sci-Fi, Drama, Comedy, Horror, Thriller) powered by Java Streams.
- **Movie Details**: View synopsis, duration, language, ratings, and active theatre schedules.
- **Interactive Cinema Seat Grid**: Cinema-style seat layout (Rows A–E, Seats 1–8) differentiating Regular and Premium Recliner seats.
- **Live Seat Availability**: Real-time status indicators (Available, Selected, Booked).
- **Customer Checkout**: Form validation (Name, Email, Phone) and real-time total price calculation.
- **Boarding Pass / Ticket Stub**: Generates a cinema boarding pass with QR code, unique Booking ID, seats list, and print/download functionality.
- **My Bookings & Cancellation**: View all past reservations with an option to cancel and release seats back to the cinema pool.

### ⚙️ Admin Management Portal
- **Dashboard Stats**: Real-time counts of Movies, Theatres, Shows, and Bookings.
- **Movie CRUD**: Add new movies, edit existing movies, and delete movies with poster previews.
- **Schedule Shows**: Create new show timings for any movie and theatre with custom ticket pricing. **Automatically generates all 40 cinema seats for each scheduled show**.
- **Theatre Registration**: Register partner cinema halls and auditoriums.
- **All Bookings View**: Complete audit table of all customer bookings with status and cancellation actions.

### ⚡ Multithreading Concurrency Demonstration
- **Interactive Collision Arena**: Visual simulation of two customers (Thread A and Thread B) attempting to book the exact same seat at the exact same millisecond.
- **Thread Safety**: Demonstrates synchronized JVM locking and atomic MySQL `UPDATE` operations, ensuring zero double-bookings.

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
| :--- | :--- | :--- |
| **Backend** | Java 17 | Core programming language |
| **Framework** | Spring Boot 3.2.x | REST API controllers, dependency injection |
| **Data Access** | Spring JDBC (`JdbcTemplate`) | Parameterized queries, prepared statements |
| **Database** | MySQL 8.0 | Relational database (`movie_ticket_booking`) |
| **Build Tool** | Apache Maven & Maven Wrapper (`mvnw`) | Dependency management & compilation |
| **Frontend** | Vanilla HTML5, CSS3, JavaScript (ES6+) | Modern cinema dark theme (no heavy frontend frameworks required) |

---

## 📁 Project Structure

```
Movie Ticket Booking System/
├── pom.xml                                   # Maven dependencies & build plugins
├── mvnw / mvnw.cmd                           # Maven Wrapper scripts
├── JAVA_CONCEPTS.md                          # Comprehensive Viva Preparation Guide
├── README.md                                 # Documentation & run instructions
│
├── database/
│   ├── schema.sql                            # DDL table creation scripts
│   └── data.sql                              # 8 movies, 4 theatres, 12 shows, 480 seats
│
└── src/
    └── main/
        ├── java/com/movieticketbooking/
        │   ├── MovieTicketBookingApplication.java
        │   ├── config/
        │   │   ├── CorsConfig.java           # Cross-Origin resource sharing
        │   │   └── DatabaseInitializer.java  # Auto-initialization on startup
        │   ├── controller/                   # REST API Endpoints
        │   │   ├── MovieController.java
        │   │   ├── TheatreController.java
        │   │   ├── ShowController.java
        │   │   ├── SeatController.java
        │   │   ├── BookingController.java
        │   │   └── ConcurrencyDemoController.java
        │   ├── dto/                          # Request/Response data transfer objects
        │   │   ├── BookingRequest.java
        │   │   ├── BookingResponse.java
        │   │   ├── ConcurrencyTestResult.java
        │   │   └── ErrorResponse.java
        │   ├── exception/                    # Custom exceptions & centralized handler
        │   │   ├── GlobalExceptionHandler.java
        │   │   ├── MovieNotFoundException.java
        │   │   ├── ShowNotFoundException.java
        │   │   ├── SeatAlreadyBookedException.java
        │   │   ├── BookingNotFoundException.java
        │   │   └── InvalidBookingException.java
        │   ├── model/                        # Domain entities & OOP hierarchy
        │   │   ├── BaseEntity.java           # Abstract base class
        │   │   ├── Person.java               # Abstract person class
        │   │   ├── Customer.java             # Customer entity extends Person
        │   │   ├── Movie.java
        │   │   ├── Theatre.java
        │   │   ├── Show.java
        │   │   ├── Seat.java
        │   │   ├── Booking.java
        │   │   ├── BookingSeat.java
        │   │   └── enums/
        │   │       ├── MovieGenre.java
        │   │       ├── SeatStatus.java
        │   │       ├── SeatType.java
        │   │       └── BookingStatus.java
        │   ├── repository/                   # Interfaces & JDBC implementations
        │   │   ├── MovieRepository.java & MovieRepositoryImpl.java
        │   │   ├── TheatreRepository.java & TheatreRepositoryImpl.java
        │   │   ├── ShowRepository.java & ShowRepositoryImpl.java
        │   │   ├── SeatRepository.java & SeatRepositoryImpl.java
        │   │   ├── CustomerRepository.java & CustomerRepositoryImpl.java
        │   │   └── BookingRepository.java & BookingRepositoryImpl.java
        │   └── service/                      # Business logic & concurrency
        │       ├── MovieService.java & MovieServiceImpl.java
        │       ├── TheatreService.java & TheatreServiceImpl.java
        │       ├── ShowService.java & ShowServiceImpl.java
        │       ├── SeatService.java & SeatServiceImpl.java
        │       ├── BookingService.java & BookingServiceImpl.java
        │       └── ConcurrencyBookingService.java
        │
        └── resources/
            ├── application.properties        # Server port & MySQL configuration
            ├── database/                     # Classpath schema & seed data
            │   ├── schema.sql
            │   └── data.sql
            └── static/                       # Web frontend assets
                ├── index.html
                ├── css/style.css
                └── js/
                    ├── api.js                # REST API client
                    └── app.js                # Frontend view state & logic
```

---

## 🗄️ Database Setup & Credentials

The application uses MySQL. Configuration is in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/movie_ticket_booking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

### Manual Database Setup (Optional):
If running MySQL on default port `3306`:
```sql
CREATE DATABASE movie_ticket_booking;
USE movie_ticket_booking;
SOURCE database/schema.sql;
SOURCE database/data.sql;
```

---

## 🚀 How to Run the Project

### Prerequisites:
- Java JDK 17+
- MySQL Server 8.0+

### Option 1: Using the Maven Wrapper (Recommended)
From the project root directory:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

### Option 2: Using Installed Maven
```bash
mvn clean compile
mvn spring-boot:run
```

Once the application starts, open your browser and navigate to:
```
http://localhost:8080
```

---

## 🌐 REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/movies` | Get all movies (supports `?genre=`, `?language=`, `?minRating=`) |
| `GET` | `/api/movies/{id}` | Get movie by ID |
| `GET` | `/api/movies/search` | Search movies by title keyword (`?query=Inception`) |
| `POST` | `/api/movies` | Add a new movie (Admin) |
| `PUT` | `/api/movies/{id}` | Update movie details (Admin) |
| `DELETE` | `/api/movies/{id}` | Delete a movie (Admin) |
| `GET` | `/api/theatres` | List all partner theatres |
| `POST` | `/api/theatres` | Register a new theatre (Admin) |
| `GET` | `/api/shows` | List all scheduled shows |
| `GET` | `/api/shows/{id}` | Get show details |
| `GET` | `/api/shows/movie/{movieId}` | Get all shows for a specific movie |
| `POST` | `/api/shows` | Schedule a new show (auto-generates 40 seats) |
| `DELETE` | `/api/shows/{id}` | Delete a scheduled show |
| `GET` | `/api/shows/{showId}/seats` | Get all seats for a show |
| `GET` | `/api/shows/{showId}/seats/grouped` | Get seats grouped by row letter ("A".."E") |
| `GET` | `/api/shows/{showId}/seats/available` | Get only available seats for a show |
| `POST` | `/api/bookings` | Book tickets (11-step transaction workflow) |
| `GET` | `/api/bookings/{id}` | Get booking details by ID |
| `GET` | `/api/bookings` | List all bookings |
| `DELETE` | `/api/bookings/{id}` | Cancel booking and release seats back to available |
| `POST` | `/api/concurrency/test` | Trigger 2-thread simultaneous booking race condition test |

---

## 🎓 Core Java Concepts Demonstrated

This project is tailored specifically for academic evaluation. Full viva reference with code lines is in **[JAVA_CONCEPTS.md](file:///d:/Movie%20Ticket%20Booking%20System/JAVA_CONCEPTS.md)**:

1. **OOP Principles**: Encapsulation, Classes, Objects, Abstraction (`BaseEntity`, `Person`), Inheritance (`Customer` extends `Person` extends `BaseEntity`), Dynamic Polymorphism.
2. **Interfaces & Abstraction**: Complete separation of service/repository interfaces (`BookingService`) from implementations (`BookingServiceImpl`).
3. **Method Overloading & Overriding**: Overloaded `searchMovies()` and `Movie` constructors; `@Override toString()` and `getRoleDescription()`.
4. **Keywords `this` and `super`**: Constructor chaining and field disambiguation.
5. **Java Collections**:
   - `List` / `ArrayList`: Ordered listings of movies, shows, and seats.
   - `Set` / `HashSet`: Verification of unique seat IDs in booking requests.
   - `Map` / `HashMap` / `LinkedHashMap`: Cinema seat grouping by row letters ("A", "B", "C"...).
6. **Enums**: `MovieGenre`, `SeatStatus` (`AVAILABLE`, `BOOKED`), `SeatType` (`REGULAR`, `PREMIUM` with 1.2x price calculation), `BookingStatus`.
7. **Java Streams & Lambdas**: `.filter()`, `.sorted()`, `.mapToDouble()`, `.collect()`, and `Collectors.groupingBy()`.
8. **Exception Handling**: Custom unchecked exceptions (`MovieNotFoundException`, `SeatAlreadyBookedException`, `InvalidBookingException`) with centralized `@RestControllerAdvice` error responses.
9. **Multithreading & Synchronization**: Race-condition prevention with thread-safe JVM synchronization, `ExecutorService`, `CountDownLatch`, and atomic SQL row updates.
10. **Database Connectivity**: Parameterized SQL statements using Spring JDBC `JdbcTemplate` preventing SQL injection, auto-generated key holders, and `@Transactional` rollbacks.

## Explicit Java Servlet implementation (Phase 4)

The project now includes traditional Jakarta `HttpServlet` classes alongside the existing Spring MVC REST API:

- `LoginServlet` — `POST /servlet/auth/login` reads JSON credentials, delegates credential checks to `AuthService`, and creates an `HttpSession` on success. `GET /servlet/auth/login` redirects to the login page.
- `RegisterServlet` — `POST /servlet/auth/register` reads JSON registration details and delegates validation, duplicate checks and persistence to `AuthService`. `GET /servlet/auth/register` redirects to the signup page.
- `LogoutServlet` — `POST /servlet/auth/logout` invalidates the current session and expires the `JSESSIONID` cookie. `GET /servlet/auth/logout` logs out and redirects to the home page.
- `config/ServletConfig.java` registers the Servlet URL mappings and injects the existing service and JSON mapper.

The login and signup browser scripts now submit to the Servlet endpoints. The shared frontend logout method also calls the logout Servlet. Existing `/api/auth/*` controller routes are retained for compatibility, including email availability checks and session-profile lookup.

### Manual verification checklist

1. Start MySQL and verify `application.properties` / environment variables match the local database credentials.
2. Start the application with `mvn spring-boot:run` (or run `MovieTicketBookingApplication` from the IDE).
3. Open `http://localhost:8080/signup.html`; register a new customer and confirm the browser request is `POST /servlet/auth/register` with HTTP `201` on success.
4. Open `http://localhost:8080/login.html`; sign in and confirm `POST /servlet/auth/login` returns HTTP `200` and sets a `JSESSIONID` cookie.
5. Open `/dashboard` while signed in; it should render the JSP dashboard.
6. Log out and revisit `/dashboard`; it should redirect to the login page.
7. Verify invalid credentials return HTTP `401`, an unknown account returns HTTP `404`, and duplicate registration returns HTTP `409`.

Build verification note: the project could not be compiled in the editing environment because the Maven Wrapper could not download Maven from Maven Central. Run the checklist on the development machine after dependencies are available.
