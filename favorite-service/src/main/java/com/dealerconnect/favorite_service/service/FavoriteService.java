package com.dealerconnect.favorite_service.service;

import com.dealerconnect.favorite_service.audit.AuditPublisher;
import com.dealerconnect.favorite_service.client.DealerClient;
import com.dealerconnect.favorite_service.client.dto.DealerDto;
import com.dealerconnect.favorite_service.dto.FavoriteRequest;
import com.dealerconnect.favorite_service.dto.FavoriteResponse;
import com.dealerconnect.favorite_service.entity.Favorite;
import com.dealerconnect.favorite_service.exception.DuplicateResourceException;
import com.dealerconnect.favorite_service.exception.ResourceNotFoundException;
import com.dealerconnect.favorite_service.repository.FavoriteRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final DealerClient dealerClient;
    private final AuditPublisher auditPublisher;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           DealerClient dealerClient,
                           AuditPublisher auditPublisher) {
        this.favoriteRepository = favoriteRepository;
        this.dealerClient = dealerClient;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public FavoriteResponse addFavorite(String username, FavoriteRequest request) {
        // Verify the dealer exists (and is not soft-deleted) via the Dealer Service.
        DealerDto dealer = fetchDealer(request.dealerId());

        if (favoriteRepository.existsByUsernameAndDealerId(username, request.dealerId())) {
            throw new DuplicateResourceException("Dealer " + request.dealerId() + " is already in favorites");
        }

        Favorite favorite = Favorite.builder()
                .username(username)
                .dealerId(dealer.id())
                .dealerName(dealer.name())
                .dealerCode(dealer.code())
                .category(request.category())
                .build();
        Favorite saved = favoriteRepository.save(favorite);
        auditPublisher.publish("FAVORITE_ADDED", saved.getId(), username,
                "Dealer '" + saved.getDealerName() + "' (id " + saved.getDealerId() + ") added to favorites");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(String username, String category) {
        List<Favorite> favorites = StringUtils.hasText(category)
                ? favoriteRepository.findAllByUsernameAndCategory(username, category)
                : favoriteRepository.findAllByUsername(username);
        return favorites.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void removeFavorite(String username, Long id) {
        Favorite favorite = favoriteRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found with id: " + id));
        favoriteRepository.delete(favorite);
        auditPublisher.publish("FAVORITE_REMOVED", favorite.getId(), username,
                "Dealer '" + favorite.getDealerName() + "' (id " + favorite.getDealerId() + ") removed from favorites");
    }

    private DealerDto fetchDealer(Long dealerId) {
        try {
            return dealerClient.getDealerById(dealerId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Dealer not found with id: " + dealerId);
        }
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getUsername(),
                favorite.getDealerId(),
                favorite.getDealerName(),
                favorite.getDealerCode(),
                favorite.getCategory(),
                favorite.getCreatedAt());
    }
}
