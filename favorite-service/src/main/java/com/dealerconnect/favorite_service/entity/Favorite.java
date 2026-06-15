package com.dealerconnect.favorite_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A dealer that a user has marked as a favorite. The dealer name/code are stored as a
 * snapshot taken (via Feign) from the Dealer Service at the time the favorite is created,
 * so the favorites list is readable without calling the Dealer Service again.
 * A user cannot favorite the same dealer twice (unique username + dealerId).
 */
@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "dealer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "dealer_id", nullable = false)
    private Long dealerId;

    private String dealerName;

    private String dealerCode;

    private String category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
