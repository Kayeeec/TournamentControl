package cz.tournament.control.repository;

import cz.tournament.control.domain.Tournament;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Tournament entity.
 */
@SuppressWarnings("unused")
public interface TournamentRepository extends JpaRepository<Tournament,Long> {

    @Query("select tournament from Tournament tournament where tournament.user.login = ?#{principal.username}")
    List<Tournament> findByUserIsCurrentUser();

    @Query("select distinct tournament from Tournament tournament left join fetch tournament.participants")
    List<Tournament> findAllWithEagerRelationships();

    @Query("select tournament from Tournament tournament left join fetch tournament.participants where tournament.id =:id")
    Tournament findOneWithEagerRelationships(@Param("id") Long id);

}