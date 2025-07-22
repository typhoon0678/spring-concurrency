package dev.typhoon.spring_concurrency.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.typhoon.spring_concurrency.model.entity.TicketVersion;

public interface TicketVersionRepository extends JpaRepository<TicketVersion, Long> {
    
    Optional<TicketVersion> findByName(String name);
}
