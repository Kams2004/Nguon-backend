package com.example.ngoun.repository;

import com.example.ngoun.model.ManifestationSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManifestationSiteRepository extends JpaRepository<ManifestationSite, Long> {
    Optional<ManifestationSite> findByTownTitle(String townTitle);
}
