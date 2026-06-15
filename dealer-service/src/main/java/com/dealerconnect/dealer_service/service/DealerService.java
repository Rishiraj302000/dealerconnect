package com.dealerconnect.dealer_service.service;

import com.dealerconnect.dealer_service.audit.AuditPublisher;
import com.dealerconnect.dealer_service.dto.DealerRequest;
import com.dealerconnect.dealer_service.dto.DealerResponse;
import com.dealerconnect.dealer_service.entity.Dealer;
import com.dealerconnect.dealer_service.enums.DealerStatus;
import com.dealerconnect.dealer_service.exception.DuplicateResourceException;
import com.dealerconnect.dealer_service.exception.ResourceNotFoundException;
import com.dealerconnect.dealer_service.repository.DealerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DealerService {

    private final DealerRepository dealerRepository;
    private final AuditPublisher auditPublisher;

    public DealerService(DealerRepository dealerRepository, AuditPublisher auditPublisher) {
        this.dealerRepository = dealerRepository;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public DealerResponse create(DealerRequest request, String performedBy) {
        if (dealerRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateResourceException("Dealer code already exists: " + request.code());
        }
        Dealer dealer = Dealer.builder()
                .name(request.name())
                .code(request.code())
                .email(request.email())
                .phone(request.phone())
                .city(request.city())
                .status(DealerStatus.ACTIVE)
                .deleted(false)
                .build();
        Dealer saved = dealerRepository.save(dealer);
        auditPublisher.publish("DEALER_CREATED", saved.getId(), performedBy,
                "Dealer '" + saved.getName() + "' (code " + saved.getCode() + ") created");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DealerResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional(readOnly = true)
    public List<DealerResponse> getAll() {
        return dealerRepository.findAllByDeletedFalse().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DealerResponse> search(String name, String city, DealerStatus status) {
        return dealerRepository.search(name, city, status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public DealerResponse update(Long id, DealerRequest request, String performedBy) {
        Dealer dealer = findActive(id);
        if (!dealer.getCode().equals(request.code())
                && dealerRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateResourceException("Dealer code already exists: " + request.code());
        }
        dealer.setName(request.name());
        dealer.setCode(request.code());
        dealer.setEmail(request.email());
        dealer.setPhone(request.phone());
        dealer.setCity(request.city());
        Dealer saved = dealerRepository.save(dealer);
        auditPublisher.publish("DEALER_UPDATED", saved.getId(), performedBy,
                "Dealer '" + saved.getName() + "' (code " + saved.getCode() + ") updated");
        return toResponse(saved);
    }

    @Transactional
    public DealerResponse activate(Long id, String performedBy) {
        Dealer dealer = findActive(id);
        dealer.setStatus(DealerStatus.ACTIVE);
        Dealer saved = dealerRepository.save(dealer);
        auditPublisher.publish("DEALER_ACTIVATED", saved.getId(), performedBy,
                "Dealer '" + saved.getName() + "' activated");
        return toResponse(saved);
    }

    @Transactional
    public DealerResponse deactivate(Long id, String performedBy) {
        Dealer dealer = findActive(id);
        dealer.setStatus(DealerStatus.INACTIVE);
        Dealer saved = dealerRepository.save(dealer);
        auditPublisher.publish("DEALER_DEACTIVATED", saved.getId(), performedBy,
                "Dealer '" + saved.getName() + "' deactivated");
        return toResponse(saved);
    }

    @Transactional
    public void softDelete(Long id) {
        Dealer dealer = findActive(id);
        dealer.setDeleted(true);
        dealerRepository.save(dealer);
    }

    private Dealer findActive(Long id) {
        return dealerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + id));
    }

    private DealerResponse toResponse(Dealer dealer) {
        return new DealerResponse(
                dealer.getId(),
                dealer.getName(),
                dealer.getCode(),
                dealer.getEmail(),
                dealer.getPhone(),
                dealer.getCity(),
                dealer.getStatus(),
                dealer.getCreatedAt(),
                dealer.getUpdatedAt());
    }
}
