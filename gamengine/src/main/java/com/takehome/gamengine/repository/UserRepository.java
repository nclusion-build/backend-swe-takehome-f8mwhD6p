package com.takehome.gamengine.repository;

import com.takehome.gamengine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    List<User> findTop3ByOrderByTotalWinsDesc();

    // Native query for efficiency. Note: H2 syntax for division might differ
    // We use a safe division (CASE WHEN) to avoid divide-by-zero errors.
    @Query(value = "SELECT *, " +
                   "(CASE WHEN total_wins > 0 THEN CAST(total_moves_made_in_wins AS DOUBLE) / total_wins ELSE NULL END) AS efficiency " +
                   "FROM users " +
                   "WHERE total_wins > 0 " +
                   "ORDER BY efficiency ASC " +
                   "LIMIT 3", nativeQuery = true)
    List<User> findTop3ByEfficiency();
}