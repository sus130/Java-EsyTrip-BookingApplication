import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public abstract class TravelOption implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id;
    public String title;
    public LocalDate date;
    public int available;
    public double price;

    public TravelOption(String title, LocalDate date, int available, double price) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.title = title;
        this.date = date;
        this.available = available;
        this.price = price;
    }

    abstract String getType();

    @Override
    public String toString() {
        return String.format("[%s] %s (on %s) - Available: %d - $%.2f", id, title, date, available, price);
    }
}
