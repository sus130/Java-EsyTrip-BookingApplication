import java.io.*;
import java.time.LocalDate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class EsyTrip implements Serializable {
    private static final long serialVersionUID = 1L;
    // Domain classes (User, Booking, TravelOption and subtypes, DataStore) were moved to separate files

    // App state
    private DataStore store = new DataStore();
    private transient Scanner sc = new Scanner(System.in);
    private final String DATA_FILE = "esytrip_data.ser";
    private transient User currentUser = null;

    public static void main(String[] args) {
        EsyTrip app = new EsyTrip();
        app.run();
    }

    void run() {
        loadOrSeed();
        // Authentication loop (login/register) before main menu actions
        authMenu();

        boolean running = true;
        while (running) {
            showMainMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": browseAndBook(store.trains); break;
                case "2": browseAndBook(store.flights); break;
                case "3": browseAndBook(store.hotels); break;
                case "4": browseAndBook(store.events); break;
                case "5": viewBookings(); break;
                case "6": cancelBooking(); break;
                case "7": saveData(); System.out.println("Data saved."); break;
                case "8": logoutUser(); break;
                case "9": adminPanel(); break;
                case "10": changePasswordUser(); break;
                case "0": saveData(); System.out.println("Exiting. Changes saved."); running = false; break;
                default: System.out.println("Invalid choice. Try again.");
            }
            System.out.println();
        }
    }

    void showMainMenu() {
        System.out.println("=== EsyTrip — Console Booking System ===");
        System.out.println("1) Trains\n2) Flights\n3) Hotels\n4) Events");
        System.out.println("5) View My Bookings\n6) Cancel Booking\n7) Save Now\n10) Change Password");
        if (currentUser!=null && currentUser.isAdmin) System.out.println("9) Admin Panel");
        System.out.println("8) Logout\n0) Exit");
        System.out.print("Choose an option: ");
    }

    void browseAndBook(List<TravelOption> list) {
        if (list.isEmpty()) { System.out.println("No options available."); return; }
        System.out.println("Available:");
        for (int i=0;i<list.size();i++) {
                TravelOption opt = list.get(i);
                System.out.printf("%d) %s - Type: %s\n", i+1, opt.toString(), opt.getType());
        }
        System.out.print("Select number to view/book (or 0 to go back): ");
        int sel = readInt(0, list.size());
        if (sel==0) return;
        TravelOption opt = list.get(sel-1);
        System.out.println(opt.toString());
        // Special handling for Events with sections/areas
        if (opt instanceof Event) {
            Event ev = (Event) opt;
            ev.ensureCollections();
            // ensure login
            if (!ensureLoggedIn()) {
                System.out.println("You must be logged in to make a booking. Use Register or Login from the authentication menu.");
                return;
            }
            String purchaser = currentUser.username;
            if (ev.hasSeating && !ev.sections.isEmpty()) {
                System.out.println("Choose section to book:");
                int i = 1;
                java.util.List<String> keys = new java.util.ArrayList<>(ev.sections.keySet());
                for (String k : keys) {
                    int av = ev.sections.getOrDefault(k, 0);
                    double p = ev.sectionPrices.getOrDefault(k, ev.price);
                    System.out.printf("%d) %s - %d seats - $%.2f\n", i++, k, av, p);
                }
                System.out.print("Select section number (0 to cancel): ");
                int selSec = readInt(0, keys.size()); if (selSec==0) { System.out.println("Booking cancelled."); return; }
                String section = keys.get(selSec-1);
                int avail = ev.sections.getOrDefault(section, 0);
                if (avail<=0) { System.out.println("No seats available in that section."); return; }
                System.out.printf("Enter quantity to book (available %d) or 0 to cancel: ", avail);
                int qty = readInt(0, avail); if (qty==0) { System.out.println("Booking cancelled."); return; }
                double unitPrice = ev.sectionPrices.getOrDefault(section, ev.price);
                double total = unitPrice * qty;
                System.out.printf("Confirm booking %d x %s (%s) for $%.2f? (y/n): ", qty, ev.title, section, total);
                String ok = sc.nextLine().trim().toLowerCase(); if (!ok.equals("y")) { System.out.println("Booking aborted."); return; }
                ev.sections.put(section, avail - qty); ev.available -= qty;
                Booking b = new Booking(ev.id, ev.title, ev.getType(), qty, total, purchaser, section);
                store.bookings.add(b);
                System.out.println("Booked: " + b.toString());
                saveData();
                return;
            } else if (!ev.areas.isEmpty()) {
                System.out.println("Choose area to book:");
                int i = 1;
                java.util.List<String> keys = new java.util.ArrayList<>(ev.areas.keySet());
                for (String k : keys) {
                    int av = ev.areas.getOrDefault(k, 0);
                    double p = ev.areaPrices.getOrDefault(k, ev.price);
                    System.out.printf("%d) %s - %d capacity - $%.2f\n", i++, k, av, p);
                }
                System.out.print("Select area number (0 to cancel): ");
                int selArea = readInt(0, keys.size()); if (selArea==0) { System.out.println("Booking cancelled."); return; }
                String area = keys.get(selArea-1);
                int avail = ev.areas.getOrDefault(area, 0);
                if (avail<=0) { System.out.println("No capacity available in that area."); return; }
                System.out.printf("Enter quantity to book (available %d) or 0 to cancel: ", avail);
                int qty = readInt(0, avail); if (qty==0) { System.out.println("Booking cancelled."); return; }
                double unitPrice = ev.areaPrices.getOrDefault(area, ev.price);
                double total = unitPrice * qty;
                System.out.printf("Confirm booking %d x %s (%s) for $%.2f? (y/n): ", qty, ev.title, area, total);
                String ok = sc.nextLine().trim().toLowerCase(); if (!ok.equals("y")) { System.out.println("Booking aborted."); return; }
                ev.areas.put(area, avail - qty); ev.available -= qty;
                Booking b = new Booking(ev.id, ev.title, ev.getType(), qty, total, purchaser, area);
                store.bookings.add(b);
                System.out.println("Booked: " + b.toString());
                saveData();
                return;
            }
            // fallback to generic availability
        }
        // generic booking flow for non-event or fallback
        System.out.printf("Enter quantity to book (available %d) or 0 to cancel: ", opt.available);
        int qty = readInt(0, opt.available);
        if (qty==0) { System.out.println("Booking cancelled."); return; }
        // require login to book
        if (!ensureLoggedIn()) {
            System.out.println("You must be logged in to make a booking. Use Register or Login from the authentication menu.");
            return;
        }
        String name = currentUser != null ? currentUser.username : "guest";
        double total = qty * opt.price;
        System.out.printf("Confirm booking %d x %s for $%.2f? (y/n): ", qty, opt.title, total);
        String ok = sc.nextLine().trim().toLowerCase();
        if (!ok.equals("y")) { System.out.println("Booking aborted."); return; }
        // perform booking
        opt.available -= qty;
        Booking b = new Booking(opt.id, opt.title, opt.getType(), qty, total, name);
        store.bookings.add(b);
        System.out.println("Booked: " + b.toString());
        saveData();
    }

    void viewBookings() {
        if (!ensureLoggedIn()) { System.out.println("Login required to view your bookings."); return; }
        List<Booking> mine = store.bookings.stream().filter(b -> b.customerName.equals(currentUser.username)).collect(Collectors.toList());
        if (mine.isEmpty()) { System.out.println("You have no bookings yet."); return; }
        System.out.println("Your bookings:");
        for (Booking b : mine) System.out.println(b.toString());
    }

    void cancelBooking() {
        if (!ensureLoggedIn()) { System.out.println("Login required to cancel bookings."); return; }
        List<Booking> mine = store.bookings.stream().filter(b -> b.customerName.equals(currentUser.username)).collect(Collectors.toList());
        if (mine.isEmpty()) { System.out.println("You have no bookings to cancel."); return; }
        System.out.println("Your bookings:");
        for (int i=0;i<mine.size();i++) System.out.printf("%d) %s\n", i+1, mine.get(i));
        System.out.print("Select booking number to cancel (0 to go back): ");
        int sel = readInt(0, mine.size());
        if (sel==0) return;
        Booking b = mine.get(sel-1);
        System.out.print("Confirm cancel booking " + b.bookingId + " (y/n): ");
        String ok = sc.nextLine().trim().toLowerCase();
        if (!ok.equals("y")) { System.out.println("Cancel aborted."); return; }
        // restore availability
        TravelOption opt = findOptionById(b.optionId);
        if (opt!=null) opt.available += b.qty;
        store.bookings.remove(b);
        System.out.println("Booking cancelled and seats restored.");
        saveData();
    }

    boolean ensureLoggedIn() {
        return currentUser != null;
    }

    void logoutUser() {
        if (currentUser==null) { System.out.println("Not logged in."); return; }
        System.out.println("User " + currentUser.username + " logged out.");
        currentUser = null;
        // after logout, prompt auth menu again
        authMenu();
    }

    void authMenu() {
        while (true) {
            System.out.println("\n=== EsyTrip Authentication ===");
            System.out.println("1) Login\n2) Register\n3) Continue as Guest\n4) Admin Login\n0) Exit");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1": if (loginUser()) return; else break;
                case "2": registerUser(); break;
                case "3": currentUser = null; return;
                case "4": if (loginAdmin()) return; else break;
                case "0": saveData(); System.out.println("Exiting."); System.exit(0); return;
                default: System.out.println("Invalid.");
            }
        }
    }

    void registerUser() {
        System.out.print("Choose username: ");
        String u = sc.nextLine().trim();
        if (u.isEmpty()) { System.out.println("Username cannot be empty."); return; }
        if (store.users.containsKey(u)) { System.out.println("Username taken."); return; }
        System.out.print("Choose password: ");
        String p = sc.nextLine();
        String h = hashPassword(p);
        User user = new User(u, h);
        store.users.put(u, user);
        saveData();
        System.out.println("User registered. You may now login.");
    }

    boolean loginUser() {
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine();
        User existing = store.users.get(u);
        if (existing==null) { System.out.println("No such user."); return false; }
        String h = hashPassword(p);
        if (!h.equals(existing.passwordHash)) { System.out.println("Invalid password."); return false; }
        currentUser = existing;
        System.out.println("Welcome, " + currentUser.username + "!");
        return true;
    }

    boolean loginAdmin() {
        System.out.print("Admin username: ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine();
        User existing = store.users.get(u);
        if (existing==null) { System.out.println("No such user."); return false; }
        if (!existing.isAdmin) { System.out.println("User is not admin."); return false; }
        String h = hashPassword(p);
        if (!h.equals(existing.passwordHash)) { System.out.println("Invalid password."); return false; }
        currentUser = existing;
        System.out.println("Welcome, admin " + currentUser.username + "!");
        return true;
    }

    String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toString(password.hashCode());
        }
    }

    void adminPanel() {
        if (currentUser==null || !currentUser.isAdmin) { System.out.println("Admin access only."); return; }
        while (true) {
            System.out.println("\n=== Admin Panel ===");
            System.out.println("1) Add Schedule\n2) Update Pricing\n3) Cancel Booking (any)\n4) View All Bookings\n5) Export CSV\n6) Import CSV\n9) Change User Password\n0) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1": adminAddSchedule(); break;
                case "2": adminUpdatePricing(); break;
                case "3": adminCancelAnyBooking(); break;
                case "4": adminViewAllBookings(); break;
                case "5": adminExportMenu(); break;
                case "6": adminImportMenu(); break;
                case "8": adminEditEventSections(); break;
                case "9": adminChangeUserPassword(); break;
                case "0": return;
                default: System.out.println("Invalid.");
            }
        }
    }

    void adminExportMenu() {
        System.out.println("Export: 1) Users CSV  2) Bookings CSV  0) Back");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1": System.out.print("Filename (users CSV) [users_export.csv]: ");
                      String fu = sc.nextLine().trim(); if (fu.isEmpty()) fu = "users_export.csv"; exportUsersCSV(fu); break;
            case "2": System.out.print("Filename (bookings CSV) [bookings_export.csv]: ");
                      String fb = sc.nextLine().trim(); if (fb.isEmpty()) fb = "bookings_export.csv"; exportBookingsCSV(fb); break;
            default: return;
        }
    }

    void adminImportMenu() {
        System.out.println("Import: 1) Users CSV  2) Bookings CSV  0) Back");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1": System.out.print("Filename to import users from [users_export.csv]: ");
                      String fu = sc.nextLine().trim(); if (fu.isEmpty()) fu = "users_export.csv"; importUsersCSV(fu); break;
            case "2": System.out.print("Filename to import bookings from [bookings_export.csv]: ");
                      String fb = sc.nextLine().trim(); if (fb.isEmpty()) fb = "bookings_export.csv"; importBookingsCSV(fb); break;
            default: return;
        }
    }


    void exportUsersCSV(String filename) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filename))) {
            w.write("username,passwordHash,isAdmin,created"); w.newLine();
            for (User u : store.users.values()) {
                w.write(String.format("%s,%s,%b,%s", escapeCsv(u.username), escapeCsv(u.passwordHash), u.isAdmin, u.created.toString()));
                w.newLine();
            }
            System.out.println("Users exported to " + filename);
        } catch (IOException e) { System.out.println("Export failed: " + e.getMessage()); }
    }

    void importUsersCSV(String filename) {
        File f = new File(filename);
        if (!f.exists()) { System.out.println("File not found: " + filename); return; }
        // backup prior to import
        if (!confirmAndBackupBeforeImport()) return;
        int added = 0, updated = 0;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine())!=null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                String username = unescapeCsv(parts[0]);
                String passwordField = unescapeCsv(parts[1]);
                boolean isAdmin = Boolean.parseBoolean(parts[2]);
                LocalDate created = LocalDate.parse(parts[3]);
                String passwordHash = passwordField;
                if (!looksLikeSha256(passwordField)) {
                    // treat as plaintext and hash it
                    passwordHash = hashPassword(passwordField);
                }
                User u = new User(username, passwordHash, isAdmin);
                u.created = created;
                if (store.users.containsKey(username)) { store.users.put(username, u); updated++; }
                else { store.users.put(username, u); added++; }
            }
            saveData();
            System.out.printf("Import completed: %d added, %d updated\n", added, updated);
        } catch (IOException | java.time.format.DateTimeParseException | NumberFormatException e) { System.out.println("Import failed: " + e.getMessage()); }
    }

    void exportBookingsCSV(String filename) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filename))) {
            w.write("bookingId,optionId,optionTitle,optionType,qty,totalPrice,customerName,created"); w.newLine();
            for (Booking b : store.bookings) {
                w.write(String.format("%s,%s,%s,%s,%d,%.2f,%s,%s",
                    escapeCsv(b.bookingId), escapeCsv(b.optionId), escapeCsv(b.optionTitle), escapeCsv(b.optionType), b.qty, b.totalPrice, escapeCsv(b.customerName), b.created.toString()));
                w.newLine();
            }
            System.out.println("Bookings exported to " + filename);
        } catch (IOException e) { System.out.println("Export failed: " + e.getMessage()); }
    }

    
    

    void importBookingsCSV(String filename) {
        File f = new File(filename);
        if (!f.exists()) { System.out.println("File not found: " + filename); return; }
        // backup prior to import
        if (!confirmAndBackupBeforeImport()) return;
        int added = 0, skipped = 0;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine())!=null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 8) continue;
                String bookingId = unescapeCsv(parts[0]);
                String optionId = unescapeCsv(parts[1]);
                String optionTitle = unescapeCsv(parts[2]);
                String optionType = unescapeCsv(parts[3]);
                int qty = Integer.parseInt(parts[4]);
                double totalPrice = Double.parseDouble(parts[5]);
                String customerName = unescapeCsv(parts[6]);
                LocalDate created = LocalDate.parse(parts[7]);
                boolean exists = store.bookings.stream().anyMatch(b -> b.bookingId.equals(bookingId));
                if (exists) { skipped++; continue; }
                Booking b = new Booking(optionId, optionTitle, optionType, qty, totalPrice, customerName);
                b.bookingId = bookingId; b.created = created;
                store.bookings.add(b);
                added++;
            }
            saveData();
            System.out.printf("Import completed: %d added, %d skipped (duplicates)\n", added, skipped);
        } catch (IOException | java.time.format.DateTimeParseException | NumberFormatException e) { System.out.println("Import failed: " + e.getMessage()); }
    }

    

    boolean confirmAndBackupBeforeImport() {
        // create backup of data file
        File data = new File(DATA_FILE);
        if (data.exists()) {
            String ts = String.valueOf(System.currentTimeMillis());
            String backupName = DATA_FILE + ".bak." + ts;
            try (FileInputStream in = new FileInputStream(data); FileOutputStream out = new FileOutputStream(backupName)) {
                byte[] buf = new byte[8192]; int r;
                while ((r = in.read(buf))>0) out.write(buf,0,r);
                System.out.println("Backup created: " + backupName);
            } catch (IOException e) { System.out.println("Backup failed: " + e.getMessage()); return false; }
        } else {
            System.out.println("No existing data file to backup.");
        }
        System.out.print("Proceed with import? (y/n): ");
        String ok = sc.nextLine().trim().toLowerCase();
        return ok.equals("y");
    }

    boolean looksLikeSha256(String s) {
        if (s==null) return false;
        return s.matches("[0-9a-fA-F]{64}");
    }

    void adminChangeUserPassword() {
        System.out.print("Username to change password for: ");
        String u = sc.nextLine().trim();
        User user = store.users.get(u);
        if (user==null) { System.out.println("No such user."); return; }
        System.out.print("New password: ");
        String p = sc.nextLine();
        if (p==null || p.isEmpty()) { System.out.println("Password cannot be empty."); return; }
        user.passwordHash = hashPassword(p);
        saveData();
        System.out.println("Password updated for " + u);
    }

    void changePasswordUser() {
        if (!ensureLoggedIn()) { System.out.println("You must be logged in to change your password."); return; }
        System.out.print("Current password: ");
        String current = sc.nextLine();
        if (!hashPassword(current).equals(currentUser.passwordHash)) { System.out.println("Current password is incorrect."); return; }
        System.out.print("New password: ");
        String np = sc.nextLine();
        if (np==null || np.isEmpty()) { System.out.println("New password cannot be empty."); return; }
        System.out.print("Confirm new password: ");
        String cp = sc.nextLine();
        if (!np.equals(cp)) { System.out.println("Passwords do not match."); return; }
        currentUser.passwordHash = hashPassword(np);
        store.users.put(currentUser.username, currentUser);
        saveData();
        System.out.println("Password changed successfully.");
    }

    String escapeCsv(String s) {
        if (s==null) return "";
        if (s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"") ) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    String unescapeCsv(String s) {
        if (s==null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }

    void adminAddSchedule() {
        System.out.println("Choose category to add:");
        System.out.println("1) Train\n2) Flight\n3) Hotel\n4) Event\n0) Cancel");
        int cat = readInt(0,4);
        if (cat==0) return;
        try {
            switch(cat) {
                case 1: {
                    System.out.print("From: "); String from = sc.nextLine().trim();
                    System.out.print("To: "); String to = sc.nextLine().trim();
                    System.out.print("Date (YYYY-MM-DD): "); LocalDate d = LocalDate.parse(sc.nextLine().trim());
                    System.out.print("Seats available: "); int seats = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Price per seat: "); double price = Double.parseDouble(sc.nextLine().trim());
                    Train t = new Train(from,to,d,seats,price);
                    store.trains.add(t);
                    System.out.println("Added train: " + t);
                    break;
                }
                case 2: {
                    System.out.print("From: "); String from = sc.nextLine().trim();
                    System.out.print("To: "); String to = sc.nextLine().trim();
                    System.out.print("Date (YYYY-MM-DD): "); LocalDate d = LocalDate.parse(sc.nextLine().trim());
                    System.out.print("Seats available: "); int seats = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Price per seat: "); double price = Double.parseDouble(sc.nextLine().trim());
                    Flight f = new Flight(from,to,d,seats,price);
                    store.flights.add(f);
                    System.out.println("Added flight: " + f);
                    break;
                }
                case 3: {
                    System.out.print("City: "); String city = sc.nextLine().trim();
                    System.out.print("Check-in Date (YYYY-MM-DD): "); LocalDate d = LocalDate.parse(sc.nextLine().trim());
                    System.out.print("Rooms available: "); int rooms = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Price per night: "); double price = Double.parseDouble(sc.nextLine().trim());
                    Hotel h = new Hotel(city,d,rooms,price);
                    store.hotels.add(h);
                    System.out.println("Added hotel: " + h);
                    break;
                }
                case 4: {
                    System.out.print("Event title: "); String title = sc.nextLine().trim();
                    System.out.print("Venue: "); String venue = sc.nextLine().trim();
                    System.out.print("Date (YYYY-MM-DD): "); LocalDate d = LocalDate.parse(sc.nextLine().trim());
                    System.out.print("Seats available: "); int seats = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Price per ticket: "); double price = Double.parseDouble(sc.nextLine().trim());
                    Event e = new Event(title,venue,d,seats,price);
                    store.events.add(e);
                    System.out.println("Added event: " + e);
                    break;
                }
            }
            saveData();
        } catch (java.time.format.DateTimeParseException | NumberFormatException ex) { System.out.println("Failed to add schedule: " + ex.getMessage()); }
    }

    void adminUpdatePricing() {
        List<TravelOption> all = concatAllOptions();
        if (all.isEmpty()) { System.out.println("No options available."); return; }
        System.out.println("Available options:");
        for (int i=0;i<all.size();i++) System.out.printf("%d) %s - $%.2f\n", i+1, all.get(i), all.get(i).price);
        System.out.print("Select option number to update price (0 to cancel): ");
        int sel = readInt(0, all.size()); if (sel==0) return;
        TravelOption opt = all.get(sel-1);
        System.out.print("New price: ");
        try {
            double np = Double.parseDouble(sc.nextLine().trim());
            opt.price = np;
            System.out.println("Price updated: " + opt);
            saveData();
        } catch (Exception ex) { System.out.println("Invalid price."); }
    }

    void adminCancelAnyBooking() {
        if (store.bookings.isEmpty()) { System.out.println("No bookings."); return; }
        System.out.println("All bookings:");
        for (int i=0;i<store.bookings.size();i++) System.out.printf("%d) %s\n", i+1, store.bookings.get(i));
        System.out.print("Select booking number to cancel (0 to go back): ");
        int sel = readInt(0, store.bookings.size()); if (sel==0) return;
        Booking b = store.bookings.get(sel-1);
        System.out.print("Confirm cancel booking " + b.bookingId + " (y/n): ");
        String ok = sc.nextLine().trim().toLowerCase(); if (!ok.equals("y")) { System.out.println("Aborted."); return; }
        TravelOption opt = findOptionById(b.optionId);
        if (opt!=null) opt.available += b.qty;
        store.bookings.remove(b);
        System.out.println("Booking cancelled by admin.");
        saveData();
    }

    void adminViewAllBookings() {
        if (store.bookings.isEmpty()) { System.out.println("No bookings."); return; }
        System.out.println("All bookings:");
        for (Booking b : store.bookings) System.out.println(b);
    }

    void adminEditEventSections() {
        if (currentUser==null || !currentUser.isAdmin) { System.out.println("Admin access only."); return; }
        List<Event> events = new ArrayList<>();
        for (TravelOption t : concatAllOptions()) if (t instanceof Event) events.add((Event)t);
        if (events.isEmpty()) { System.out.println("No events available to edit."); return; }
        System.out.println("Select event to edit:");
        for (int i=0;i<events.size();i++) System.out.printf("%d) %s\n", i+1, events.get(i).toString());
        System.out.print("Choose event number (0 to cancel): ");
        int sel = readInt(0, events.size()); if (sel==0) return;
        Event ev = events.get(sel-1);
        ev.ensureCollections();
        while (true) {
            System.out.println("Editing event: " + ev.title);
            System.out.println("1) Add section/area\n2) Update capacity\n3) Update price\n4) Remove section/area\n0) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1": {
                    System.out.print("Enter name for new section/area: ");
                    String name = sc.nextLine().trim(); if (name.isEmpty()) { System.out.println("Name required."); break; }
                    System.out.print("Capacity: ");
                    int cap = readInt(0, Integer.MAX_VALUE);
                    System.out.print("Price per ticket: ");
                    double price = 0.0; try { price = Double.parseDouble(sc.nextLine().trim()); } catch (NumberFormatException ex) { System.out.println("Invalid price."); break; }
                    if (ev.hasSeating) { ev.sections.put(name, cap); ev.sectionPrices.put(name, price); }
                    else { ev.areas.put(name, cap); ev.areaPrices.put(name, price); }
                    ev.available = ev.hasSeating ? ev.sections.values().stream().mapToInt(Integer::intValue).sum() : ev.areas.values().stream().mapToInt(Integer::intValue).sum();
                    saveData(); System.out.println("Added."); break;
                }
                case "2": {
                    if (ev.hasSeating) {
                        java.util.List<String> keys = new ArrayList<>(ev.sections.keySet());
                        if (keys.isEmpty()) { System.out.println("No sections to update."); break; }
                        for (int i=0;i<keys.size();i++) System.out.printf("%d) %s - %d\n", i+1, keys.get(i), ev.sections.get(keys.get(i)));
                        System.out.print("Select section to update: "); int si = readInt(1, keys.size()); String name = keys.get(si-1);
                        System.out.print("New capacity: "); int newCap = readInt(0, Integer.MAX_VALUE);
                        int old = ev.sections.getOrDefault(name, 0); ev.sections.put(name, newCap); ev.available += (newCap - old);
                        saveData(); System.out.println("Updated.");
                    } else {
                        java.util.List<String> keys = new ArrayList<>(ev.areas.keySet());
                        if (keys.isEmpty()) { System.out.println("No areas to update."); break; }
                        for (int i=0;i<keys.size();i++) System.out.printf("%d) %s - %d\n", i+1, keys.get(i), ev.areas.get(keys.get(i)));
                        System.out.print("Select area to update: "); int si = readInt(1, keys.size()); String name = keys.get(si-1);
                        System.out.print("New capacity: "); int newCap = readInt(0, Integer.MAX_VALUE);
                        int old = ev.areas.getOrDefault(name, 0); ev.areas.put(name, newCap); ev.available += (newCap - old);
                        saveData(); System.out.println("Updated.");
                    }
                    break;
                }
                case "3": {
                    if (ev.hasSeating) {
                        java.util.List<String> keys = new ArrayList<>(ev.sectionPrices.keySet());
                        if (keys.isEmpty()) { System.out.println("No sections to price."); break; }
                        for (int i=0;i<keys.size();i++) System.out.printf("%d) %s - $%.2f\n", i+1, keys.get(i), ev.sectionPrices.get(keys.get(i)));
                        System.out.print("Select section to update price: "); int si = readInt(1, keys.size()); String name = keys.get(si-1);
                        System.out.print("New price: "); double p = 0.0; try { p = Double.parseDouble(sc.nextLine().trim()); } catch (NumberFormatException ex) { System.out.println("Invalid price."); break; }
                        ev.sectionPrices.put(name, p); saveData(); System.out.println("Updated price.");
                    } else {
                        java.util.List<String> keys = new ArrayList<>(ev.areaPrices.keySet());
                        if (keys.isEmpty()) { System.out.println("No areas to price."); break; }
                        for (int i=0;i<keys.size();i++) System.out.printf("%d) %s - $%.2f\n", i+1, keys.get(i), ev.areaPrices.get(keys.get(i)));
                        System.out.print("Select area to update price: "); int si = readInt(1, keys.size()); String name = keys.get(si-1);
                        System.out.print("New price: "); double p = 0.0; try { p = Double.parseDouble(sc.nextLine().trim()); } catch (NumberFormatException ex) { System.out.println("Invalid price."); break; }
                        ev.areaPrices.put(name, p); saveData(); System.out.println("Updated price.");
                    }
                    break;
                }
                case "4": {
                    if (ev.hasSeating) {
                        java.util.List<String> keys = new ArrayList<>(ev.sections.keySet());
                        if (keys.isEmpty()) { System.out.println("No sections to remove."); break; }
                        for (int i=0;i<keys.size();i++) System.out.printf("%d) %s - %d\n", i+1, keys.get(i), ev.sections.get(keys.get(i)));
                        System.out.print("Select section to remove: "); int si = readInt(1, keys.size()); String name = keys.get(si-1);
                        int old = ev.sections.remove(name); ev.sectionPrices.remove(name); ev.available = Math.max(0, ev.available - old);
                        saveData(); System.out.println("Removed section.");
                    } else {
                        java.util.List<String> keys = new ArrayList<>(ev.areas.keySet());
                        if (keys.isEmpty()) { System.out.println("No areas to remove."); break; }
                        for (int i=0;i<keys.size();i++) System.out.printf("%d) %s - %d\n", i+1, keys.get(i), ev.areas.get(keys.get(i)));
                        System.out.print("Select area to remove: "); int si = readInt(1, keys.size()); String name = keys.get(si-1);
                        int old = ev.areas.remove(name); ev.areaPrices.remove(name); ev.available = Math.max(0, ev.available - old);
                        saveData(); System.out.println("Removed area.");
                    }
                    break;
                }
                case "0": return;
                default: System.out.println("Invalid.");
            }
        }
    }

    TravelOption findOptionById(String id) {
        for (TravelOption t : concatAllOptions()) if (t.id.equals(id)) return t;
        return null;
    }

    List<TravelOption> concatAllOptions() {
        List<TravelOption> all = new ArrayList<>();
        all.addAll(store.trains); all.addAll(store.flights); all.addAll(store.hotels); all.addAll(store.events);
        return all;
    }

    int readInt(int min, int max) {
        while (true) {
            String s = sc.nextLine().trim();
                try {
                    int v = Integer.parseInt(s);
                    if (v<min || v>max) System.out.printf("Enter a number between %d and %d: ", min, max);
                    else return v;
                } catch (NumberFormatException e) {
                    System.out.print("Invalid input. Enter a number: ");
                }
        }
    }

    void loadOrSeed() {
        File f = new File(DATA_FILE);
        if (f.exists()) {
            if (loadData()) { System.out.println("Loaded saved data."); ensureAdminAccountExists(); return; }
            else System.out.println("Could not load saved data; seeding defaults.");
        }
        // If loading succeeded above we return; otherwise we seed defaults.
        seedDefaults();
        saveData();
        System.out.println("Seeded default inventory.");
        // Ensure admin account exists or is promoted even after seeding
        ensureAdminAccountExists();
    }

    // Ensure admin account exists even when loading an existing data file
    void ensureAdminAccountExists() {
        String adminUser = "roysus130";
        String adminHash = hashPassword("roysusmit03");
        if (!store.users.containsKey(adminUser)) {
            store.users.put(adminUser, new User(adminUser, adminHash, true));
            saveData();
            System.out.println("Admin account created: " + adminUser + " (seed)");
            return;
        }
        User existing = store.users.get(adminUser);
        if (!existing.isAdmin) {
            // Promote existing user to admin and (re)set password to the requested admin password
            existing.isAdmin = true;
            if (!existing.passwordHash.equals(adminHash)) {
                existing.passwordHash = adminHash;
                System.out.println("Existing user '" + adminUser + "' promoted to admin and password reset to requested value.");
            } else {
                System.out.println("Existing user '" + adminUser + "' promoted to admin.");
            }
            store.users.put(adminUser, existing);
            saveData();
        }
    }

    void seedDefaults() {
        store = new DataStore();
        // Trains
        store.trains.add(new Train("CityA","CityB", LocalDate.now().plusDays(2), 50, 12.5));
        store.trains.add(new Train("CityB","CityC", LocalDate.now().plusDays(5), 30, 20.0));
        // Flights
        store.flights.add(new Flight("NYC","LAX", LocalDate.now().plusDays(10), 100, 199.99));
        store.flights.add(new Flight("LAX","SFO", LocalDate.now().plusDays(3), 40, 79.0));
        // Hotels
        store.hotels.add(new Hotel("CityA", LocalDate.now().plusDays(2), 10, 75.0));
        store.hotels.add(new Hotel("CityC", LocalDate.now().plusDays(5), 5, 120.0));
        // Events
        // Multi-section seated event
        java.util.Map<String,Integer> sections = new java.util.LinkedHashMap<>();
        java.util.Map<String,Double> sectionPrices = new java.util.LinkedHashMap<>();
        sections.put("Front", 100); sectionPrices.put("Front", 99.99);
        sections.put("Middle", 200); sectionPrices.put("Middle", 59.99);
        sections.put("Balcony", 150); sectionPrices.put("Balcony", 29.99);
        store.events.add(new Event("Rock Concert", "Stadium1", LocalDate.now().plusDays(15), sections, sectionPrices));
        // Area-based GA event (no assigned seating)
        java.util.Map<String,Integer> areas = new java.util.LinkedHashMap<>();
        java.util.Map<String,Double> areaPrices = new java.util.LinkedHashMap<>();
        areas.put("Floor GA", 500); areaPrices.put("Floor GA", 49.99);
        areas.put("Lawn", 300); areaPrices.put("Lawn", 19.99);
        store.events.add(new Event("Open Air Festival", "Meadow Park", LocalDate.now().plusDays(30), areas, areaPrices, true));
        // default demo user
        String demoHash = hashPassword("demo");
        store.users.put("demo", new User("demo", demoHash));
        // default admin user (changed per request)
        String adminHash = hashPassword("roysusmit03");
        store.users.put("roysus130", new User("roysus130", adminHash, true));
    }

    boolean saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(store);
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save data: " + e.getMessage());
            return false;
        }
    }

    boolean loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Object o = ois.readObject();
            if (o instanceof DataStore) { store = (DataStore) o; return true; }
            return false;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }
}
