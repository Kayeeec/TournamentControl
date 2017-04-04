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
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.jopendocument.model.OpenDocument;
import org.jopendocument.sample.SpreadSheetCreation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

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
    
    public File generateAllVersusAllODS(Tournament tournament) throws FileNotFoundException, IOException{
        if(tournament instanceof AllVersusAll){
            log.debug("tournament is AllVersusAll");
        }
        final Object[][] data = new Object[6][2];
        data[0] = new Object[] { "January", 1 };
        data[1] = new Object[] { "February", 3 };
        data[2] = new Object[] { "March", 8 };
        data[3] = new Object[] { "April", 10 };
        data[4] = new Object[] { "May", 15 };
        data[5] = new Object[] { "June", 18 };

        String[] columns = new String[] { "Month", "Temp" };

        TableModel model = new DefaultTableModel(data, columns);
        String name = "tournament_" + tournament.getId().toString();
        
        File temp = File.createTempFile(name, ".ods");
        SpreadSheet.createEmpty(model).saveAs(temp);
        
        return temp;
 
    }
    
    
}
