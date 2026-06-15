package com.dealerconnect.dealer_service.controller;

import com.dealerconnect.dealer_service.dto.DealerRequest;
import com.dealerconnect.dealer_service.dto.DealerResponse;
import com.dealerconnect.dealer_service.enums.DealerStatus;
import com.dealerconnect.dealer_service.service.DealerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dealers")
public class DealerController {

    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @PostMapping
    public ResponseEntity<DealerResponse> create(
            @RequestHeader(value = "X-Auth-User", required = false) String performedBy,
            @Valid @RequestBody DealerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealerService.create(request, performedBy));
    }

    @GetMapping
    public ResponseEntity<List<DealerResponse>> getAll() {
        return ResponseEntity.ok(dealerService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DealerResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) DealerStatus status) {
        return ResponseEntity.ok(dealerService.search(name, city, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dealerService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealerResponse> update(
            @RequestHeader(value = "X-Auth-User", required = false) String performedBy,
            @PathVariable Long id,
            @Valid @RequestBody DealerRequest request) {
        return ResponseEntity.ok(dealerService.update(id, request, performedBy));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<DealerResponse> activate(
            @RequestHeader(value = "X-Auth-User", required = false) String performedBy,
            @PathVariable Long id) {
        return ResponseEntity.ok(dealerService.activate(id, performedBy));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<DealerResponse> deactivate(
            @RequestHeader(value = "X-Auth-User", required = false) String performedBy,
            @PathVariable Long id) {
        return ResponseEntity.ok(dealerService.deactivate(id, performedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        dealerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
