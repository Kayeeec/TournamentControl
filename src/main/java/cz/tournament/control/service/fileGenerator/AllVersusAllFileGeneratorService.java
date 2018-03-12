package cz.tournament.control.service.fileGenerator;

import cz.tournament.control.domain.AllVersusAll;
import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Karolina Bozkova
 */
@Service
@Transactional
public class AllVersusAllFileGeneratorService extends FileGeneratorService {

    public AllVersusAllFileGeneratorService() {
    }
    
    public File generateSpreadSheet(AllVersusAll tournament) throws FileNotFoundException, IOException {
        String name = "allVersusAll_" + tournament.getName();
        File temp = File.createTempFile(name, ".ods");
        SpreadSheet spreadSheet = SpreadSheet.create(2, 1, 1);
        
        //participant evaluation 
        TableModel participantEvaluation_model = getParticipantEvaluationModel(tournament.getRankedEvaluation());
        spreadSheet.getSheet(0).setName("Participant Evaluation");
        spreadSheet.getSheet(0).merge(participantEvaluation_model, 0, 0, true);
        
        //match evaluation
        TableModel matchEvaluation_model = getMatchModel(tournament);
        spreadSheet.getSheet(1).setName("Matches");
        spreadSheet.getSheet(1).merge(matchEvaluation_model, 0, 0, true);
        
        return spreadSheet.saveAs(temp);
    }
    
    public TableModel getMatchModel(AllVersusAll tournament) {
        List<Game> matches = new ArrayList<>(tournament.getMatches());
        Collections.sort(matches, Game.PeriodRoundComparator);
        
        List<Object[]> dataList = new ArrayList<>();
        int maxSetCount = 0;
        
        for (int i = 0; i < matches.size(); i++) {
            Game match = matches.get(i);
            List<Object> objList = new ArrayList<>();
            objList.add(match.getPeriod());
            objList.add(match.getRound());
            objList.add(match.getRivalA().getName());
            objList.add(match.getRivalB().getName());
            
            maxSetCount = Math.max(maxSetCount, match.getSets().size());
            
            for (GameSet set : match.getSets()) {
                objList.add(set.getScoreA());
                objList.add(set.getScoreB());
            }
            
            dataList.add(objList.toArray());
        }
        
        String[] match_cols = getMatchCols(maxSetCount);
        Object[][] data = dataList.toArray(new Object[][] {});
        return new DefaultTableModel(data, match_cols);
    }

    private String[] getMatchCols(int maxSetCount) {
        List<String> header = new ArrayList<>();
        header.add("Period");
        header.add("Round");
        header.add("Rival A");
        header.add("Rival B");
        for (int i = 0; i < maxSetCount; i++) {
            header.add("Score A - set "+(i+1));
            header.add("Score B - set "+(i+1));
        }
        return header.toArray(new String[0]); //new optimizations
    }
    
}
