package com.jsystems.carsimul.repository;

import com.jsystems.carsimul.domain.Infraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfractionRepository extends JpaRepository<Infraction, Long> {
    List<Infraction> findBySessionIdOrderByOccurredAtAsc(Long sessionId);
}
