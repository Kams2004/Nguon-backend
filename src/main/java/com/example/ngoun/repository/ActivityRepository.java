package com.example.ngoun.repository;

import com.example.ngoun.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Optional<Activity> findByName(String name);
    Optional<Activity> findByDisplayOrder(Integer displayOrder);
}
