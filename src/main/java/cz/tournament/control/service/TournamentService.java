/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
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
public class TournamentService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;

    public TournamentService(UserRepository userRepository, TournamentRepository tournamentRepository) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
    }
    
    public Tournament createTournament(Tournament tournament){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        tournament.setUser(creator);
        
        Tournament tmp;
        
//      --------------use the right constructor
        if(tournament.getTournamentType().equals(TournamentType.ALL_VERSUS_ALL)){
            tmp = new AllVersusAll();
        }
        else { tmp = new Tournament();}
        
//      --------------use the right constructor - END
        tmp.setMatches(tournament.getMatches());
        tmp.setName(tournament.getName());
        tmp.setNote(tournament.getNote());
        tmp.setParticipants(tournament.getParticipants());
        tmp.setPointsForLosing(tournament.getPointsForLosing());
        tmp.setPointsForTie(tournament.getPointsForTie());
        tmp.setPointsForWinning(tournament.getPointsForWinning());
        tmp.setTournamentType(tournament.getTournamentType());
        
        Tournament result = tournamentRepository.save(tmp);
        log.debug("TOUTNAMENT_SERVICE: Created Tournament: {}", result);
        return result;
        
        
    }
    
    
}
