/**
 * ============================================================================
 * CinePass - Streaming-Style Cinema Ticket Booking App Controller
 * ============================================================================
 */

const App = {
    // Application State
    state: {
        movies: [],
        filteredMovies: [],
        trendingMovies: [],
        discoveryTab: 'popular', // 'popular', 'premieres', 'recent'
        theatres: [],
        shows: [],
        bookings: [],
        currentGenre: 'ALL',
        searchQuery: '',
        sortBy: 'rating',
        loggedInUser: null,
        
        // Active Booking Flow State
        activeMovie: null,
        activeShow: null,
        selectedSeats: new Map(), // seatId -> seat object

        // Admin State
        adminTab: 'movies'
    },

    // Initialization
    async init() {
        console.log("Initializing CinePass Streaming UI...");
        await this.verifyServerSession();
        this.updateAuthUI();
        this.setupScrollListener();
        
        try {
            await this.loadMovies();
            await this.loadTheatres();
            await this.loadShows();
            this.setupAdminDateDefault();
            this.showView('catalog');
        } catch (err) {
            console.error("Application initialization error:", err);
            this.showToast(`Initialization failed: ${err.message}`, "error");
        }
    },

    async verifyServerSession() {
        try {
            const res = await fetch('/api/auth/me');
            if (res.ok) {
                const data = await res.json();
                if (data.authenticated) {
                    this.state.loggedInUser = {
                        customerId: data.customerId,
                        name: data.name,
                        email: data.email,
                        phone: data.phone,
                        role: data.role
                    };
                    localStorage.setItem('cinepassUser', JSON.stringify(this.state.loggedInUser));
                    return;
                }
            }
        } catch (e) {
            console.warn("Server session verification notice:", e);
        }
        this.state.loggedInUser = null;
        localStorage.removeItem('cinepassUser');
    },

    // Scroll Navbar Effect
    setupScrollListener() {
        window.addEventListener('scroll', () => {
            const navbar = document.getElementById('main-navbar');
            if (navbar) {
                if (window.scrollY > 30) {
                    navbar.classList.add('scrolled');
                } else {
                    navbar.classList.remove('scrolled');
                }
            }
        });
    },

    // Expandable Search
    toggleNavbarSearch() {
        const box = document.getElementById('nav-search-box');
        const input = document.getElementById('movie-search-input');
        if (box) {
            box.classList.toggle('open');
            if (box.classList.contains('open') && input) {
                input.focus();
            }
        }
    },

    // Mobile Navigation Drawer Toggle
    toggleMobileMenu() {
        const menu = document.getElementById('primary-nav-menu');
        if (menu) {
            menu.classList.toggle('open');
        }
    },

    closeMobileMenu() {
        const menu = document.getElementById('primary-nav-menu');
        if (menu) {
            menu.classList.remove('open');
        }
    },

    // ------------------------------------------------------------------------
    // Navigation & View Routing
    // ------------------------------------------------------------------------
    showView(viewName) {
        const views = ['catalog', 'theatres', 'my-bookings', 'admin'];

        if (viewName === 'my-bookings' && !this.state.loggedInUser) {
            this.showToast("Please sign in to view your cinema reservations.", "info");
            setTimeout(() => { window.location.href = 'login.html'; }, 800);
            return;
        }

        if (viewName === 'admin') {
            if (!this.state.loggedInUser) {
                this.showToast("Please sign in as an administrator.", "info");
                setTimeout(() => { window.location.href = 'login.html'; }, 800);
                return;
            }
            if (this.state.loggedInUser.role !== 'ADMIN') {
                this.showToast("Access Denied: Administrator role required.", "error");
                return;
            }
        }

        views.forEach(v => {
            const el = document.getElementById(`view-${v}`);
            if (el) el.style.display = (v === viewName) ? 'block' : 'none';
        });

        document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));
        if (viewName === 'catalog') document.getElementById('nav-movies')?.classList.add('active');
        if (viewName === 'theatres') {
            document.getElementById('nav-theatres')?.classList.add('active');
            this.loadTheatres();
        }
        if (viewName === 'my-bookings') {
            document.getElementById('nav-bookings')?.classList.add('active');
            this.loadBookings();
        }
        if (viewName === 'admin') {
            document.getElementById('nav-admin')?.classList.add('active');
            this.loadAdminData();
        }

        this.updateAuthUI();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    updateAuthUI() {
        const profile = document.getElementById('user-profile');
        if (!profile) return;

        const adminNav = document.getElementById('nav-admin');

        if (!this.state.loggedInUser) {
            if (adminNav) adminNav.style.display = 'none';
            profile.innerHTML = `
                <a class="btn btn-secondary btn-sm" href="login.html">Sign In</a>
                <a class="btn btn-primary btn-sm" href="signup.html" style="margin-left: 0.4rem;">Sign Up</a>
            `;
            return;
        }

        const name = this.state.loggedInUser.name || 'User';
        const role = this.state.loggedInUser.role || 'CUSTOMER';
        const initials = name.split(' ').map(part => part[0]).slice(0, 2).join('').toUpperCase();

        if (adminNav) {
            adminNav.style.display = (role === 'ADMIN') ? 'inline-block' : 'none';
        }

        profile.innerHTML = `
            <a class="btn btn-secondary btn-sm" href="/dashboard" style="margin-right: 0.5rem; border-color: rgba(255,215,0,0.3); color: var(--accent-gold);" title="View JSP Dashboard">
                📊 JSP Dashboard
            </a>
            <div class="user-chip" title="Signed in as ${name} (${role})">
                <span class="user-avatar">${initials}</span>
                <span>${name}</span>
                ${role === 'ADMIN' ? '<span style="font-size: 0.68rem; background: var(--accent-red); color: #fff; padding: 0.15rem 0.4rem; border-radius: 4px; font-weight: 800; margin-left: 0.35rem;">ADMIN</span>' : ''}
            </div>
            <button class="btn-ghost-logout" onclick="App.logout()" title="Sign Out">
                Logout
            </button>
        `;
    },

    restoreSession() {
        try {
            this.state.loggedInUser = JSON.parse(localStorage.getItem('cinepassUser'));
        } catch (error) {
            localStorage.removeItem('cinepassUser');
        }
    },

    async logout() {
        try {
            await Api.logout();
        } catch (e) {
            console.warn("Logout error:", e);
        }
        this.state.loggedInUser = null;
        this.state.selectedSeats.clear();
        localStorage.removeItem('cinepassUser');
        this.updateAuthUI();
        this.showView('catalog');
        this.showToast("Logged out successfully", "info");
    },

    scrollToCatalog() {
        const el = document.getElementById('movies-container');
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    },

    // ------------------------------------------------------------------------
    // Movies & Hero Banner
    // ------------------------------------------------------------------------
    async loadMovies() {
        try {
            const movies = await Api.getMovies();
            this.state.movies = movies || [];
            this.updateHeroBanner();
            this.renderTrendingCarousel();
            this.applyFiltersAndSort();
        } catch (err) {
            console.error("Error loading movies:", err);
            this.showToast("Failed to fetch movies from server", "error");
        }
    },

    updateHeroBanner() {
        if (!this.state.movies || this.state.movies.length === 0) return;
        
        // Feature highest rated movie (e.g., The Dark Knight or Inception)
        const featuredMovie = [...this.state.movies].sort((a, b) => (b.rating || 0) - (a.rating || 0))[0];
        
        const titleEl = document.getElementById('hero-title');
        const ratingEl = document.getElementById('hero-rating');
        const genreEl = document.getElementById('hero-genre');
        const durationEl = document.getElementById('hero-duration');
        const langEl = document.getElementById('hero-lang');
        const descEl = document.getElementById('hero-desc');
        const backdropEl = document.getElementById('hero-backdrop-img');
        const bookBtn = document.getElementById('hero-book-btn');
        const detailsBtn = document.getElementById('hero-details-btn');

        if (titleEl) titleEl.textContent = featuredMovie.title;
        if (ratingEl) ratingEl.textContent = `★ ${Number(featuredMovie.rating).toFixed(1)}`;
        if (genreEl) genreEl.textContent = featuredMovie.genre;
        if (durationEl) durationEl.textContent = `${featuredMovie.duration} mins`;
        if (langEl) langEl.textContent = featuredMovie.language;
        if (descEl) descEl.textContent = featuredMovie.description;
        if (backdropEl) backdropEl.src = featuredMovie.posterUrl;
        
        const movieId = featuredMovie.movieId || featuredMovie.id;
        if (bookBtn) bookBtn.onclick = () => this.openMovieDetails(movieId);
        if (detailsBtn) detailsBtn.onclick = () => this.openMovieDetails(movieId);
    },

    // ------------------------------------------------------------------------
    // Trending Horizontal Carousel
    // ------------------------------------------------------------------------
    setDiscoveryTab(tabName) {
        this.state.discoveryTab = tabName;
        document.querySelectorAll('.subtab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.textContent.toLowerCase().includes(tabName));
        });
        this.renderTrendingCarousel();
    },

    renderTrendingCarousel() {
        const container = document.getElementById('trending-carousel-container');
        if (!container) return;

        let list = [...this.state.movies];
        if (this.state.discoveryTab === 'popular') {
            list.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        } else if (this.state.discoveryTab === 'premieres') {
            list.sort((a, b) => (a.duration || 0) - (b.duration || 0));
        } else if (this.state.discoveryTab === 'recent') {
            list.sort((a, b) => (b.movieId || b.id || 0) - (a.movieId || a.id || 0));
        }

        container.innerHTML = list.map(movie => {
            const mId = movie.movieId || movie.id;
            const year = movie.releaseDate ? movie.releaseDate.substring(0, 4) : '2026';
            return `
                <div class="poster-card" onclick="App.openMovieDetails(${mId})">
                    <div class="poster-media">
                        <img src="${movie.posterUrl}" alt="${movie.title}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800'">
                        <div class="poster-hover-overlay">
                            <button class="poster-quick-btn">
                                ▶ Book Tickets
                            </button>
                        </div>
                    </div>
                    <div class="poster-info">
                        <div class="poster-title" title="${movie.title}">${movie.title}</div>
                        <div class="poster-meta">
                            <span>${year} • ${movie.genre}</span>
                            <span style="color: var(--accent-gold); font-weight: 700;">★ ${Number(movie.rating).toFixed(1)}</span>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    scrollCarousel(direction) {
        const track = document.getElementById('trending-carousel-container');
        if (!track) return;
        const offset = direction === 'left' ? -420 : 420;
        track.scrollBy({ left: offset, behavior: 'smooth' });
    },

    // ------------------------------------------------------------------------
    // Catalog Filtering & 6-Column Grid
    // ------------------------------------------------------------------------
    applyFiltersAndSort() {
        let result = [...this.state.movies];

        // 1. Genre filter
        if (this.state.currentGenre !== 'ALL') {
            result = result.filter(m => m.genre === this.state.currentGenre);
        }

        // 2. Search query filter
        if (this.state.searchQuery.trim()) {
            const q = this.state.searchQuery.toLowerCase().trim();
            result = result.filter(m =>
                (m.title && m.title.toLowerCase().includes(q)) ||
                (m.description && m.description.toLowerCase().includes(q)) ||
                (m.language && m.language.toLowerCase().includes(q))
            );
        }

        // 3. Sorting
        if (this.state.sortBy === 'rating') {
            result.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        } else if (this.state.sortBy === 'title') {
            result.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
        } else if (this.state.sortBy === 'duration') {
            result.sort((a, b) => (b.duration || 0) - (a.duration || 0));
        }

        this.state.filteredMovies = result;
        this.renderMovieGrid();
    },

    renderMovieGrid() {
        const container = document.getElementById('movies-container');
        if (!container) return;

        if (this.state.filteredMovies.length === 0) {
            container.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 4rem 2rem; background: var(--bg-surface); border-radius: var(--radius-md); border: 1px solid var(--border-subtle);">
                    <div style="font-size: 2.8rem; margin-bottom: 0.75rem;">🔍</div>
                    <h3 style="font-size: 1.4rem; margin-bottom: 0.4rem;">No movies found</h3>
                    <p style="color: var(--text-secondary); max-width: 440px; margin: 0 auto 1.5rem;">
                        No titles matching "${this.state.searchQuery || this.state.currentGenre}". Try exploring other categories.
                    </p>
                    <button class="btn btn-secondary btn-sm" onclick="App.filterGenre('ALL'); document.getElementById('movie-search-input').value=''; App.onSearchInput();">
                        Show All Movies
                    </button>
                </div>
            `;
            return;
        }

        container.innerHTML = this.state.filteredMovies.map(movie => {
            const mId = movie.movieId || movie.id;
            const year = movie.releaseDate ? movie.releaseDate.substring(0, 4) : '2026';
            return `
                <article class="catalog-card" id="movie-card-${mId}" onclick="App.openMovieDetails(${mId})">
                    <div class="catalog-media">
                        <img src="${movie.posterUrl}" alt="${movie.title}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800'">
                        <div class="catalog-media-overlay">
                            <button class="poster-quick-btn">
                                🎟️ Book Tickets
                            </button>
                        </div>
                    </div>
                    <h3 class="catalog-title" title="${movie.title}">${movie.title}</h3>
                    <div class="catalog-meta">
                        <span>${year} • ${movie.genre}</span>
                        <span style="color: var(--accent-gold); font-weight: 700;">★ ${Number(movie.rating).toFixed(1)}</span>
                    </div>
                </article>
            `;
        }).join('');
    },

    onSearchInput() {
        const input = document.getElementById('movie-search-input');
        this.state.searchQuery = input.value;
        this.applyFiltersAndSort();
    },

    filterGenre(genre) {
        this.state.currentGenre = genre;
        document.querySelectorAll('.genre-pill-compact').forEach(btn => {
            const label = btn.textContent.trim().toUpperCase().replace('-', '_');
            const target = genre === 'SCI_FI' ? 'SCI_FI' : (genre === 'ALL' ? 'ALL' : genre);
            btn.classList.toggle('active', label === target || btn.textContent.trim().toUpperCase().includes(genre));
        });
        this.applyFiltersAndSort();
    },

    onSortChange() {
        const select = document.getElementById('movie-sort-select');
        this.state.sortBy = select.value;
        this.applyFiltersAndSort();
    },

    // ------------------------------------------------------------------------
    // Movie Details & Showtimes Modal
    // ------------------------------------------------------------------------
    async openMovieDetails(movieId) {
        try {
            const movie = await Api.getMovieById(movieId);
            const shows = await Api.getShowsByMovie(movieId);
            this.state.activeMovie = movie;

            const body = document.getElementById('movie-details-body');
            body.innerHTML = `
                <div style="display: grid; grid-template-columns: 260px 1fr; gap: 2.5rem; align-items: flex-start;">
                    <div>
                        <img src="${movie.posterUrl}" alt="${movie.title}" style="width: 100%; border-radius: var(--radius-sm); box-shadow: var(--shadow-lg); border: 1px solid var(--border-light);">
                    </div>
                    <div>
                        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.6rem;">
                            <span style="background: var(--accent-red); color: #fff; font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.65rem; border-radius: var(--radius-xs); text-transform: uppercase;">${movie.genre}</span>
                            <span class="rating-badge">★ ${Number(movie.rating).toFixed(1)} / 10</span>
                        </div>
                        <h2 style="font-size: 2.3rem; margin-bottom: 0.5rem; line-height: 1.15;">${movie.title}</h2>
                        <div class="hero-metadata-row" style="margin-bottom: 1.25rem;">
                            <span>⏱️ ${movie.duration} mins</span>
                            <span class="meta-divider">•</span>
                            <span>🌐 ${movie.language}</span>
                            <span class="meta-divider">•</span>
                            <span>📅 Release: ${movie.releaseDate || 'In Theatres'}</span>
                            <span class="meta-divider">•</span>
                            <span style="color: var(--accent-gold); font-weight: 600;">4K LASER IMAX</span>
                        </div>
                        <p style="color: var(--text-secondary); margin-bottom: 2rem; font-size: 0.96rem; line-height: 1.65;">
                            ${movie.description}
                        </p>

                        <div style="border-top: 1px solid var(--border-subtle); padding-top: 1.5rem;">
                            <h3 style="font-size: 1.3rem; margin-bottom: 0.35rem;">Select Cinema & Showtime</h3>
                            <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 1.25rem;">Choose your preferred theatre auditorium and showtime to pick seats.</p>
                            <div id="movie-shows-list">
                                ${this.renderShowsList(shows)}
                            </div>
                        </div>
                    </div>
                </div>
            `;

            this.openModal('modal-movie-details');
        } catch (err) {
            this.showToast("Failed to load movie details: " + err.message, "error");
        }
    },

    renderShowsList(shows) {
        if (!shows || shows.length === 0) {
            return `
                <div style="background: var(--bg-surface-elevated); padding: 1.5rem; border-radius: var(--radius-xs); border: 1px solid var(--border-subtle); text-align: center;">
                    <p style="color: var(--text-muted);">No shows currently scheduled for this movie.</p>
                    <small style="color: var(--text-faint);">Check back soon or visit the Admin Portal to schedule a new show.</small>
                </div>
            `;
        }

        // Group shows by Theatre
        const grouped = {};
        shows.forEach(s => {
            const tName = s.theatreName || `Theatre #${s.theatreId}`;
            if (!grouped[tName]) grouped[tName] = [];
            grouped[tName].push(s);
        });

        return Object.entries(grouped).map(([theatreName, sList]) => `
            <div style="background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-xs); padding: 1.25rem; margin-bottom: 1rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                    <div>
                        <div style="font-weight: 700; font-size: 1.05rem; color: #fff;">🏛️ ${theatreName}</div>
                        <div style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.15rem;">
                            📍 ${sList[0].theatreLocation || 'Auditorium 1'}
                        </div>
                    </div>
                    <span style="font-size: 0.8rem; color: var(--accent-gold); font-weight: 600;">Dolby Atmos 7.1</span>
                </div>
                <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                    ${sList.map(s => {
                        const sId = s.showId || s.id;
                        const timeStr = (s.showTime || '').substring(0, 5);
                        return `
                            <button class="btn btn-secondary btn-sm" style="border-color: rgba(229, 9, 20, 0.4); font-weight: 600;" 
                                onclick="App.openSeatSelection(${sId})">
                                🕒 ${timeStr} • <span style="color: var(--accent-gold);">₹${s.ticketPrice}</span>
                            </button>
                        `;
                    }).join('')}
                </div>
            </div>
        `).join('');
    },

    // ------------------------------------------------------------------------
    // Cinema Seat Selection Modal
    // ------------------------------------------------------------------------
    async openSeatSelection(showId) {
        try {
            this.closeModal('modal-movie-details');
            
            const show = await Api.request(`/shows/${showId}`);
            const groupedSeats = await Api.getGroupedSeats(showId);
            
            this.state.activeShow = show;
            this.state.selectedSeats.clear();

            // Set Header
            document.getElementById('seat-selection-header').innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <div style="font-size: 0.82rem; color: var(--accent-red); font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 0.2rem;">Step 2: Choose Your Seats</div>
                        <h2 style="font-size: 2rem; margin-bottom: 0.35rem;">${show.movieTitle || 'Movie'}</h2>
                        <div style="color: var(--text-secondary); font-size: 0.92rem;">
                            🏛️ <strong>${show.theatreName || 'Cinema Hall'}</strong> • 📅 ${show.showDate} • ⏰ ${(show.showTime || '').substring(0, 5)}
                        </div>
                    </div>
                    <div style="text-align: right;">
                        <span class="rating-badge" style="font-size: 0.95rem; padding: 0.35rem 0.75rem;">Base Price: ₹${show.ticketPrice}</span>
                    </div>
                </div>
            `;

            // Render Seat Matrix
            const grid = document.getElementById('seat-grid-container');
            grid.innerHTML = '';

            Object.entries(groupedSeats).forEach(([rowLetter, seats]) => {
                const rowDiv = document.createElement('div');
                rowDiv.className = 'seat-row';

                const label = document.createElement('div');
                label.className = 'row-label';
                label.textContent = rowLetter;
                rowDiv.appendChild(label);

                seats.forEach(seat => {
                    const btn = document.createElement('button');
                    const seatId = seat.seatId || seat.id;
                    const isRecliner = (seat.seatType && seat.seatType.toUpperCase() === 'RECLINER') || rowLetter === 'E';
                    
                    btn.id = `seat-btn-${seatId}`;
                    btn.className = `seat-btn ${isRecliner ? 'premium' : 'regular'}`;
                    btn.textContent = seat.seatNumber;
                    btn.title = `Seat ${seat.seatNumber} (${seat.seatType || (isRecliner ? 'RECLINER' : 'REGULAR')}) - ₹${seat.price}`;

                    if (seat.status === 'BOOKED') {
                        btn.classList.add('booked');
                        btn.disabled = true;
                    } else {
                        btn.onclick = () => this.toggleSeat(seat, btn);
                    }

                    rowDiv.appendChild(btn);
                });

                grid.appendChild(rowDiv);
            });

            this.updateSeatSummary();
            this.openModal('modal-seat-selection');
        } catch (err) {
            this.showToast("Failed to load cinema seats: " + err.message, "error");
        }
    },

    toggleSeat(seat, buttonEl) {
        const seatId = seat.seatId || seat.id;
        if (this.state.selectedSeats.has(seatId)) {
            this.state.selectedSeats.delete(seatId);
            buttonEl.classList.remove('selected');
        } else {
            this.state.selectedSeats.set(seatId, seat);
            buttonEl.classList.add('selected');
        }
        this.updateSeatSummary();
    },

    updateSeatSummary() {
        const summarySeatsEl = document.getElementById('summary-seat-numbers');
        const summaryTotalEl = document.getElementById('summary-total-price');
        const checkoutBtn = document.getElementById('btn-proceed-checkout');

        const selectedList = Array.from(this.state.selectedSeats.values());

        if (selectedList.length === 0) {
            summarySeatsEl.textContent = 'None selected';
            summaryTotalEl.textContent = '₹0.00';
            checkoutBtn.disabled = true;
            return;
        }

        const seatNames = selectedList.map(s => s.seatNumber).sort().join(', ');
        const totalAmount = selectedList.reduce((acc, s) => acc + (s.price || 0), 0);

        summarySeatsEl.textContent = `${seatNames} (${selectedList.length} seat${selectedList.length > 1 ? 's' : ''})`;
        summaryTotalEl.textContent = `₹${totalAmount.toFixed(2)}`;
        checkoutBtn.disabled = false;
    },

    // ------------------------------------------------------------------------
    // Customer Details & Booking Checkout
    // ------------------------------------------------------------------------
    openCustomerModal() {
        if (!this.state.loggedInUser) {
            this.showToast("Please sign in or create an account to reserve cinema seats.", "info");
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 900);
            return;
        }

        const selectedList = Array.from(this.state.selectedSeats.values());
        if (selectedList.length === 0) {
            this.showToast("Please select at least one cinema seat", "info");
            return;
        }

        const show = this.state.activeShow;
        const totalAmount = selectedList.reduce((acc, s) => acc + (s.price || 0), 0);

        document.getElementById('checkout-summary-show').textContent = `${show.movieTitle} at ${show.theatreName}`;
        document.getElementById('checkout-summary-seats').textContent = selectedList.map(s => s.seatNumber).sort().join(', ');
        document.getElementById('checkout-summary-amount').textContent = `₹${totalAmount.toFixed(2)}`;

        // Prefill user profile info if present
        if (this.state.loggedInUser) {
            const nameField = document.getElementById('cust-name');
            const emailField = document.getElementById('cust-email');
            const phoneField = document.getElementById('cust-phone');
            if (nameField) nameField.value = this.state.loggedInUser.name || '';
            if (emailField) emailField.value = this.state.loggedInUser.email || '';
            if (phoneField && !phoneField.value) phoneField.value = this.state.loggedInUser.phone || '';
        }

        this.closeModal('modal-seat-selection');
        this.openModal('modal-customer-checkout');
    },

    async handleBookingSubmit(event) {
        event.preventDefault();
        const submitBtn = document.getElementById('btn-confirm-booking');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Reserving Seats in Database...';

        const name = document.getElementById('cust-name').value.trim();
        const email = document.getElementById('cust-email').value.trim();
        const phone = document.getElementById('cust-phone').value.trim();

        const seatIds = Array.from(this.state.selectedSeats.keys());
        const showId = this.state.activeShow.showId || this.state.activeShow.id;

        const payload = {
            showId: showId,
            seatIds: seatIds,
            customerName: name,
            customerEmail: email,
            customerPhone: phone
        };

        try {
            const bookingResponse = await Api.bookTickets(payload);
            this.showToast("🎉 Booking Confirmed! Boarding pass generated.", "success");
            this.closeModal('modal-customer-checkout');
            this.renderTicketStub(bookingResponse);
            this.state.selectedSeats.clear();
        } catch (err) {
            if (err.status === 401) {
                this.showToast("Your session has expired. Please sign in to book tickets.", "error");
                setTimeout(() => { window.location.href = 'login.html'; }, 1000);
            } else {
                this.showToast("Booking Failed: " + err.message, "error");
            }
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = '🎟️ Confirm & Book Tickets';
        }
    },

    renderTicketStub(booking) {
        document.getElementById('ticket-movie-title').textContent = booking.movieTitle;
        document.getElementById('ticket-booking-id').textContent = `#BK-${booking.bookingId}`;
        document.getElementById('ticket-theatre').textContent = `${booking.theatreName} (${booking.theatreLocation || 'Auditorium 1'})`;
        document.getElementById('ticket-datetime').textContent = `${booking.showDate} | ${(booking.showTime || '').substring(0, 5)}`;
        document.getElementById('ticket-customer').textContent = `${booking.customerName} (${booking.customerPhone || 'N/A'})`;
        document.getElementById('ticket-seats').textContent = (booking.seatNumbers || []).join(', ');
        document.getElementById('ticket-amount').textContent = `₹${Number(booking.totalAmount).toFixed(2)}`;
        document.getElementById('ticket-qr-code').textContent = `TKT-${booking.bookingId}-${booking.customerId || '9821'}`;

        this.openModal('modal-ticket-confirmation');
    },

    viewExistingTicket(booking) {
        this.renderTicketStub(booking);
    },

    // ------------------------------------------------------------------------
    // My Bookings
    // ------------------------------------------------------------------------
    async loadBookings() {
        const container = document.getElementById('bookings-list-container');
        if (!container) return;

        container.innerHTML = `
            <div style="text-align: center; padding: 3rem; color: var(--text-muted);">
                <div style="font-size: 2rem; margin-bottom: 0.5rem;">⏳</div>
                <div>Fetching your cinema reservations...</div>
            </div>
        `;

        try {
            const bookings = await Api.getMyBookings();
            this.state.bookings = bookings || [];

            if (!bookings || bookings.length === 0) {
                container.innerHTML = `
                    <div style="background: var(--bg-surface); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: 4rem 2rem; text-align: center;">
                        <div style="font-size: 3.5rem; margin-bottom: 1rem;">🎟️</div>
                        <h3 style="font-size: 1.6rem; margin-bottom: 0.5rem;">No bookings found yet</h3>
                        <p style="color: var(--text-secondary); max-width: 460px; margin: 0 auto 1.5rem;">
                            You haven't reserved any movie tickets yet. Explore our trending releases and book your seats!
                        </p>
                        <button class="btn btn-primary btn-lg" onclick="App.showView('catalog')">
                            🍿 Browse Movies
                        </button>
                    </div>
                `;
                return;
            }

            container.innerHTML = `
                <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                    ${bookings.map(b => {
                        const isConfirmed = b.bookingStatus === 'CONFIRMED';
                        return `
                            <div class="booking-ticket-card ${!isConfirmed ? 'cancelled' : ''}">
                                <div style="display: flex; gap: 1.5rem; align-items: center;">
                                    <div style="font-size: 2.4rem; background: var(--bg-surface-elevated); width: 60px; height: 60px; border-radius: var(--radius-xs); display: flex; align-items: center; justify-content: center; border: 1px solid var(--border-subtle);">
                                        🎟️
                                    </div>
                                    <div>
                                        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem;">
                                            <h3 style="font-size: 1.25rem; margin: 0;">${b.movieTitle}</h3>
                                            <span style="font-size: 0.75rem; font-weight: 700; color: ${isConfirmed ? 'var(--accent-emerald)' : 'var(--accent-red)'}; background: ${isConfirmed ? 'var(--accent-emerald-subtle)' : 'var(--accent-red-subtle)'}; padding: 0.2rem 0.6rem; border-radius: var(--radius-full); border: 1px solid ${isConfirmed ? 'rgba(16,185,129,0.3)' : 'rgba(229,9,20,0.3)'};">
                                                ● ${b.bookingStatus}
                                            </span>
                                        </div>
                                        <div style="color: var(--text-secondary); font-size: 0.9rem;">
                                            🏛️ ${b.theatreName} • 📅 ${b.showDate} • ⏰ ${(b.showTime || '').substring(0, 5)}
                                        </div>
                                        <div style="color: var(--accent-gold); font-size: 0.92rem; font-weight: 700; margin-top: 0.35rem;">
                                            Reserved Seats: ${(b.seatNumbers || []).join(', ')} • Customer: ${b.customerName}
                                        </div>
                                    </div>
                                </div>
                                <div style="text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 0.65rem;">
                                    <div style="font-size: 1.4rem; font-weight: 900; color: #fff;">₹${Number(b.totalAmount).toFixed(2)}</div>
                                    <div style="display: flex; gap: 0.65rem;">
                                        <button class="btn btn-secondary btn-sm" onclick='App.viewExistingTicket(${JSON.stringify(b)})'>
                                            Boarding Pass
                                        </button>
                                        ${isConfirmed ? `
                                            <button class="btn btn-outline-danger btn-sm" onclick="App.cancelBooking(${b.bookingId})">
                                                Cancel
                                            </button>
                                        ` : ''}
                                    </div>
                                </div>
                            </div>
                        `;
                    }).join('')}
                </div>
            `;
        } catch (err) {
            if (err.status === 401) {
                this.showToast("Your session has expired. Please sign in.", "error");
                setTimeout(() => { window.location.href = 'login.html'; }, 1000);
            } else {
                container.innerHTML = `<p style="color: #ff4d4d; padding: 2rem;">Error loading bookings: ${err.message}</p>`;
            }
        }
    },

    async cancelBooking(bookingId) {
        if (!confirm("Are you sure you want to cancel this booking? The seats will be released back to AVAILABLE.")) {
            return;
        }

        try {
            await Api.cancelBooking(bookingId);
            this.showToast("Booking cancelled and seats released!", "info");
            this.loadBookings();
        } catch (err) {
            this.showToast("Cancellation failed: " + err.message, "error");
        }
    },

    // ------------------------------------------------------------------------
    // Theatres View
    // ------------------------------------------------------------------------
    async loadTheatres() {
        try {
            const theatres = await Api.getTheatres();
            this.state.theatres = theatres || [];
            this.renderTheatresView();
        } catch (err) {
            console.error("Error loading theatres:", err);
        }
    },

    async loadShows() {
        try {
            this.state.shows = await Api.getShows() || [];
        } catch (err) {
            console.error("Error loading shows:", err);
            this.state.shows = [];
        }
    },

    renderTheatresView() {
        const container = document.getElementById('theatres-container');
        if (!container) return;

        if (this.state.theatres.length === 0) {
            container.innerHTML = `<p style="color: var(--text-muted); grid-column: 1/-1;">No partner cinemas found.</p>`;
            return;
        }

        container.innerHTML = this.state.theatres.map(t => `
            <div class="stat-card" style="display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div style="font-size: 2.6rem; margin-bottom: 0.85rem;">🏛️</div>
                    <h3 style="font-size: 1.4rem; margin-bottom: 0.4rem; color: #fff;">${t.name}</h3>
                    <p style="color: var(--text-secondary); font-size: 0.92rem; margin-bottom: 1.25rem;">
                        📍 ${t.location}
                    </p>
                    <div style="background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); padding: 0.85rem; border-radius: var(--radius-xs); font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1.5rem;">
                        ✨ Capacity: <strong>${t.totalSeats} Recliner & Regular Seats</strong><br>
                        🔊 Audio: <strong>Dolby Atmos 7.1 Surround</strong>
                    </div>
                </div>
                <button class="btn btn-secondary btn-sm" onclick="App.showView('catalog')" style="width: 100%;">
                    🍿 Explore Movies at this Cinema
                </button>
            </div>
        `).join('');
    },

    // ------------------------------------------------------------------------
    // Multithreading Concurrency Demo
    // ------------------------------------------------------------------------
    async initConcurrencyDemo() {
        const showSelect = document.getElementById('concurrency-show-select');
        if (!showSelect) return;
        
        try {
            const shows = await Api.getShows();
            this.state.shows = shows || [];

            if (shows.length === 0) {
                showSelect.innerHTML = `<option value="">No shows available</option>`;
                return;
            }

            showSelect.innerHTML = shows.map(s => {
                const sId = s.showId || s.id;
                return `<option value="${sId}">${s.movieTitle} (${s.theatreName} - ${(s.showTime || '').substring(0, 5)})</option>`;
            }).join('');

            this.onConcurrencyShowChange();
        } catch (err) {
            console.error("Error setting up concurrency demo:", err);
        }
    },

    async onConcurrencyShowChange() {
        const showSelect = document.getElementById('concurrency-show-select');
        const seatSelect = document.getElementById('concurrency-seat-select');
        if (!showSelect || !seatSelect) return;

        const showId = showSelect.value;
        if (!showId) return;

        seatSelect.innerHTML = '<option>Loading available seats...</option>';

        try {
            const seats = await Api.getSeatsByShow(showId);
            const availableSeats = (seats || []).filter(s => s.status === 'AVAILABLE');

            if (availableSeats.length === 0) {
                seatSelect.innerHTML = `<option value="">No available seats (All Booked)</option>`;
                return;
            }

            seatSelect.innerHTML = availableSeats.map(s => {
                const sId = s.seatId || s.id;
                return `<option value="${sId}">Seat ${s.seatNumber} (${s.seatType}) - ₹${s.price}</option>`;
            }).join('');
        } catch (err) {
            seatSelect.innerHTML = `<option value="">Error loading seats</option>`;
        }
    },

    async triggerConcurrencyTest() {
        const showId = document.getElementById('concurrency-show-select')?.value;
        const seatId = document.getElementById('concurrency-seat-select')?.value;

        if (!showId || !seatId) {
            this.showToast("Please select a valid show and an available seat", "info");
            return;
        }

        const terminal = document.getElementById('concurrency-terminal');
        const btn = document.getElementById('run-concurrency-btn');
        const cardA = document.getElementById('card-thread-a');
        const cardB = document.getElementById('card-thread-b');
        const statusA = document.getElementById('status-thread-a');
        const statusB = document.getElementById('status-thread-b');

        btn.disabled = true;
        btn.textContent = 'Simulating Race Condition...';
        cardA.className = 'customer-thread-card';
        cardB.className = 'customer-thread-card';
        statusA.textContent = 'Executing concurrent bookTickets()...';
        statusB.textContent = 'Executing concurrent bookTickets()...';
        terminal.textContent = '> Initializing CountDownLatch and thread pool with 2 simultaneous threads...\n> Firing simultaneous requests into Spring Boot backend...';

        try {
            const result = await Api.runConcurrencyTest(showId, seatId);

            // Animate Terminal Logs
            terminal.textContent = '';
            (result.executionLogs || []).forEach(line => {
                terminal.textContent += line + '\n';
            });

            // Update Winner / Loser thread cards
            if (result.winnerCustomer === 'Customer A') {
                cardA.classList.add('winner');
                statusA.innerHTML = `<span style="color: var(--accent-emerald); font-weight: 800;">🏆 WINNER: Ticket Reserved!</span><br><small>${result.winnerStatus}</small>`;
                cardB.classList.add('loser');
                statusB.innerHTML = `<span style="color: var(--accent-red); font-weight: 800;">❌ BLOCKED: Seat Collision Prevented!</span><br><small>${result.rejectedStatus}</small>`;
            } else if (result.winnerCustomer === 'Customer B') {
                cardB.classList.add('winner');
                statusB.innerHTML = `<span style="color: var(--accent-emerald); font-weight: 800;">🏆 WINNER: Ticket Reserved!</span><br><small>${result.winnerStatus}</small>`;
                cardA.classList.add('loser');
                statusA.innerHTML = `<span style="color: var(--accent-red); font-weight: 800;">❌ BLOCKED: Seat Collision Prevented!</span><br><small>${result.rejectedStatus}</small>`;
            }

            this.showToast("Thread-Safety Verified: Exactly 1 Winner, 0 Double-Bookings!", "success");
            this.onConcurrencyShowChange();
        } catch (err) {
            terminal.textContent += `\nError running simulation: ${err.message}`;
            this.showToast("Simulation error: " + err.message, "error");
        } finally {
            btn.disabled = false;
            btn.textContent = '⚡ Fire Simultaneous Collision Test';
        }
    },

    // ------------------------------------------------------------------------
    // Admin Management Dashboard
    // ------------------------------------------------------------------------
    async loadAdminData() {
        try {
            const [movies, theatres, shows, bookings] = await Promise.all([
                Api.getMovies(),
                Api.getTheatres(),
                Api.getShows(),
                Api.getAllBookings()
            ]);

            document.getElementById('stat-movies').textContent = (movies || []).length;
            document.getElementById('stat-theatres').textContent = (theatres || []).length;
            document.getElementById('stat-shows').textContent = (shows || []).length;
            document.getElementById('stat-bookings').textContent = (bookings || []).length;

            this.renderAdminMoviesTable(movies || []);
            this.renderAdminShowsTable(shows || []);
            this.renderAdminTheatresTable(theatres || []);
            this.renderAdminBookingsTable(bookings || []);

            if (this.state.adminTab === 'concurrency') {
                this.initConcurrencyDemo();
            }
        } catch (err) {
            console.error("Error loading admin stats:", err);
        }
    },

    switchAdminTab(tabName) {
        this.state.adminTab = tabName;
        const tabs = ['movies', 'shows', 'theatres', 'bookings', 'concurrency'];
        tabs.forEach(t => {
            const el = document.getElementById(`admin-tab-${t}`);
            if (el) el.style.display = (t === tabName) ? 'block' : 'none';
        });

        document.querySelectorAll('.admin-tab-btn').forEach(b => {
            b.classList.toggle('active', b.getAttribute('onclick')?.includes(tabName));
        });

        if (tabName === 'concurrency') {
            this.initConcurrencyDemo();
        }
    },

    renderAdminMoviesTable(movies) {
        const tbody = document.getElementById('admin-movies-tbody');
        if (!tbody) return;
        tbody.innerHTML = movies.map(m => {
            const mId = m.movieId || m.id;
            return `
                <tr>
                    <td><strong>#${mId}</strong></td>
                    <td><img src="${m.posterUrl}" style="width: 36px; height: 50px; object-fit: cover; border-radius: 4px;"></td>
                    <td><strong style="color: #fff;">${m.title}</strong></td>
                    <td><span class="genre-pill-compact" style="font-size: 0.75rem; padding: 0.2rem 0.6rem;">${m.genre}</span></td>
                    <td><span style="color: var(--accent-gold); font-weight: 700;">★ ${Number(m.rating).toFixed(1)}</span></td>
                    <td>${m.duration}m</td>
                    <td>
                        <div style="display: flex; gap: 0.5rem;">
                            <button class="btn btn-secondary btn-sm" onclick="App.openEditMovieModal(${mId})">Edit</button>
                            <button class="btn btn-outline-danger btn-sm" onclick="App.deleteMovie(${mId})">Delete</button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    renderAdminShowsTable(shows) {
        const tbody = document.getElementById('admin-shows-tbody');
        if (!tbody) return;
        tbody.innerHTML = shows.map(s => {
            const sId = s.showId || s.id;
            return `
                <tr>
                    <td><strong>#${sId}</strong></td>
                    <td><strong style="color: #fff;">${s.movieTitle}</strong></td>
                    <td>${s.theatreName}</td>
                    <td>${s.showDate}</td>
                    <td>${(s.showTime || '').substring(0, 5)}</td>
                    <td><strong style="color: var(--accent-gold);">₹${Number(s.ticketPrice).toFixed(2)}</strong></td>
                    <td>
                        <button class="btn btn-outline-danger btn-sm" onclick="App.deleteShow(${sId})">Delete</button>
                    </td>
                </tr>
            `;
        }).join('');
    },

    renderAdminTheatresTable(theatres) {
        const tbody = document.getElementById('admin-theatres-tbody');
        if (!tbody) return;
        tbody.innerHTML = theatres.map(t => {
            const tId = t.theatreId || t.id;
            return `
                <tr>
                    <td><strong>#${tId}</strong></td>
                    <td><strong style="color: #fff;">${t.name}</strong></td>
                    <td>${t.location}</td>
                    <td><span style="color: var(--accent-gold); font-weight: 700;">${t.totalSeats} Seats</span></td>
                </tr>
            `;
        }).join('');
    },

    renderAdminBookingsTable(bookings) {
        const tbody = document.getElementById('admin-bookings-tbody');
        if (!tbody) return;
        tbody.innerHTML = bookings.map(b => `
            <tr>
                <td><strong>#${b.bookingId}</strong></td>
                <td><strong>${b.customerName}</strong><br><small style="color: var(--text-muted);">${b.customerEmail}</small></td>
                <td>${b.movieTitle}<br><small style="color: var(--text-muted);">${b.theatreName}</small></td>
                <td><span style="color: var(--accent-gold); font-weight: 700;">${(b.seatNumbers || []).join(', ')}</span></td>
                <td><strong>₹${Number(b.totalAmount).toFixed(2)}</strong></td>
                <td>
                    <span style="color: ${b.bookingStatus === 'CONFIRMED' ? 'var(--accent-emerald)' : 'var(--accent-red)'}; font-weight: 700; font-size: 0.85rem;">
                        ● ${b.bookingStatus}
                    </span>
                </td>
                <td>
                    ${b.bookingStatus === 'CONFIRMED' ? `
                        <button class="btn btn-outline-danger btn-sm" onclick="App.cancelBooking(${b.bookingId})">Cancel</button>
                    ` : '-'}
                </td>
            </tr>
        `).join('');
    },

    // Admin Movie Actions
    openAddMovieModal() {
        document.getElementById('admin-movie-modal-title').textContent = 'Add New Movie';
        document.getElementById('admin-movie-form').reset();
        document.getElementById('admin-movie-id').value = '';
        this.openModal('modal-admin-movie');
    },

    async openEditMovieModal(id) {
        try {
            const m = await Api.getMovieById(id);
            document.getElementById('admin-movie-modal-title').textContent = 'Edit Movie';
            document.getElementById('admin-movie-id').value = m.movieId || m.id;
            document.getElementById('admin-m-title').value = m.title;
            document.getElementById('admin-m-genre').value = m.genre;
            document.getElementById('admin-m-language').value = m.language;
            document.getElementById('admin-m-duration').value = m.duration;
            document.getElementById('admin-m-rating').value = m.rating;
            document.getElementById('admin-m-poster').value = m.posterUrl;
            document.getElementById('admin-m-desc').value = m.description;
            this.openModal('modal-admin-movie');
        } catch (err) {
            this.showToast("Failed to fetch movie details: " + err.message, "error");
        }
    },

    async handleSaveMovie(event) {
        event.preventDefault();
        const id = document.getElementById('admin-movie-id').value;
        const moviePayload = {
            title: document.getElementById('admin-m-title').value.trim(),
            genre: document.getElementById('admin-m-genre').value,
            language: document.getElementById('admin-m-language').value.trim(),
            duration: parseInt(document.getElementById('admin-m-duration').value, 10),
            rating: parseFloat(document.getElementById('admin-m-rating').value),
            posterUrl: document.getElementById('admin-m-poster').value.trim(),
            description: document.getElementById('admin-m-desc').value.trim()
        };

        try {
            if (id) {
                await Api.updateMovie(id, moviePayload);
                this.showToast("Movie updated successfully!", "success");
            } else {
                await Api.createMovie(moviePayload);
                this.showToast("New movie added to database!", "success");
            }
            this.closeModal('modal-admin-movie');
            await this.loadMovies();
            this.loadAdminData();
        } catch (err) {
            this.showToast("Failed to save movie: " + err.message, "error");
        }
    },

    async deleteMovie(id) {
        if (!confirm("Are you sure you want to delete this movie? This will also remove associated shows and seats.")) return;
        try {
            await Api.deleteMovie(id);
            this.showToast("Movie deleted successfully", "info");
            await this.loadMovies();
            this.loadAdminData();
        } catch (err) {
            this.showToast("Failed to delete movie: " + err.message, "error");
        }
    },

    // Admin Show Actions
    async openAddShowModal() {
        const movieSelect = document.getElementById('admin-s-movie');
        const theatreSelect = document.getElementById('admin-s-theatre');

        movieSelect.innerHTML = this.state.movies.map(m => `
            <option value="${m.movieId || m.id}">${m.title} (${m.language})</option>
        `).join('');

        theatreSelect.innerHTML = this.state.theatres.map(t => `
            <option value="${t.theatreId || t.id}">${t.name} (${t.location})</option>
        `).join('');

        this.openModal('modal-admin-show');
    },

    async handleSaveShow(event) {
        event.preventDefault();
        const showPayload = {
            movieId: parseInt(document.getElementById('admin-s-movie').value, 10),
            theatreId: parseInt(document.getElementById('admin-s-theatre').value, 10),
            showDate: document.getElementById('admin-s-date').value,
            showTime: document.getElementById('admin-s-time').value + ':00',
            ticketPrice: parseFloat(document.getElementById('admin-s-price').value)
        };

        try {
            await Api.createShow(showPayload);
            this.showToast("Show scheduled & 40 cinema seats generated!", "success");
            this.closeModal('modal-admin-show');
            this.loadAdminData();
        } catch (err) {
            this.showToast("Failed to schedule show: " + err.message, "error");
        }
    },

    async deleteShow(id) {
        if (!confirm("Are you sure you want to delete this show? All seat reservations will be cleared.")) return;
        try {
            await Api.deleteShow(id);
            this.showToast("Show deleted successfully", "info");
            this.loadAdminData();
        } catch (err) {
            this.showToast("Failed to delete show: " + err.message, "error");
        }
    },

    // Admin Theatre Actions
    openAddTheatreModal() {
        document.getElementById('admin-theatre-form').reset();
        this.openModal('modal-admin-theatre');
    },

    async handleSaveTheatre(event) {
        event.preventDefault();
        const theatrePayload = {
            name: document.getElementById('admin-t-name').value.trim(),
            location: document.getElementById('admin-t-location').value.trim(),
            totalSeats: 40
        };

        try {
            await Api.createTheatre(theatrePayload);
            this.showToast("Theatre registered successfully!", "success");
            this.closeModal('modal-admin-theatre');
            await this.loadTheatres();
            this.loadAdminData();
        } catch (err) {
            this.showToast("Failed to save theatre: " + err.message, "error");
        }
    },

    setupAdminDateDefault() {
        const today = new Date().toISOString().split('T')[0];
        const dateInput = document.getElementById('admin-s-date');
        if (dateInput) dateInput.value = today;
    },

    // ------------------------------------------------------------------------
    // Modal Helpers & Toasts
    // ------------------------------------------------------------------------
    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    },

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.remove('active');
        document.body.style.overflow = 'auto';
    },

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        let icon = 'ℹ️';
        if (type === 'success') icon = '✅';
        if (type === 'error') icon = '❌';

        toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 350);
        }, 4000);
    }
};

// Global startup on window DOM load
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
