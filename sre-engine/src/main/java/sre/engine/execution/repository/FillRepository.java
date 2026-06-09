package sre.engine.execution.repository;

import sre.engine.execution.entity.Fill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FillRepository extends JpaRepository<Fill, Long> {
}
