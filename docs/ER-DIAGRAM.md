# DealerConnect — Entity Relationship Diagram

Each microservice owns a separate MySQL database (database-per-service). There are no
cross-database foreign keys; references that span services (for example a favorite
pointing at a dealer) are **logical** references resolved at runtime through OpenFeign.

## Databases and tables

| Database | Owning service | Tables |
|----------|----------------|--------|
| `dealerconnect_auth` | auth-service | `users` |
| `dealerconnect_dealer` | dealer-service | `dealers`, `dealer_contacts` |
| `dealerconnect_favorite` | favorite-service | `favorites` |
| `dealerconnect_audit` | audit-service | `audit_records` |

## Diagram

```mermaid
erDiagram
    USERS {
        BIGINT        id PK
        VARCHAR       name
        VARCHAR       email "unique"
        VARCHAR       password "BCrypt hash"
        VARCHAR       role "ADMIN | RELATIONSHIP_MANAGER"
        DATETIME      created_at
    }

    DEALERS {
        BIGINT        id PK
        VARCHAR       name
        VARCHAR       code "unique"
        VARCHAR       email
        VARCHAR       phone
        VARCHAR       city
        VARCHAR       status "ACTIVE | INACTIVE"
        BOOLEAN       deleted "soft-delete flag"
        DATETIME      created_at
        DATETIME      updated_at
    }

    DEALER_CONTACTS {
        BIGINT        id PK
        BIGINT        dealer_id FK
        VARCHAR       name
        VARCHAR       email
        VARCHAR       phone
        VARCHAR       designation
        DATETIME      created_at
    }

    FAVORITES {
        BIGINT        id PK
        VARCHAR       username
        BIGINT        dealer_id "logical ref to DEALERS.id"
        VARCHAR       dealer_name "snapshot via Feign"
        VARCHAR       dealer_code "snapshot via Feign"
        VARCHAR       category
        DATETIME      created_at
    }

    AUDIT_RECORDS {
        BIGINT        id PK
        VARCHAR       action
        VARCHAR       entity_type "DEALER | FAVORITE"
        BIGINT        entity_id "logical ref"
        VARCHAR       performed_by
        VARCHAR       details
        DATETIME      timestamp
    }

    DEALERS ||--o{ DEALER_CONTACTS : "has (FK dealer_id)"
    DEALERS ||..o{ FAVORITES : "referenced by (logical, via Feign)"
    DEALERS ||..o{ AUDIT_RECORDS : "audited (logical)"
    FAVORITES ||..o{ AUDIT_RECORDS : "audited (logical)"
```

Legend: solid line `||--o{` is a real foreign key within a single database; dotted line
`||..o{` is a logical, cross-service reference (no database-level foreign key).

## Relationships

- **DEALERS 1 — N DEALER_CONTACTS**: a dealer has many contacts. Real foreign key
  `dealer_contacts.dealer_id → dealers.id` (same database).
- **FAVORITES → DEALERS**: each favorite stores a `dealer_id`. The dealer lives in a
  different database, so existence is verified through the Dealer Service (Feign) before a
  favorite is created; the dealer name/code are snapshotted onto the favorite at that time.
- **AUDIT_RECORDS → DEALERS / FAVORITES**: each audit row stores `entity_type` and
  `entity_id` identifying the audited record. This is a logical reference only.

## Uniqueness and constraints

- `users.email` — unique.
- `dealers.code` — unique among non-deleted dealers (enforced in the service layer).
- `favorites (username, dealer_id)` — unique composite (a user cannot favorite the same
  dealer twice).
