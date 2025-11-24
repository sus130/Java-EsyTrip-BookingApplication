import java.time.LocalDate;

public class Hotel extends TravelOption {
    private static final long serialVersionUID = 1L;
    public String city;
    public Hotel(String city, LocalDate checkIn, int rooms, double pricePerNight) {
        super(String.format("Hotel in %s", city), checkIn, rooms, pricePerNight);
        this.city = city;
    }
    @Override String getType() { return "Hotel"; }
}
