/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.repository.UserRepository;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Karolina Bozkova
 */
@Service
@Transactional
public class FileGeneratorService {
    private final Logger log = LoggerFactory.getLogger(FileGeneratorService.class);

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;

    public FileGeneratorService(UserRepository userRepository, TournamentRepository tournamentRepository) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
    }
    
    public File getGeneratedFile(Tournament tournament){
        if(tournament instanceof AllVersusAll){
            log.debug("tournament is AllVersusAll");
        }
        File file = new File(getClass().getClassLoader().getResource("pokus.ods").getFile());

        return file;
    }
    
    
}
