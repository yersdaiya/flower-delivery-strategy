package flowerdelivery.delivery;

import java.util.Set;

public class ZoneRules {
    public static final int MAX_ACTIVE_PER_ZONE = 3;
    private static final Set<String> SUPPORTED_ZONES = Set.of("CENTER", "LEFT_BANK", "RIGHT_BANK");

    public void validate(String zone, int activeInZone) {
        if (zone == null || !SUPPORTED_ZONES.contains(zone))
            throw new DeliveryException("Zone not covered: " + zone);
        if (activeInZone >= MAX_ACTIVE_PER_ZONE)
            throw new DeliveryException("No courier available in zone " + zone);
    }
}