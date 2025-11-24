import java.io.Serializable;
import java.time.LocalDate;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public String username;
    public String passwordHash; // sha-256 hex
    public boolean isAdmin;
    public LocalDate created;

    public User(String username, String passwordHash) {
        this(username, passwordHash, false);
    }

    public User(String username, String passwordHash, boolean isAdmin) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
        this.created = LocalDate.now();
    }

    @Override
    public String toString() { return String.format("User %s (created %s)", username, created); }
}
