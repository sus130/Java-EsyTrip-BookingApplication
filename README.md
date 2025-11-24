# EsyTrip — Console Booking System

## Overview
This is a simple university-demo console application written in Java. It lets you browse and book Trains, Flights, Hotels and Events. Data is stored locally using Java serialization (no network/API required).

## Files
- `EsyTrip.java` — main application (entrypoint). Saves data to `esytrip_data.ser` in the current directory.
- Domain classes (moved out of `EsyTrip.java`): `User.java`, `Booking.java`, `TravelOption.java`, `Train.java`, `Flight.java`, `Hotel.java`, `Event.java`, `DataStore.java`.

## How to compile & run (Windows PowerShell)

```powershell
Set-Location -Path 'C:\Users\SUSMIT ROY\OneDrive\Desktop\JAVA\JAVA Project'
javac -Xlint:all *.java
java EsyTrip
```

# EsyTrip — Console Booking System

## Project Title
EsyTrip — Console Booking System

## Overview
EsyTrip is a simple console-based booking demo written in Java. It allows browsing and booking of Trains, Flights, Hotels and Events. Data is stored locally using Java serialization (`esytrip_data.ser`). The app supports user registration, login, an admin panel, CSV export/import for users and bookings, and events with seating sections or general-area (GA) zones.

## Features
- Browse inventory: Trains, Flights, Hotels, Events
- Book tickets/rooms/seats with availability checks
- Event support: per-section seating (name, capacity, price) and area/GA zones
- User registration and authentication
- Admin Panel: add schedules, update pricing, view/cancel bookings, edit event sections/areas, export/import CSV
- Local persistence via Java serialization

## Technologies / Tools Used
- Java 17 (tested with Eclipse Adoptium/OpenJDK 17)
- javac / java (JDK command-line tools)
- PowerShell (Windows) for run/test examples

## Files (important source files)
- `EsyTrip.java` — main application (entrypoint)
- `User.java`, `Booking.java`, `TravelOption.java`, `Train.java`, `Flight.java`, `Hotel.java`, `Event.java`, `DataStore.java`
- `README.md` — this file

## Steps to Install & Run (Windows PowerShell)
1. Ensure JDK 17 or later is installed and `javac`/`java` are on your PATH.
2. Open PowerShell and change to the project folder:

```powershell
Set-Location -Path 'C:\Users\SUSMIT ROY\OneDrive\Desktop\JAVA\JAVA Project'
```

3. Compile all Java sources:

```powershell
javac -Xlint:all *.java
```

4. Run the app:

```powershell
java EsyTrip
```

Notes:
- To reset data to the seeded defaults, delete `esytrip_data.ser` and restart the program.

## Default Credentials
- Demo user: `demo` / `demo`
- Admin user: `roysus130` / `roysusmit03`

## Instructions for Testing
There are two scripted tests included for the new Event seating/area flows. Each test removes `esytrip_data.ser` to ensure a predictable seeded state.

Seated section booking (Rock Concert, "Front"):

```powershell
Set-Location -Path 'C:\Users\SUSMIT ROY\OneDrive\Desktop\JAVA\JAVA Project'
Remove-Item .\esytrip_data.ser -Force -ErrorAction SilentlyContinue
Set-Content .\test_event_seated.txt -Value "1`ndemo`ndemo`n4`n1`n1`n1`ny`n5`n0"
Get-Content .\test_event_seated.txt -Raw | java EsyTrip
```

Area/GA booking (Open Air Festival, "Floor GA"):

```powershell
Set-Location -Path 'C:\Users\SUSMIT ROY\OneDrive\Desktop\JAVA\JAVA Project'
Remove-Item .\esytrip_data.ser -Force -ErrorAction SilentlyContinue
Set-Content .\test_event_area.txt -Value "1`ndemo`ndemo`n4`n2`n1`n2`ny`n5`n0"
Get-Content .\test_event_area.txt -Raw | java EsyTrip
```

Manual tests
- Start the app and use the Authentication menu to login as `demo` or as admin `roysus130`.
- As admin, open Admin Panel and try `8` Edit Event Sections/Areas to add/update/remove sections or areas.
- As a regular user, try booking seats in a section or tickets in a GA area and verify bookings appear under "View My Bookings".

## CSV Export/Import
- From the Admin Panel use `Export CSV` / `Import CSV` to export/import `users_export.csv` and `bookings_export.csv`.
- Note: Event definitions (sections/areas) are editable via Admin Panel but are not yet exported/imported as a dedicated events CSV. If you require event import/export, I can add CSV support for event definitions.
