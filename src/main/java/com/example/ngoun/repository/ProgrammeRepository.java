package com.example.ngoun.repository;

import com.example.ngoun.model.Programme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
    List<Programme> findByDate(LocalDate date);
}
