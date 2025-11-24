import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<TravelOption> trains = new ArrayList<>();
    public List<TravelOption> flights = new ArrayList<>();
    public List<TravelOption> hotels = new ArrayList<>();
    public List<TravelOption> events = new ArrayList<>();
    public List<Booking> bookings = new ArrayList<>();
    public Map<String, User> users = new HashMap<>();
}
