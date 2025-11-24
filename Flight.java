import java.time.LocalDate;

public class Flight extends TravelOption {
    private static final long serialVersionUID = 1L;
    public String from, to;
    public Flight(String from, String to, LocalDate date, int available, double price) {
        super(String.format("Flight %s->%s", from, to), date, available, price);
        this.from = from; this.to = to;
    }
    @Override String getType() { return "Flight"; }
}
