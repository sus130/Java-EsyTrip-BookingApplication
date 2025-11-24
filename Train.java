import java.time.LocalDate;

public class Train extends TravelOption {
    private static final long serialVersionUID = 1L;
    public String from, to;
    public Train(String from, String to, LocalDate date, int available, double price) {
        super(String.format("Train %s->%s", from, to), date, available, price);
        this.from = from; this.to = to;
    }
    @Override String getType() { return "Train"; }
}
