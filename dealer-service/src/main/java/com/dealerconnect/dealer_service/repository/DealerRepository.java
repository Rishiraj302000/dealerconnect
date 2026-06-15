package com.dealerconnect.dealer_service.repository;

import com.dealerconnect.dealer_service.entity.Dealer;
import com.dealerconnect.dealer_service.enums.DealerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    Optional<Dealer> findByIdAndDeletedFalse(Long id);

    List<Dealer> findAllByDeletedFalse();

    boolean existsByCodeAndDeletedFalse(String code);

    /**
     * Case-insensitive search over non-deleted dealers. Any criterion left null is ignored.
     */
    @Query("""
            SELECT d FROM Dealer d
            WHERE d.deleted = false
              AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%')))
              AND (:status IS NULL OR d.status = :status)
            """)
    List<Dealer> search(@Param("name") String name,
                        @Param("city") String city,
                        @Param("status") DealerStatus status);
}
