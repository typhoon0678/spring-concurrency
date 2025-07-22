package dev.typhoon.spring_concurrency.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import dev.typhoon.spring_concurrency.model.entity.Ticket;
import jakarta.persistence.LockModeType;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Optional<Ticket> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.name = :name")
    Optional<Ticket> findByNameWithPessimisticLock(String name);
}
