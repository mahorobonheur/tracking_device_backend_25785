package mahoro.backend.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s&zoom=18&addressdetails=1";

    public String reverseGeocode(double latitude, double longitude) {
        try {
            String urlString = String.format(NOMINATIM_URL, latitude, longitude);
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Nominatim requires a valid User-Agent header
            connection.setRequestProperty("User-Agent", "mahoro-backend-tracker/1.0 (your-email@example.com)");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());

            if (json.has("display_name")) {
                return json.getString("display_name");
            } else {
                log.warn("No address found for coordinates ({}, {})", latitude, longitude);
                return "Unknown Location (" + latitude + ", " + longitude + ")";
            }

        } catch (Exception e) {
            log.error("Reverse geocoding failed for ({}, {}): {}", latitude, longitude, e.getMessage());
            return "Error retrieving location";
        }
    }
}
