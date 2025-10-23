package com.cabbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class backend {

    public static void main(String[] args) {
        SpringApplication.run(backend.class, args);
    }

    private Map<String, User> users = new HashMap<>();
    private Map<Long, Driver> drivers = new HashMap<>();
    private Map<Long, Booking> bookings = new HashMap<>();

    private AtomicLong driverIdGen = new AtomicLong(1);
    private AtomicLong bookingIdGen = new AtomicLong(1);

    // --- Models ---
    static class User {
        public String username;
        public String password;
        public String fullName;
        public String phone;

        public User() {}
        public User(String username, String password, String fullName, String phone) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
        }
    }

    static class Driver {
        public Long id;
        public String name;
        public String vehicleNumber;
        public String availability;

        public Driver() {}
        public Driver(Long id, String name, String vehicleNumber, String availability) {
            this.id = id;
            this.name = name;
            this.vehicleNumber = vehicleNumber;
            this.availability = availability;
        }
    }

    static class Booking {
        public Long id;
        public String username;
        public String pickup;
        public String drop;
        public String status;

        public Booking() {}
        public Booking(Long id, String username, String pickup, String drop, String status) {
            this.id = id;
            this.username = username;
            this.pickup = pickup;
            this.drop = drop;
            this.status = status;
        }
    }

    // --- User APIs ---

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User u) {
        if(u.username == null || u.password == null || u.fullName == null || u.phone == null) {
            return ResponseEntity.badRequest().body("All fields required");
        }
        if(users.containsKey(u.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username taken");
        }
        users.put(u.username, u);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> creds) {
        User u = users.get(creds.get("username"));
        if(u == null || !u.password.equals(creds.get("password"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(u);
    }

    @PutMapping("/profile/{username}")
    public ResponseEntity<String> updateProfile(@PathVariable String username, @RequestBody User profile) {
        User u = users.get(username);
        if(u == null) return ResponseEntity.notFound().build();
        u.fullName = profile.fullName != null ? profile.fullName : u.fullName;
        u.phone = profile.phone != null ? profile.phone : u.phone;
        return ResponseEntity.ok("Profile updated");
    }

    @GetMapping("/users/count")
    public long userCount() {
        return users.size();
    }

    // --- Driver APIs ---

    @PostMapping("/drivers")
    public ResponseEntity<Driver> addDriver(@RequestBody Driver d) {
        if(d.name == null || d.vehicleNumber == null || d.availability == null)
            return ResponseEntity.badRequest().build();
        long id = driverIdGen.getAndIncrement();
        d.id = id;
        drivers.put(id, d);
        return ResponseEntity.ok(d);
    }

    @GetMapping("/drivers/count")
    public long driverCount() {
        return drivers.size();
    }

    // --- Booking APIs ---

    @PostMapping("/bookings")
    public ResponseEntity<Booking> bookRide(@RequestBody Booking b) {
        if(b.username == null || b.pickup == null || b.drop == null)
            return ResponseEntity.badRequest().build();
        if(!users.containsKey(b.username)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        long id = bookingIdGen.getAndIncrement();
        b.id = id;
        b.status = "Booked";
        bookings.put(id, b);
        return ResponseEntity.ok(b);
    }

    @GetMapping("/bookings/{username}")
    public List<Booking> getUserBookings(@PathVariable String username) {
        List<Booking> list = new ArrayList<>();
        bookings.values().forEach(b -> {
          if(b.username.equals(username)) list.add(b);
        });
        return list;
    }

    @GetMapping("/bookings/count")
    public long bookingCount() {
        return bookings.size();
    }

    // --- Admin API (summary) ---
    @GetMapping("/admin/summary")
    public Map<String, Long> adminSummary() {
        Map<String, Long> map = new HashMap<>();
        map.put("users", (long)users.size());
        map.put("drivers", (long)drivers.size());
        map.put("bookings", (long)bookings.size());
        return map;
    }
}

