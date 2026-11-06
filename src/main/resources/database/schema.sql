-- ==========================================================
-- Movie Ticket Booking System - Database Schema
-- ==========================================================

-- 1. Movies Table
CREATE TABLE IF NOT EXISTS movies (
    movie_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    language VARCHAR(50) NOT NULL,
    duration INT NOT NULL COMMENT 'Duration in minutes',
    description TEXT NOT NULL,
    release_date DATE NOT NULL,
    rating DECIMAL(3, 1) NOT NULL DEFAULT 0.0,
    poster_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Theatres Table
CREATE TABLE IF NOT EXISTS theatres (
    theatre_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    total_seats INT NOT NULL DEFAULT 40,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Shows Table
CREATE TABLE IF NOT EXISTS shows (
    show_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    theatre_id BIGINT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    ticket_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movies (movie_id) ON DELETE CASCADE,
    CONSTRAINT fk_show_theatre FOREIGN KEY (theatre_id) REFERENCES theatres (theatre_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Seats Table
CREATE TABLE IF NOT EXISTS seats (
    seat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_show FOREIGN KEY (show_id) REFERENCES shows (show_id) ON DELETE CASCADE,
    CONSTRAINT uq_show_seat UNIQUE (show_id, seat_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows (show_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Booking Seats Relationship Table
CREATE TABLE IF NOT EXISTS booking_seats (
    booking_seat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id) REFERENCES bookings (booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_bs_seat FOREIGN KEY (seat_id) REFERENCES seats (seat_id) ON DELETE CASCADE,
    CONSTRAINT uq_booking_seat UNIQUE (booking_id, seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
