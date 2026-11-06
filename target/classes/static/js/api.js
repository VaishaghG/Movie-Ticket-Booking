
/**
 * ============================================================================
 * Movie Ticket Booking System - REST API Client Wrapper
 * Handles AJAX requests to Spring Boot backend.
 * ============================================================================
 */

const API_BASE_URL = '/api';

const Api = {
    async request(endpoint, options = {}) {
        const config = {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        };

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            
            // Handle 204 No Content
            if (response.status === 204) {
                return null;
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMessage = (data && data.message) ? data.message : `HTTP Error ${response.status}`;
                const err = new Error(errorMessage);
                err.status = response.status;
                err.data = data;
                throw err;
            }

            return data;
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    },

    // Movies
    getMovies(params = {}) {
        const query = new URLSearchParams(params).toString();
        return this.request(`/movies${query ? '?' + query : ''}`);
    },

    searchMovies(query, genre) {
        const params = new URLSearchParams();
        if (query) params.append('query', query);
        if (genre && genre !== 'ALL') params.append('genre', genre);
        return this.request(`/movies/search?${params.toString()}`);
    },

    getMovieById(id) {
        return this.request(`/movies/${id}`);
    },

    createMovie(movie) {
        return this.request('/movies', {
            method: 'POST',
            body: JSON.stringify(movie)
        });
    },

    updateMovie(id, movie) {
        return this.request(`/movies/${id}`, {
            method: 'PUT',
            body: JSON.stringify(movie)
        });
    },

    deleteMovie(id) {
        return this.request(`/movies/${id}`, { method: 'DELETE' });
    },

    // Theatres
    getTheatres() {
        return this.request('/theatres');
    },

    createTheatre(theatre) {
        return this.request('/theatres', {
            method: 'POST',
            body: JSON.stringify(theatre)
        });
    },

    // Shows
    getShows() {
        return this.request('/shows');
    },

    getShowsByMovie(movieId) {
        return this.request(`/shows/movie/${movieId}`);
    },

    createShow(show) {
        return this.request('/shows', {
            method: 'POST',
            body: JSON.stringify(show)
        });
    },

    deleteShow(id) {
        return this.request(`/shows/${id}`, { method: 'DELETE' });
    },

    // Seats
    getSeatsByShow(showId) {
        return this.request(`/shows/${showId}/seats`);
    },

    getGroupedSeats(showId) {
        return this.request(`/shows/${showId}/seats/grouped`);
    },

    // Bookings
    bookTickets(payload) {
        return this.request('/bookings', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
    },

    getBookingById(id) {
        return this.request(`/bookings/${id}`);
    },

    getMyBookings() {
        return this.request('/bookings/my');
    },

    getAllBookings() {
        return this.request('/bookings');
    },

    cancelBooking(id) {
        return this.request(`/bookings/${id}`, { method: 'DELETE' });
    },

    // Auth & Session
    getSessionProfile() {
        return this.request('/auth/me');
    },

    async logout() {
        const response = await fetch('/servlet/auth/logout', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json' }
        });
        const data = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(data.message || `HTTP Error ${response.status}`);
        return data;
    },

    // Concurrency Demo
    runConcurrencyTest(showId, seatId) {
        return this.request(`/concurrency/test?showId=${showId}&seatId=${seatId}`, {
            method: 'POST'
        });
    }
};
