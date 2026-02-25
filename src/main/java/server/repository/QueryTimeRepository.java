package server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entity.QueryTimeEntity;

@Repository
public interface QueryTimeRepository extends JpaRepository<QueryTimeEntity, String> {
}
