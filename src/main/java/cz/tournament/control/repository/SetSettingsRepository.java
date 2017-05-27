package cz.tournament.control.repository;

import cz.tournament.control.domain.SetSettings;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the SetSettings entity.
 */
@SuppressWarnings("unused")
public interface SetSettingsRepository extends JpaRepository<SetSettings,Long> {

}