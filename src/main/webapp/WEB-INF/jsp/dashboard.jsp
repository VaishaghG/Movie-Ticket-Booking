<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CinePass — Customer Dashboard (JSP)</title>
    <meta name="description" content="Server-rendered JSP user dashboard for CinePass Movie Ticket Booking System demonstrating Spring MVC and HttpSession tracking.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Outfit:wght@500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/style.css?v=cinema_v2">
    <style>
        .dashboard-hero {
            background: linear-gradient(135deg, rgba(229, 9, 20, 0.15) 0%, rgba(11, 13, 20, 0.95) 100%),
                        radial-gradient(circle at top right, rgba(255, 215, 0, 0.08) 0%, transparent 60%);
            border-bottom: 1px solid var(--border-subtle);
            padding: 3.5rem 0 2.5rem;
            margin-bottom: 2.5rem;
        }
        .dashboard-header-flex {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 1.5rem;
        }
        .user-profile-summary {
            display: flex;
            align-items: center;
            gap: 1.25rem;
        }
        .profile-big-avatar {
            width: 72px;
            height: 72px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--accent-red), #990000);
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Outfit', sans-serif;
            font-size: 1.75rem;
            font-weight: 800;
            color: #fff;
            box-shadow: 0 8px 24px rgba(229, 9, 20, 0.35);
            border: 2px solid rgba(255, 255, 255, 0.15);
        }
        .role-pill {
            display: inline-block;
            font-size: 0.72rem;
            font-weight: 800;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            padding: 0.2rem 0.6rem;
            border-radius: 9999px;
            background: rgba(255, 215, 0, 0.15);
            color: var(--accent-gold);
            border: 1px solid rgba(255, 215, 0, 0.3);
            margin-left: 0.5rem;
        }
        .stats-grid-dashboard {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 1.25rem;
            margin-bottom: 2.5rem;
        }
        .stat-card-dash {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-sm);
            padding: 1.5rem;
            transition: transform 0.2s ease, border-color 0.2s ease;
        }
        .stat-card-dash:hover {
            transform: translateY(-2px);
            border-color: var(--border-light);
        }
        .stat-card-dash .stat-label {
            font-size: 0.82rem;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 0.4rem;
        }
        .stat-card-dash .stat-value {
            font-family: 'Outfit', sans-serif;
            font-size: 2rem;
            font-weight: 800;
            color: #fff;
        }
        .table-responsive-dash {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-sm);
            overflow-x: auto;
        }
        .table-dash {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }
        .table-dash th {
            background: var(--bg-surface-elevated);
            padding: 1rem 1.25rem;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: var(--text-secondary);
            border-bottom: 1px solid var(--border-subtle);
        }
        .table-dash td {
            padding: 1.15rem 1.25rem;
            font-size: 0.92rem;
            border-bottom: 1px solid var(--border-subtle);
            vertical-align: middle;
        }
        .table-dash tr:last-child td {
            border-bottom: none;
        }
        .table-dash tr:hover td {
            background: rgba(255, 255, 255, 0.02);
        }
        .badge-confirmed {
            color: var(--accent-emerald);
            background: var(--accent-emerald-subtle);
            border: 1px solid rgba(16, 185, 129, 0.3);
            font-size: 0.75rem;
            font-weight: 700;
            padding: 0.2rem 0.6rem;
            border-radius: 9999px;
            display: inline-block;
        }
        .badge-cancelled {
            color: var(--accent-red);
            background: var(--accent-red-subtle);
            border: 1px solid rgba(229, 9, 20, 0.3);
            font-size: 0.75rem;
            font-weight: 700;
            padding: 0.2rem 0.6rem;
            border-radius: 9999px;
            display: inline-block;
        }
        .syllabus-callout {
            background: rgba(56, 189, 248, 0.08);
            border: 1px solid rgba(56, 189, 248, 0.25);
            border-radius: var(--radius-sm);
            padding: 1rem 1.25rem;
            margin-bottom: 2rem;
            display: flex;
            align-items: center;
            gap: 1rem;
            font-size: 0.88rem;
            color: #93c5fd;
        }
    </style>
</head>
<body>

    <!-- Header Navigation -->
    <header class="navbar" style="position: static; background: rgba(11, 13, 20, 0.95); border-bottom: 1px solid var(--border-subtle);">
        <div class="container nav-container">
            <a href="/index.html" class="brand" style="text-decoration: none;">
                <span class="brand-icon">🎬</span>
                <span class="brand-text">CinePass</span>
            </a>

            <nav class="nav-center-menu">
                <ul class="nav-menu">
                    <li><a class="nav-link" href="/index.html">🍿 Browse Movies</a></li>
                    <li><a class="nav-link" href="/index.html#theatres">🏛️ Theatres</a></li>
                    <li><a class="nav-link active" href="/dashboard">📊 JSP Dashboard</a></li>
                </ul>
            </nav>

            <div class="nav-right-tools">
                <div class="user-profile">
                    <div class="user-chip" title="Signed in as <c:out value='${customerName}'/>">
                        <span class="user-avatar">
                            <c:out value="${customerName.substring(0, 1).toUpperCase()}"/>
                        </span>
                        <span><c:out value="${customerName}"/></span>
                    </div>
                    <button class="btn-ghost-logout" onclick="performLogout()" title="Sign Out">
                        Logout
                    </button>
                </div>
            </div>
        </div>
    </header>

    <!-- Dashboard Hero Banner -->
    <div class="dashboard-hero">
        <div class="container">
            <div class="dashboard-header-flex">
                <div class="user-profile-summary">
                    <div class="profile-big-avatar">
                        <c:out value="${customerName.substring(0, 1).toUpperCase()}"/>
                    </div>
                    <div>
                        <div style="font-size: 0.82rem; color: var(--accent-red); font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 0.2rem;">
                            Authenticated Member Portal
                        </div>
                        <h1 style="font-size: 2.2rem; margin-bottom: 0.25rem; display: flex; align-items: center;">
                            <c:out value="${customerName}"/>
                            <span class="role-pill"><c:out value="${customerRole}"/></span>
                        </h1>
                        <div style="color: var(--text-secondary); font-size: 0.92rem;">
                            📧 <c:out value="${customerEmail}"/> &nbsp;•&nbsp; 📱 <c:out value="${customerPhone}"/> &nbsp;•&nbsp; 🆔 Customer #<c:out value="${customerId}"/>
                        </div>
                    </div>
                </div>
                <div style="display: flex; gap: 0.75rem;">
                    <a href="/index.html" class="btn btn-primary btn-sm">
                        🍿 Book More Tickets
                    </a>
                    <button class="btn btn-secondary btn-sm" onclick="performLogout()">
                        Sign Out
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Main Content Container -->
    <main class="container" style="padding-bottom: 4rem;">

        <!-- Syllabus Requirements Notice -->
        <div class="syllabus-callout">
            <span style="font-size: 1.5rem;">🎓</span>
            <div>
                <strong>Java Mini Project Specification — JSP &amp; HttpSession Tracking:</strong>
                This page is server-rendered via <code>Spring MVC</code> and <code>Jakarta Tomcat Jasper</code> from <code>/WEB-INF/jsp/dashboard.jsp</code>.
                Session state is tracked on the server using <code>jakarta.servlet.http.HttpSession</code>.
            </div>
        </div>

        <!-- Quick Statistics Row -->
        <div class="stats-grid-dashboard">
            <div class="stat-card-dash">
                <div class="stat-label">Total Reservations</div>
                <div class="stat-value"><c:out value="${totalBookings}"/></div>
            </div>
            <div class="stat-card-dash">
                <div class="stat-label">Active Confirmed Passes</div>
                <div class="stat-value" style="color: var(--accent-emerald);"><c:out value="${confirmedBookings}"/></div>
            </div>
            <div class="stat-card-dash">
                <div class="stat-label">Total Cinema Spend</div>
                <div class="stat-value" style="color: var(--accent-gold);">₹<c:out value="${totalSpent}"/></div>
            </div>
        </div>

        <!-- Bookings Section -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
            <div>
                <h2 style="font-size: 1.4rem; margin: 0 0 0.25rem;">My Digital Boarding Passes</h2>
                <p style="color: var(--text-secondary); font-size: 0.88rem; margin: 0;">Detailed history of your reserved cinema seats and shows.</p>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty bookings}">
                <div style="background: var(--bg-surface); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: 3.5rem 2rem; text-align: center;">
                    <div style="font-size: 3rem; margin-bottom: 0.75rem;">🎟️</div>
                    <h3 style="font-size: 1.3rem; margin-bottom: 0.35rem;">No Bookings Recorded Yet</h3>
                    <p style="color: var(--text-secondary); max-width: 440px; margin: 0 auto 1.5rem; font-size: 0.92rem;">
                        You haven't reserved any cinema seats yet. Browse trending movies and book tickets with instant confirmation!
                    </p>
                    <a href="/index.html" class="btn btn-primary">
                        🍿 Explore Now Showing Movies
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive-dash">
                    <table class="table-dash">
                        <thead>
                            <tr>
                                <th>Booking ID</th>
                                <th>Movie</th>
                                <th>Cinema &amp; Auditorium</th>
                                <th>Showtime</th>
                                <th>Seats</th>
                                <th>Total</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="b" items="${bookings}">
                                <tr>
                                    <td><strong>#BK-<c:out value="${b.bookingId}"/></strong></td>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                                            <img src="<c:out value='${b.posterUrl}'/>" alt="Poster" style="width: 32px; height: 46px; object-fit: cover; border-radius: 4px;" onerror="this.src='https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800'">
                                            <span style="font-weight: 700; color: #fff;"><c:out value="${b.movieTitle}"/></span>
                                        </div>
                                    </td>
                                    <td>
                                        <c:out value="${b.theatreName}"/><br>
                                        <small style="color: var(--text-muted);"><c:out value="${b.theatreLocation}"/></small>
                                    </td>
                                    <td>
                                        <c:out value="${b.showDate}"/><br>
                                        <small style="color: var(--accent-gold); font-weight: 600;"><c:out value="${b.showTime}"/></small>
                                    </td>
                                    <td>
                                        <span style="color: var(--accent-gold); font-weight: 700;">
                                            <c:forEach var="seat" items="${b.seatNumbers}" varStatus="loop">
                                                <c:out value="${seat}"/><c:if test="${!loop.last}">, </c:if>
                                            </c:forEach>
                                        </span>
                                    </td>
                                    <td><strong style="font-size: 1.05rem;">₹<c:out value="${b.totalAmount}"/></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${b.bookingStatus == 'CONFIRMED'}">
                                                <span class="badge-confirmed">● CONFIRMED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-cancelled">● CANCELLED</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${b.bookingStatus == 'CONFIRMED'}">
                                            <button class="btn btn-outline-danger btn-sm" onclick="cancelUserBooking(${b.bookingId})">
                                                Cancel
                                            </button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

    </main>

    <script>
        async function performLogout() {
            try {
                const response = await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                });
                localStorage.removeItem('cinepassUser');
                window.location.href = '/login.html';
            } catch (err) {
                console.error("Logout error:", err);
                localStorage.removeItem('cinepassUser');
                window.location.href = '/login.html';
            }
        }

        async function cancelUserBooking(bookingId) {
            if (!confirm('Are you sure you want to cancel this booking? The reserved seats will be released back to AVAILABLE.')) {
                return;
            }

            try {
                const response = await fetch('/api/bookings/' + bookingId, {
                    method: 'DELETE',
                    headers: { 'Content-Type': 'application/json' }
                });

                if (response.ok || response.status === 204) {
                    alert('Booking cancelled successfully! Page will refresh.');
                    window.location.reload();
                } else {
                    const err = await response.json().catch(() => ({}));
                    alert('Failed to cancel booking: ' + (err.message || 'Unauthorized or server error'));
                }
            } catch (e) {
                alert('Error cancelling booking: ' + e.message);
            }
        }
    </script>
</body>
</html>
