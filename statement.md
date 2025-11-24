Problem Statement

EsyTrip is a compact, self-contained console booking application intended as an educational/demo project. The problem it solves is twofold:

- There is a need for a small, easy-to-understand example of a booking system that demonstrates common domain concepts (users, bookings, inventory, admin operations) without requiring a web stack or database setup.
- Existing simple examples often omit realistic event booking concerns such as per-section seating or general-admission (GA) area capacity; this project adds that capability for realistic demonstrations.

Scope of the Project

In scope:
- Single-process, console-based Java application (CLI) that runs locally.
- Browse and book inventory types: Trains, Flights, Hotels, Events.
- Event support for both named seating sections (per-section capacity and price) and named areas / GA zones.
- User accounts with registration and login; admin accounts with elevated controls.
- Admin Panel functions: add/update inventory, edit event sections/areas, view and cancel bookings, export/import users and bookings as CSV.
- Local persistence using Java serialization to `esytrip_data.ser` for easy portability.
- Simple scripted tests (PowerShell) to validate booking flows.

Out of scope:
- Networked server or remote database integration (no REST API/back-end service).
- Payment processing, real-time seat locking across distributed clients, or heavy concurrency support.
- Full-featured GUI or web frontend (the app is intentionally CLI-only).

Target Users

- Computer science students learning basic object-oriented design and I/O in Java.
- Instructors who need a compact demonstration of a booking domain with admin/user roles.
- Developers creating small prototypes or exercises that require booking semantics without the overhead of a web stack.
- Graders and reviewers who need a reproducible, local demo environment.

High-level Features

- Authentication: user registration, login, and guest access.
- Inventory types: Trains, Flights, Hotels, Events with common booking flows.
- Event enhancements: named seating sections (capacity + per-seat price), and areas/GA zones (capacity + price).
- Booking lifecycle: create bookings (quantity and availability checks), view bookings, cancel bookings (restore availability).
- Admin Panel: create/edit inventory, change pricing, edit event sections/areas interactively, export/import users and bookings via CSV.
- Persistence: local binary serialization (`esytrip_data.ser`) with seeded defaults and simple backups before imports.
- Scripted tests: PowerShell scripts that seed predictable test data and exercise seated and GA booking flows.

