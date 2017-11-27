package cz.tournament.control.repository;

import cz.tournament.control.domain.Combined;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the Combined entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CombinedRepository extends JpaRepository<Combined, Long> {

    @Query("select combined from Combined combined where combined.user.login = ?#{principal.username}")
    Page<Combined> findByUserIsCurrentUser(Pageable pageable);

}
