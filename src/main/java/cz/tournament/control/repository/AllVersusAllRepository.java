package cz.tournament.control.repository;

import cz.tournament.control.domain.tournaments.AllVersusAll;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the AllVersusAll entity.
 */
@SuppressWarnings("unused")
public interface AllVersusAllRepository extends JpaRepository<AllVersusAll,Long> {

}
