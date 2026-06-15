package com.ridex.driver.service;

import com.ridex.driver.entity.Driver;
import com.ridex.driver.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverLocationService locationService;

    public DriverService(DriverRepository driverRepository, DriverLocationService locationService) {
        this.driverRepository = driverRepository;
        this.locationService = locationService;
    }

    public Driver register(Driver driver) {
        return driverRepository.save(driver);
    }

    public Driver getProfile(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
    }

    public Driver updateStatus(Long id, Driver.DriverStatus status, Double lat, Double lon) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setStatus(status);
        if (status == Driver.DriverStatus.ONLINE && lat != null && lon != null) {
            driver.setLatitude(lat);
            driver.setLongitude(lon);
            locationService.updateLocation(id, lat, lon);
        } else if (status == Driver.DriverStatus.OFFLINE) {
            locationService.removeDriver(id);
        }
        return driverRepository.save(driver);
    }

    public List<Long> getNearestDrivers(double lat, double lon, double radiusKm) {
        return locationService.findNearestDrivers(lat, lon, radiusKm, 5);
    }

    public Driver updateRating(Long id, double newRating) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setRating((driver.getRating() + newRating) / 2.0);
        return driverRepository.save(driver);
    }
}
