package com.example.medbook.controller;

import com.example.medbook.dto.response.*;
import com.example.medbook.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = {"https://medbooking-client-flax.vercel.app", "http://localhost:3000"})
public class PublicServiceController {

    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Integer id) {
        ServiceResponse service = serviceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/{serviceId}/doctors")
    public ResponseEntity<List<DoctorProfileResponse>> getDoctorsByService(@PathVariable Integer serviceId) {
        List<DoctorProfileResponse> doctors = doctorService.getDoctorsByService(serviceId);
        return ResponseEntity.ok(doctors);
    }
}
