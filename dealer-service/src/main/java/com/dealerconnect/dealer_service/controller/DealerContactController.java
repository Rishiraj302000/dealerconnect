package com.dealerconnect.dealer_service.controller;

import com.dealerconnect.dealer_service.dto.DealerContactRequest;
import com.dealerconnect.dealer_service.dto.DealerContactResponse;
import com.dealerconnect.dealer_service.service.DealerContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/contacts")
public class DealerContactController {

    private final DealerContactService contactService;

    public DealerContactController(DealerContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<DealerContactResponse> create(
            @RequestHeader(value = "X-Auth-User", required = false) String performedBy,
            @Valid @RequestBody DealerContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.create(request, performedBy));
    }

    @GetMapping
    public ResponseEntity<List<DealerContactResponse>> getByDealer(@RequestParam Long dealerId) {
        return ResponseEntity.ok(contactService.getByDealer(dealerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealerContactResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealerContactResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody DealerContactRequest request) {
        return ResponseEntity.ok(contactService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-Auth-User", required = false) String performedBy,
            @PathVariable Long id) {
        contactService.delete(id, performedBy);
        return ResponseEntity.noContent().build();
    }
}
