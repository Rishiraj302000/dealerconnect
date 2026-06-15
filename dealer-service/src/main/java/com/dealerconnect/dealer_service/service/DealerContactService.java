package com.dealerconnect.dealer_service.service;

import com.dealerconnect.dealer_service.audit.AuditPublisher;
import com.dealerconnect.dealer_service.dto.DealerContactRequest;
import com.dealerconnect.dealer_service.dto.DealerContactResponse;
import com.dealerconnect.dealer_service.entity.Dealer;
import com.dealerconnect.dealer_service.entity.DealerContact;
import com.dealerconnect.dealer_service.exception.ResourceNotFoundException;
import com.dealerconnect.dealer_service.repository.DealerContactRepository;
import com.dealerconnect.dealer_service.repository.DealerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DealerContactService {

    private final DealerContactRepository contactRepository;
    private final DealerRepository dealerRepository;
    private final AuditPublisher auditPublisher;

    public DealerContactService(DealerContactRepository contactRepository,
                                DealerRepository dealerRepository,
                                AuditPublisher auditPublisher) {
        this.contactRepository = contactRepository;
        this.dealerRepository = dealerRepository;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public DealerContactResponse create(DealerContactRequest request, String performedBy) {
        Dealer dealer = requireDealer(request.dealerId());
        DealerContact contact = DealerContact.builder()
                .dealer(dealer)
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .designation(request.designation())
                .build();
        DealerContact saved = contactRepository.save(contact);
        auditPublisher.publishContact("CONTACT_ADDED", saved.getId(), performedBy,
                "Contact '" + saved.getName() + "' added to dealer '" + dealer.getName() + "'");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DealerContactResponse getById(Long id) {
        return toResponse(findContact(id));
    }

    @Transactional(readOnly = true)
    public List<DealerContactResponse> getByDealer(Long dealerId) {
        requireDealer(dealerId);
        return contactRepository.findAllByDealerId(dealerId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public DealerContactResponse update(Long id, DealerContactRequest request) {
        DealerContact contact = findContact(id);
        if (!contact.getDealer().getId().equals(request.dealerId())) {
            contact.setDealer(requireDealer(request.dealerId()));
        }
        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());
        contact.setDesignation(request.designation());
        return toResponse(contactRepository.save(contact));
    }

    @Transactional
    public void delete(Long id, String performedBy) {
        DealerContact contact = findContact(id);
        String name = contact.getName();
        contactRepository.delete(contact);
        auditPublisher.publishContact("CONTACT_REMOVED", id, performedBy,
                "Contact '" + name + "' removed");
    }

    private Dealer requireDealer(Long dealerId) {
        return dealerRepository.findByIdAndDeletedFalse(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + dealerId));
    }

    private DealerContact findContact(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }

    private DealerContactResponse toResponse(DealerContact contact) {
        return new DealerContactResponse(
                contact.getId(),
                contact.getDealer().getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getDesignation(),
                contact.getCreatedAt());
    }
}
