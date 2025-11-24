import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Event extends TravelOption {
    private static final long serialVersionUID = 1L;
    public String venue;
    // If true, the event has specific seating sections (named sections with seat counts)
    public boolean hasSeating = true;
    // section name -> available seats (preserve insertion order)
    public Map<String,Integer> sections = new LinkedHashMap<>();
    // section name -> price per seat
    public Map<String,Double> sectionPrices = new LinkedHashMap<>();
    // For non-seated events we support named areas (standing zones) with capacities
    public Map<String,Integer> areas = new LinkedHashMap<>();
    public Map<String,Double> areaPrices = new LinkedHashMap<>();

    // Legacy constructor: single seating section named "General"
    public Event(String title, String venue, LocalDate date, int seats, double price) {
        super(title + " @" + venue, date, seats, price);
        this.venue = venue;
        this.hasSeating = true;
        this.sections.put("General", seats);
        this.sectionPrices.put("General", price);
    }

    // Constructor for explicit seating sections
    public Event(String title, String venue, LocalDate date, Map<String,Integer> sections, Map<String,Double> sectionPrices) {
        super(title + " @" + venue, date, sections.values().stream().mapToInt(Integer::intValue).sum(),
                sectionPrices.values().stream().findFirst().orElse(0.0));
        this.venue = venue;
        this.hasSeating = true;
        if (sections != null) this.sections.putAll(sections);
        if (sectionPrices != null) this.sectionPrices.putAll(sectionPrices);
    }

    // Constructor for area-based (non-seating) events
    public Event(String title, String venue, LocalDate date, Map<String,Integer> areas, Map<String,Double> areaPrices, boolean isArea) {
        super(title + " @" + venue, date, areas.values().stream().mapToInt(Integer::intValue).sum(),
                areaPrices.values().stream().findFirst().orElse(0.0));
        this.venue = venue;
        this.hasSeating = false;
        if (areas != null) this.areas.putAll(areas);
        if (areaPrices != null) this.areaPrices.putAll(areaPrices);
    }

    @Override String getType() { return "Event"; }

    // Ensure collections are non-null (helps when loading older serialized objects)
    public void ensureCollections() {
        if (this.sections == null) this.sections = new LinkedHashMap<>();
        if (this.sectionPrices == null) this.sectionPrices = new LinkedHashMap<>();
        if (this.areas == null) this.areas = new LinkedHashMap<>();
        if (this.areaPrices == null) this.areaPrices = new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        String base = super.toString();
        StringBuilder sb = new StringBuilder(base);
        ensureCollections();
        if (hasSeating) {
            sb.append(" Sections:");
            for (Map.Entry<String,Integer> e : sections.entrySet()) {
                double p = sectionPrices.getOrDefault(e.getKey(), price);
                sb.append(String.format(" %s[%d seats @ $%.2f]", e.getKey(), e.getValue(), p));
            }
        } else {
            sb.append(" Areas:");
            for (Map.Entry<String,Integer> e : areas.entrySet()) {
                double p = areaPrices.getOrDefault(e.getKey(), price);
                sb.append(String.format(" %s[%d cap @ $%.2f]", e.getKey(), e.getValue(), p));
            }
        }
        return sb.toString();
    }
}
