package com.pixeltribe.forumsys.reporttype.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportTypeRepository extends JpaRepository<ReportType, Integer> {

    Optional<ReportType> findByRpiType(String rpiType);

}
