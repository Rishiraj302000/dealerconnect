package com.dealerconnect.favorite_service.controller;

import com.dealerconnect.favorite_service.dto.FavoriteRequest;
import com.dealerconnect.favorite_service.dto.FavoriteResponse;
import com.dealerconnect.favorite_service.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The owner of each favorite is taken from the X-Auth-User header that the API Gateway
 * injects after validating the JWT, so favorites are always scoped to the calling user.
 */
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public ResponseEntity<FavoriteResponse> add(@RequestHeader("X-Auth-User") String username,
                                                @Valid @RequestBody FavoriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteService.addFavorite(username, request));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> view(@RequestHeader("X-Auth-User") String username,
                                                       @RequestParam(required = false) String category) {
        return ResponseEntity.ok(favoriteService.getFavorites(username, category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@RequestHeader("X-Auth-User") String username,
                                       @PathVariable Long id) {
        favoriteService.removeFavorite(username, id);
        return ResponseEntity.noContent().build();
    }
}
