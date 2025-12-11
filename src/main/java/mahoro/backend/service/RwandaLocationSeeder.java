package mahoro.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Location;
import mahoro.backend.model.LocationType;
import mahoro.backend.repository.LocationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RwandaLocationSeeder implements CommandLineRunner {

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (locationRepository.count() == 0) {
            log.info("Seeding Rwandan locations...");
            seedRwandaLocations();
            log.info("Rwandan locations seeded successfully!");
        } else {
            log.info("Locations already exist, skipping seeding.");
        }
    }

    private void seedRwandaLocations() {
        Map<String, Location> provinces = new HashMap<>();
        String[] provinceNames = {"Kigali City", "Southern Province", "Western Province", "Northern Province", "Eastern Province"};
        
        for (String provinceName : provinceNames) {
            Location province = new Location();
            province.setName(provinceName);
            province.setType(LocationType.PROVINCE);
            province = locationRepository.save(province);
            provinces.put(provinceName, province);
        }

        Map<String, Location> districts = new HashMap<>();
        String[] kigaliDistricts = {"Gasabo", "Kicukiro", "Nyarugenge"};
        for (String districtName : kigaliDistricts) {
            Location district = createLocation(districtName, LocationType.DISTRICT, provinces.get("Kigali City"));
            districts.put(districtName, district);
        }
        
        String[] southernDistricts = {"Nyanza", "Gisagara", "Nyaruguru", "Huye", "Nyamagabe", "Ruhango", "Muhanga", "Kamonyi"};
        for (String districtName : southernDistricts) {
            Location district = createLocation(districtName, LocationType.DISTRICT, provinces.get("Southern Province"));
            districts.put(districtName, district);
        }
        
        String[] westernDistricts = {"Rubavu", "Rutsiro", "Nyabihu", "Ngororero", "Karongi", "Rusizi", "Nyamasheke"};
        for (String districtName : westernDistricts) {
            Location district = createLocation(districtName, LocationType.DISTRICT, provinces.get("Western Province"));
            districts.put(districtName, district);
        }
        
        String[] northernDistricts = {"Rulindo", "Gakenke", "Musanze", "Burera", "Gicumbi"};
        for (String districtName : northernDistricts) {
            Location district = createLocation(districtName, LocationType.DISTRICT, provinces.get("Northern Province"));
            districts.put(districtName, district);
        }

        String[] easternDistricts = {"Rwamagana", "Nyagatare", "Gatsibo", "Kayonza", "Kirehe", "Ngoma", "Bugesera"};
        for (String districtName : easternDistricts) {
            Location district = createLocation(districtName, LocationType.DISTRICT, provinces.get("Eastern Province"));
            districts.put(districtName, district);
        }

        Map<String, Location> sectors = new HashMap<>();
        String[] gasaboSectors = {"Kimironko", "Remera", "Gisozi", "Kacyiru", "Gikondo"};
        
        for (String sectorName : gasaboSectors) {
            Location sector = createLocation(sectorName, LocationType.SECTOR, districts.get("Gasabo"));
            sectors.put(sectorName, sector);
        }

        Map<String, Location> cells = new HashMap<>();
        String[] kimironkoCells = {"Kimironko I", "Kimironko II", "Kimironko III", "Kimironko IV"};
        
        for (String cellName : kimironkoCells) {
            Location cell = createLocation(cellName, LocationType.CELL, sectors.get("Kimironko"));
            cells.put(cellName, cell);
        }

        String[] kimironkoIVillages = {"Amajyambere", "Gacuriro", "Kibagabaga", "Nyagatovu"};
        
        for (String villageName : kimironkoIVillages) {
            createLocation(villageName, LocationType.VILLAGE, cells.get("Kimironko I"));
        }
    }

    private Location createLocation(String name, LocationType type, Location parent) {
        Location location = new Location();
        location.setName(name);
        location.setType(type);
        location.setParent(parent);
        return locationRepository.save(location);
    }
}