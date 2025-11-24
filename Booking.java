import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;
    public String bookingId;
    public String optionId;
    public String optionTitle;
    public String optionType;
    public int qty;
    public double totalPrice;
    public String customerName;
    // optional seat/area information (e.g., "VIP", "Floor", or null)
    public String seatInfo;
    public LocalDate created;

    public Booking(String optionId, String optionTitle, String optionType, int qty, double totalPrice, String customerName) {
        this.bookingId = UUID.randomUUID().toString().substring(0, 8);
        this.optionId = optionId;
        this.optionTitle = optionTitle;
        this.optionType = optionType;
        this.qty = qty;
        this.totalPrice = totalPrice;
        this.customerName = customerName;
        this.created = LocalDate.now();
    }

    public Booking(String optionId, String optionTitle, String optionType, int qty, double totalPrice, String customerName, String seatInfo) {
        this(optionId, optionTitle, optionType, qty, totalPrice, customerName);
        this.seatInfo = seatInfo;
    }

    @Override
    public String toString() {
        String base = String.format("Booking %s - %s (%s) x%d - $%.2f - by %s on %s", bookingId, optionTitle, optionType, qty, totalPrice, customerName, created);
        if (seatInfo != null && !seatInfo.isEmpty()) base += " [" + seatInfo + "]";
        return base;
    }
}
