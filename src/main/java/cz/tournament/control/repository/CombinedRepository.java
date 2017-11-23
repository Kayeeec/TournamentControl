package cz.tournament.control.repository;

import cz.tournament.control.domain.Combined;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Combined entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CombinedRepository extends JpaRepository<Combined, Long> {

}
