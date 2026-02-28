const API_BASE = "http://localhost:8080/api";
let adminMode = false;
let lastSeenEventId = null;

// Search events using specifications
async function searchEvents(page = 0) {
    const name = document.getElementById("searchName").value;
    const location = document.getElementById("searchLocation").value;
    const start = performance.now();

    let url = `${API_BASE}/events/search?page=${page}&size=5`;
    if (name) url += `&name=${name}`;
    if (location) url += `&location=${location}`;

    try {
        const res = await fetch(url);
        const data = await res.json();
        const end = performance.now();

        document.getElementById("queryTime").innerText = `${(end - start).toFixed(1)}ms`;
        document.getElementById("queryType").innerText = "Standard Pageable OFFSET";

        const tbody = document.querySelector("#eventTable tbody");
        tbody.innerHTML = "";
        data.content.forEach(event => {
            tbody.innerHTML += `
                <tr>
                    <td>${event.id}</td>
                    <td>${event.name}</td>
                    <td>${event.location}</td>
                    <td>${new Date(event.eventDate).toLocaleString()}</td>
                </tr>
            `;
        });
        if (data.content.length > 0) {
            lastSeenEventId = data.content[data.content.length - 1].id;
        }
    } catch (e) {
        console.error(e);
    }
}

// Search events using keyset pagination (seek method)
async function searchEventsKeyset() {
    const start = performance.now();
    let url = `${API_BASE}/events/search/keyset?size=5`;
    if (lastSeenEventId) url += `&lastId=${lastSeenEventId}`;

    try {
        const res = await fetch(url);
        const content = await res.json();
        const end = performance.now();

        document.getElementById("queryTime").innerText = `${(end - start).toFixed(1)}ms`;
        document.getElementById("queryType").innerText = "Keyset Range Seek (O(1))";

        const tbody = document.querySelector("#eventTable tbody");
        tbody.innerHTML = "";
        content.forEach(event => {
            tbody.innerHTML += `
                <tr>
                    <td>${event.id}</td>
                    <td>${event.name}</td>
                    <td>${event.location}</td>
                    <td>${new Date(event.eventDate).toLocaleString()}</td>
                </tr>
            `;
        });
        if (content.length > 0) {
            lastSeenEventId = content[content.length - 1].id;
        } else {
            lastSeenEventId = null; // Wrap back to page 1
        }
    } catch (e) {
        console.error(e);
    }
}

// Render Seat grid inventory
function renderSeatGrid(availableCount) {
    const grid = document.getElementById("seatGrid");
    grid.innerHTML = "";
    const total = 50;
    for (let i = 1; i <= total; i++) {
        const isReserved = i > availableCount;
        grid.innerHTML += `
            <div class="seat-box ${isReserved ? "reserved" : "available"}">
                ${i}
            </div>
        `;
    }
}

// Runs concurrent stress test bookings
async function runConcurrencySimulation() {
    const logs = document.getElementById("simLogs");
    logs.innerHTML = "<div class='log-entry'>Firing 10 concurrent requests to reserve seats...</div>";
    
    // Seed 10 concurrent fetch calls (simulating 10 users booking 1 ticket each simultaneously)
    const promises = [];
    for (let i = 1; i <= 10; i++) {
        const url = `${API_BASE}/bookings?userId=${i}&eventId=1&quantity=1`;
        promises.push(
            fetch(url, { method: "POST" })
                .then(async res => {
                    const status = res.status;
                    if (status === 200) {
                        const booking = await res.json();
                        return { success: true, msg: `User-${i} successfully booked Ticket ID: ${booking.id}` };
                    } else {
                        const txt = await res.text();
                        return { success: false, msg: `User-${i} failed: Stock exhausted or locked` };
                    }
                })
        );
    }

    const results = await Promise.all(promises);
    results.forEach(res => {
        const cl = res.success ? "success" : "fail";
        logs.innerHTML += `<div class="log-entry ${cl}">${res.msg}</div>`;
    });

    // Refresh visual database elements
    await loadBookings();
    await updateSeatInventoryInfo();
}

// Fetch seats info
async function updateSeatInventoryInfo() {
    try {
        const res = await fetch(`${API_BASE}/events/search?name=Taylor`);
        const data = await res.json();
        if (data.content.length > 0) {
            // Find inventory available seats
            const eventId = data.content[0].id;
            // Seed grid
            const itemsRes = await fetch(`${API_BASE}/bookings/fetch-join`);
            const bookings = await itemsRes.json();
            const bookedCount = bookings.reduce((sum, b) => sum + b.tickets.length, 0);
            renderSeatGrid(50 - bookedCount);
        }
    } catch (e) {
        console.error(e);
    }
}

// Fetch bookings list
async function loadBookings() {
    const url = adminMode ? `${API_BASE}/bookings/admin/all` : `${API_BASE}/bookings/fetch-join`;
    try {
        const res = await fetch(url);
        const data = await res.json();
        const tbody = document.querySelector("#bookingTable tbody");
        tbody.innerHTML = "";
        data.forEach(b => {
            const hasDelete = b.status !== "CANCELLED";
            const deleteBtn = hasDelete ? 
                `<button class="btn btn-secondary" style="padding:4px 8px;font-size:0.75rem;" onclick="cancelBooking(${b.id})">Cancel</button>` : 
                `<span style="color:var(--accent-red)">Archived</span>`;
                
            tbody.innerHTML += `
                <tr>
                    <td>${b.id}</td>
                    <td>${b.userId}</td>
                    <td><strong style="color:${b.status === "CANCELLED" ? "var(--accent-red)" : "var(--accent-green)"}">${b.status}</strong></td>
                    <td>${new Date(b.createdDate).toLocaleTimeString()}</td>
                    <td>${new Date(b.lastModifiedDate).toLocaleTimeString()}</td>
                    <td>${deleteBtn}</td>
                </tr>
            `;
        });
    } catch (e) {
        console.error(e);
    }
}

// Soft delete booking
async function cancelBooking(id) {
    if (confirm("Are you sure you want to cancel booking " + id + "?")) {
        try {
            await fetch(`${API_BASE}/bookings/${id}`, { method: "DELETE" });
            await loadBookings();
            await updateSeatInventoryInfo();
        } catch (e) {
            console.error(e);
        }
    }
}

// Toggle admin view
function toggleAdminMode() {
    adminMode = !adminMode;
    document.getElementById("adminModeBtn").innerText = adminMode ? "Switch to Customer Mode (Filter Cancelled)" : "Toggle Admin Mode (Show Cancelled)";
    loadBookings();
}

// Initialize
window.onload = async () => {
    await searchEvents();
    await updateSeatInventoryInfo();
    await loadBookings();
};
