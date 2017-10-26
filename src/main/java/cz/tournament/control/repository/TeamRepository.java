package cz.tournament.control.repository;

import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.User;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Team entity.
 */
@SuppressWarnings("unused")
public interface TeamRepository extends JpaRepository<Team,Long> {

    @Query("select team from Team team where team.user.login = ?#{principal.username}")
    List<Team> findByUserIsCurrentUser();

    @Query("select distinct team from Team team left join fetch team.members")
    List<Team> findAllWithEagerRelationships();

    @Query("select team from Team team left join fetch team.members where team.id =:id")
    Team findOneWithEagerRelationships(@Param("id") Long id);
    
    List<Team> findByUser(User user);

}
