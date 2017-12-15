package cz.tournament.control.service.fileGenerator;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.domain.AllVersusAll;
import cz.tournament.control.service.util.EvaluationParticipant;
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
public class CombinedFileGeneratorService extends FileGeneratorService {
    private final String[] GROUP_PARTICIPANT_EVALUATION_COLS = new String[] {"Group", "Rank", "Participant", "Wins", "Loses", "Ties",
                       "Participants total score", "Rivals total score", "Points" };
        
    private final AllVersusAllFileGeneratorService allVersusAllFileGeneratorService;
    private final SwissFileGeneratorService swissFileGeneratorService;
    private final EliminationFileGeneratorService eliminationFileGeneratorService;

    public CombinedFileGeneratorService(AllVersusAllFileGeneratorService allVersusAllFileGeneratorService, SwissFileGeneratorService swissFileGeneratorService, EliminationFileGeneratorService eliminationFileGeneratorService) {
        this.allVersusAllFileGeneratorService = allVersusAllFileGeneratorService;
        this.swissFileGeneratorService = swissFileGeneratorService;
        this.eliminationFileGeneratorService = eliminationFileGeneratorService;
    }
    
    /**
     * number of sheets =  4 + numberOfGroups:
     *         1 - participant eval
     *        +1 - group participant evals
     * + #groups - matches for each group
     *       + 1 - playoff participant eval
     *       + 1 - playoff matches
     * 
     * @param tournament
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public File generateSpreadSheet(Combined tournament) throws FileNotFoundException, IOException {
        String name = "combined" + tournament.getName();
        File temp = File.createTempFile(name, ".ods");
        SpreadSheet spreadSheet = SpreadSheet.create(4 + tournament.getNumberOfGroups(), 1, 1);
        
        //participant evaluation 
        TableModel participantEvaluation_model = getParticipantEvaluationModel(tournament.getRankedEvaluation());
        spreadSheet.getSheet(0).setName("Participant Evaluation");
        spreadSheet.getSheet(0).merge(participantEvaluation_model, 0, 0, true);
        
        //group participant evaluation 
        TableModel groupParticipantEvaluation_model = getGroupParticipantEvaluationModel(tournament);
        spreadSheet.getSheet(1).setName("Group Participant Evaluations");
        spreadSheet.getSheet(1).merge(groupParticipantEvaluation_model, 0, 0, true);
        
        //matches sheet of each group
        List<Tournament> groups = new ArrayList<>(tournament.getGroups());
        Collections.sort(groups, (g1, g2) -> g1.getName().compareTo(g2.getName()));
        int sheetIndex = 2;
        for (Tournament group : groups) {
            TableModel groupTableModel = getMatchesTableModel(group, tournament.getInGroupTournamentType());
            spreadSheet.getSheet(sheetIndex).setName(group.getName()+" - matches");
            spreadSheet.getSheet(sheetIndex).merge(groupTableModel, 0, 0, true);
            sheetIndex++;
        }
        
        //playoff
        TableModel playoffParticipantEvalTableModel = getParticipantEvaluationModel(tournament.getPlayoff().getRankedEvaluation());
        spreadSheet.getSheet(sheetIndex).setName("Playoff - participant evaluation");
        spreadSheet.getSheet(sheetIndex).merge(playoffParticipantEvalTableModel, 0, 0, true);
        
        TableModel playoffMatchesTableModel = getMatchesTableModel(tournament.getPlayoff(), tournament.getPlayoffType());
        spreadSheet.getSheet(sheetIndex+1).setName("Playoff - matches");
        spreadSheet.getSheet(sheetIndex+1).merge(playoffMatchesTableModel, 0, 0, true);
        
        return spreadSheet.saveAs(temp);
    }

    private TableModel getGroupParticipantEvaluationModel(Combined tournament) {
        Object[][] groupParticipantEvaluation_data = getGroupParticipantEvaluationData(tournament);
        return new DefaultTableModel(groupParticipantEvaluation_data, GROUP_PARTICIPANT_EVALUATION_COLS);
    }

    private Object[][] getGroupParticipantEvaluationData(Combined tournament) {
        List<Tournament> groups = new ArrayList<>(tournament.getGroups());
        Collections.sort(groups, (g1, g2) -> g1.getName().compareTo(g2.getName()));
        List<Object[]> dataList = new ArrayList<>();
        
        for (Tournament group : groups) {
            List<EvaluationParticipant> rankedEvaluation = group.getRankedEvaluation();
            for (EvaluationParticipant eval : rankedEvaluation) {
                dataList.add(new Object[] {group.getName(), eval.rank, eval.participant.getName(), 
                eval.wins,  eval.loses,      eval.ties, 
                eval.score, eval.rivalScore, eval.total});
            }
        }
        return dataList.toArray(new Object[][] {});
    }

    private TableModel getMatchesTableModel(Tournament group, TournamentType inGroupTournamentType) throws IOException {
        switch (inGroupTournamentType) {
            case ALL_VERSUS_ALL:
                return allVersusAllFileGeneratorService.getMatchModel((AllVersusAll) group);
            case SWISS:
                return swissFileGeneratorService.getMatchModel((Swiss) group);
            default://elimination
                return eliminationFileGeneratorService.getMatchModel((Elimination) group);
        }
    }
}
