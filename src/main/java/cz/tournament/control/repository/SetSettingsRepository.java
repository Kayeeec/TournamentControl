package cz.tournament.control.repository;

import cz.tournament.control.domain.SetSettings;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data JPA repository for the SetSettings entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SetSettingsRepository extends JpaRepository<SetSettings, Long> {

}
