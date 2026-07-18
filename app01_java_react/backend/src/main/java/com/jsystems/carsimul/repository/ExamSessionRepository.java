package com.jsystems.carsimul.repository;

import com.jsystems.carsimul.domain.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
}
