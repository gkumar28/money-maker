package execution.engine.repository;

import execution.engine.entity.Fill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FillRepository extends JpaRepository<Long, Fill> {
}
