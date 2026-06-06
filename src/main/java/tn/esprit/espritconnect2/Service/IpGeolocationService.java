package tn.esprit.espritconnect2.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class IpGeolocationService {

    private static final Logger log = LoggerFactory.getLogger(IpGeolocationService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public CompletableFuture<String> getGeoLocation(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> {
            if (ipAddress == null || ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equalsIgnoreCase("localhost")) {
                return "Local Network";
            }
            try {
                String url = "http://ip-api.com/json/" + ipAddress;
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && "success".equals(response.get("status"))) {
                    String country = (String) response.get("country");
                    String city = (String) response.get("city");
                    return city + ", " + country;
                }
            } catch (Exception e) {
                log.warn("Failed to geolocate IP {}: {}", ipAddress, e.getMessage());
            }
            return "Inconnu";
        });
    }
}
