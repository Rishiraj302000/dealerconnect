package com.dealerconnect.favorite_service.repository;

import com.dealerconnect.favorite_service.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findAllByUsername(String username);

    List<Favorite> findAllByUsernameAndCategory(String username, String category);

    Optional<Favorite> findByIdAndUsername(Long id, String username);

    boolean existsByUsernameAndDealerId(String username, Long dealerId);
}
