package com.jsystems.carsimul.repository;

import com.jsystems.carsimul.domain.ExamEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamEventRepository extends JpaRepository<ExamEvent, Long> {
}
