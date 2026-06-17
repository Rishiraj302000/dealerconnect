package com.dealerconnect.dealer_service;

import com.dealerconnect.dealer_service.audit.AuditPublisher;
import com.dealerconnect.dealer_service.dto.DealerRequest;
import com.dealerconnect.dealer_service.dto.DealerResponse;
import com.dealerconnect.dealer_service.entity.Dealer;
import com.dealerconnect.dealer_service.enums.DealerStatus;
import com.dealerconnect.dealer_service.exception.DuplicateResourceException;
import com.dealerconnect.dealer_service.exception.ResourceNotFoundException;
import com.dealerconnect.dealer_service.repository.DealerRepository;
import com.dealerconnect.dealer_service.service.DealerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DealerService using Mockito - the repository and audit publisher are
 * mocked so the tests focus purely on the dealer business logic.
 */
class DealerServiceTest {

    @Mock
    private DealerRepository dealerRepository;
    @Mock
    private AuditPublisher auditPublisher;
    @InjectMocks
    private DealerService dealerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_savesDealer_andPublishesAudit() {
        DealerRequest request = new DealerRequest("Acme Motors", "DLR-001", "a@acme.com", "123", "Pune");
        when(dealerRepository.existsByCodeAndDeletedFalse("DLR-001")).thenReturn(false);
        Dealer saved = Dealer.builder().id(1L).name("Acme Motors").code("DLR-001")
                .status(DealerStatus.ACTIVE).deleted(false).build();
        when(dealerRepository.save(any(Dealer.class))).thenReturn(saved);

        DealerResponse response = dealerService.create(request, "admin@dc.com");

        assertEquals("Acme Motors", response.name());
        assertEquals(DealerStatus.ACTIVE, response.status());
        verify(auditPublisher).publish(eq("DEALER_CREATED"), eq(1L), eq("admin@dc.com"), anyString());
    }

    @Test
    void create_withDuplicateCode_throwsConflict() {
        DealerRequest request = new DealerRequest("Acme", "DLR-001", null, null, null);
        when(dealerRepository.existsByCodeAndDeletedFalse("DLR-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> dealerService.create(request, "admin@dc.com"));
        verify(dealerRepository, never()).save(any());
    }

    @Test
    void getById_whenExists_returnsDealer() {
        Dealer dealer = Dealer.builder().id(5L).name("Acme").code("DLR-001").status(DealerStatus.ACTIVE).build();
        when(dealerRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(dealer));

        DealerResponse response = dealerService.getById(5L);

        assertEquals(5L, response.id());
        assertEquals("Acme", response.name());
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(dealerRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dealerService.getById(99L));
    }

    @Test
    void deactivate_setsStatusInactive_andPublishesAudit() {
        Dealer dealer = Dealer.builder().id(1L).name("Acme").code("DLR-001").status(DealerStatus.ACTIVE).build();
        when(dealerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(dealer));
        when(dealerRepository.save(any(Dealer.class))).thenAnswer(inv -> inv.getArgument(0));

        DealerResponse response = dealerService.deactivate(1L, "admin@dc.com");

        assertEquals(DealerStatus.INACTIVE, response.status());
        verify(auditPublisher).publish(eq("DEALER_DEACTIVATED"), eq(1L), eq("admin@dc.com"), anyString());
    }

    @Test
    void softDelete_setsDeletedFlag() {
        Dealer dealer = Dealer.builder().id(1L).name("Acme").code("DLR-001")
                .status(DealerStatus.ACTIVE).deleted(false).build();
        when(dealerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(dealer));

        dealerService.softDelete(1L);

        assertTrue(dealer.isDeleted());
        verify(dealerRepository).save(dealer);
    }

    @Test
    void activate_setsStatusActive_andPublishesAudit() {
        Dealer dealer = Dealer.builder().id(1L).name("Acme").code("DLR-001").status(DealerStatus.INACTIVE).build();
        when(dealerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(dealer));
        when(dealerRepository.save(any(Dealer.class))).thenAnswer(inv -> inv.getArgument(0));

        DealerResponse response = dealerService.activate(1L, "admin@dc.com");

        assertEquals(DealerStatus.ACTIVE, response.status());
        verify(auditPublisher).publish(eq("DEALER_ACTIVATED"), eq(1L), eq("admin@dc.com"), anyString());
    }

    @Test
    void update_changesFields_andPublishesAudit() {
        Dealer existing = Dealer.builder().id(1L).name("Acme").code("DLR-001")
                .city("Pune").status(DealerStatus.ACTIVE).build();
        when(dealerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(dealerRepository.save(any(Dealer.class))).thenAnswer(inv -> inv.getArgument(0));
        DealerRequest request = new DealerRequest("Acme Motors", "DLR-001", "a@acme.com", "999", "Mumbai");

        DealerResponse response = dealerService.update(1L, request, "admin@dc.com");

        assertEquals("Acme Motors", response.name());
        assertEquals("Mumbai", response.city());
        verify(auditPublisher).publish(eq("DEALER_UPDATED"), eq(1L), eq("admin@dc.com"), anyString());
    }

    @Test
    void getAll_returnsActiveDealers() {
        Dealer dealer = Dealer.builder().id(1L).name("Acme").code("DLR-001").status(DealerStatus.ACTIVE).build();
        when(dealerRepository.findAllByDeletedFalse()).thenReturn(java.util.List.of(dealer));

        assertEquals(1, dealerService.getAll().size());
    }
}
