package com.dealerconnect.dealer_service.repository;

import com.dealerconnect.dealer_service.entity.DealerContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealerContactRepository extends JpaRepository<DealerContact, Long> {

    List<DealerContact> findAllByDealerId(Long dealerId);
}
