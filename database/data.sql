-- ==========================================================
-- Movie Ticket Booking System - Sample Data
-- ==========================================================

USE movie_ticket_booking;

-- Disable foreign key checks for clean truncation/insertion
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM booking_seats;
DELETE FROM bookings;
DELETE FROM seats;
DELETE FROM shows;
DELETE FROM customers;
DELETE FROM theatres;
DELETE FROM movies;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert 8 Movies
INSERT INTO movies (movie_id, title, genre, language, duration, description, release_date, rating, poster_url) VALUES
(1, 'Interstellar', 'SCI_FI', 'English', 169, 'When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft, along with a team of researchers, to find a new planet for humans.', '2014-11-07', 8.7, 'https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=800&auto=format&fit=crop'),
(2, 'Inception', 'SCI_FI', 'English', 148, 'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project.', '2010-07-16', 8.8, 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop'),
(3, 'Avengers: Endgame', 'ACTION', 'English', 181, 'After the devastating events of Infinity War, the universe is in ruins. With the help of remaining allies, the Avengers assemble once more in order to reverse Thanos'' actions and restore balance.', '2019-04-26', 8.4, 'https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=800&auto=format&fit=crop'),
(4, 'The Dark Knight', 'ACTION', 'English', 152, 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.', '2008-07-18', 9.0, 'https://images.unsplash.com/photo-1509281373149-e957c6296406?q=80&w=800&auto=format&fit=crop'),
(5, 'Oppenheimer', 'DRAMA', 'English', 180, 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II, examining the moral implications and political fallout.', '2023-07-21', 8.9, 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=800&auto=format&fit=crop'),
(6, 'Dune: Part Two', 'SCI_FI', 'English', 166, 'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between love and the fate of the universe, he endeavors to prevent a terrible future.', '2024-03-01', 8.6, 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop'),
(7, 'Spider-Man: No Way Home', 'ACTION', 'English', 148, 'With Spider-Man''s identity now revealed, Peter asks Doctor Strange for help. When a spell goes wrong, dangerous foes from other worlds start to appear, forcing Peter to discover what it truly means to be Spider-Man.', '2021-12-17', 8.2, 'https://images.unsplash.com/photo-1604200213928-ba3cf4fc8436?q=80&w=800&auto=format&fit=crop'),
(8, 'Avatar: The Way of Water', 'SCI_FI', 'English', 192, 'Jake Sully lives with his newfound family formed on the extrasolar moon Pandora. Once a familiar threat returns to finish what was previously started, Jake must work with Neytiri and the army of the Na''vi race to protect their home.', '2022-12-16', 7.6, 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop');

-- 2. Insert 4 Theatres
INSERT INTO theatres (theatre_id, name, location, total_seats) VALUES
(1, 'CineMax Grand Multiplex', 'Downtown Avenue, Tech City', 40),
(2, 'IMAX Luxe Screens', 'Skyline Mall, West Bay', 40),
(3, 'PVR Heritage Cinemas', 'Central Square, Metro City', 40),
(4, 'Starplex 4DX Theatre', 'Outer Ring Road, Silicon Park', 40);

-- 3. Insert Shows (Dates from current onwards)
INSERT INTO shows (show_id, movie_id, theatre_id, show_date, show_time, ticket_price) VALUES
(1, 1, 1, CURRENT_DATE(), '10:00:00', 250.00),
(2, 1, 2, CURRENT_DATE(), '14:30:00', 350.00),
(3, 1, 3, CURRENT_DATE(), '19:00:00', 280.00),
(4, 2, 1, CURRENT_DATE(), '13:00:00', 220.00),
(5, 2, 4, CURRENT_DATE(), '18:15:00', 320.00),
(6, 3, 2, CURRENT_DATE(), '11:00:00', 300.00),
(7, 3, 3, CURRENT_DATE(), '16:00:00', 260.00),
(8, 4, 1, CURRENT_DATE(), '20:30:00', 240.00),
(9, 5, 2, CURRENT_DATE(), '17:45:00', 350.00),
(10, 6, 4, CURRENT_DATE(), '21:00:00', 320.00),
(11, 7, 3, CURRENT_DATE(), '12:30:00', 250.00),
(12, 8, 2, CURRENT_DATE(), '15:00:00', 380.00);

-- 4. Helper Procedure to populate 40 seats (A1-E8) for each show
DELIMITER $$
CREATE PROCEDURE PopulateSeatsForShows()
BEGIN
    DECLARE s_id BIGINT;
    DECLARE done INT DEFAULT FALSE;
    DECLARE cur CURSOR FOR SELECT show_id FROM shows;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO s_id;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Regular Seats (Rows A, B, C: 1 to 8)
        INSERT INTO seats (show_id, seat_number, seat_type, status) VALUES
        (s_id, 'A1', 'REGULAR', 'AVAILABLE'), (s_id, 'A2', 'REGULAR', 'AVAILABLE'),
        (s_id, 'A3', 'REGULAR', 'AVAILABLE'), (s_id, 'A4', 'REGULAR', 'AVAILABLE'),
        (s_id, 'A5', 'REGULAR', 'AVAILABLE'), (s_id, 'A6', 'REGULAR', 'AVAILABLE'),
        (s_id, 'A7', 'REGULAR', 'AVAILABLE'), (s_id, 'A8', 'REGULAR', 'AVAILABLE'),
        (s_id, 'B1', 'REGULAR', 'AVAILABLE'), (s_id, 'B2', 'REGULAR', 'AVAILABLE'),
        (s_id, 'B3', 'REGULAR', 'AVAILABLE'), (s_id, 'B4', 'REGULAR', 'AVAILABLE'),
        (s_id, 'B5', 'REGULAR', 'AVAILABLE'), (s_id, 'B6', 'REGULAR', 'AVAILABLE'),
        (s_id, 'B7', 'REGULAR', 'AVAILABLE'), (s_id, 'B8', 'REGULAR', 'AVAILABLE'),
        (s_id, 'C1', 'REGULAR', 'AVAILABLE'), (s_id, 'C2', 'REGULAR', 'AVAILABLE'),
        (s_id, 'C3', 'REGULAR', 'AVAILABLE'), (s_id, 'C4', 'REGULAR', 'AVAILABLE'),
        (s_id, 'C5', 'REGULAR', 'AVAILABLE'), (s_id, 'C6', 'REGULAR', 'AVAILABLE'),
        (s_id, 'C7', 'REGULAR', 'AVAILABLE'), (s_id, 'C8', 'REGULAR', 'AVAILABLE'),

        -- Premium Seats (Rows D, E: 1 to 8)
        (s_id, 'D1', 'PREMIUM', 'AVAILABLE'), (s_id, 'D2', 'PREMIUM', 'AVAILABLE'),
        (s_id, 'D3', 'PREMIUM', 'AVAILABLE'), (s_id, 'D4', 'PREMIUM', 'AVAILABLE'),
        (s_id, 'D5', 'PREMIUM', 'AVAILABLE'), (s_id, 'D6', 'PREMIUM', 'AVAILABLE'),
        (s_id, 'D7', 'PREMIUM', 'AVAILABLE'), (s_id, 'D8', 'PREMIUM', 'AVAILABLE'),
        (s_id, 'E1', 'PREMIUM', 'AVAILABLE'), (s_id, 'E2', 'PREMIUM', 'AVAILABLE'),
        (s_id, 'E3', 'PREMIUM', 'AVAILABLE'), (s_id, 'E4', 'PREMIUM', 'AVAILABLE'),
        (s_id, 'E5', 'PREMIUM', 'AVAILABLE'), (s_id, 'E6', 'PREMIUM', 'AVAILABLE'),
        (s_id, 'E7', 'PREMIUM', 'AVAILABLE'), (s_id, 'E8', 'PREMIUM', 'AVAILABLE');

    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL PopulateSeatsForShows();
DROP PROCEDURE IF EXISTS PopulateSeatsForShows;

-- 5. Insert Sample Customers
INSERT INTO customers (customer_id, name, email, phone, password) VALUES
(1, 'Alex Mercer', 'alex.mercer@example.com', '9876543210', 'password123'),
(2, 'Sarah Connor', 'sarah.connor@example.com', '9876543211', 'password123'),
(3, 'Bruce Wayne', 'bruce.wayne@example.com', '9876543212', 'password123');

-- 6. Insert Sample Bookings and mark seats as BOOKED
-- Booking 1: Customer 1 books Show 1, Seats A3 and A4
INSERT INTO bookings (booking_id, customer_id, show_id, booking_date, total_amount, booking_status) VALUES
(1, 1, 1, NOW(), 500.00, 'CONFIRMED');

UPDATE seats SET status = 'BOOKED' WHERE show_id = 1 AND seat_number IN ('A3', 'A4');

INSERT INTO booking_seats (booking_id, seat_id)
SELECT 1, seat_id FROM seats WHERE show_id = 1 AND seat_number IN ('A3', 'A4');

-- Booking 2: Customer 2 books Show 2, Premium Seats D4 and D5
INSERT INTO bookings (booking_id, customer_id, show_id, booking_date, total_amount, booking_status) VALUES
(2, 2, 2, NOW(), 840.00, 'CONFIRMED');

UPDATE seats SET status = 'BOOKED' WHERE show_id = 2 AND seat_number IN ('D4', 'D5');

INSERT INTO booking_seats (booking_id, seat_id)
SELECT 2, seat_id FROM seats WHERE show_id = 2 AND seat_number IN ('D4', 'D5');
