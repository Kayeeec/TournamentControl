package cz.tournament.control.service.fileGenerator;

import cz.tournament.control.service.util.EvaluationParticipant;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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
    
    private final String[] PARTICIPANT_EVALUATION_COLS = new String[] { "Rank", "Participant", "Wins", "Loses", "Ties",
                       "Participants total score", "Rivals total score", "Points" };
    
    public FileGeneratorService() {
    }

    public TableModel getParticipantEvaluationModel(List<EvaluationParticipant> participantEvaluation){
//        List<EvaluationParticipant> participantEvaluation = tournament.getRankedEvaluation();
        
        Object[][] participantEvaluation_data = getParticipantEvaluationData(participantEvaluation);
        return new DefaultTableModel(participantEvaluation_data, PARTICIPANT_EVALUATION_COLS);
    }
    
    /**
     * Converts list of EvaluationParticipant objects to Object[][]
     * object: {rank, participant_name, wins, loses, ties, score, rivalScore, total}
     * 
     * @param participantEvaluation
     * @return 
     */
    private Object[][] getParticipantEvaluationData(List<EvaluationParticipant> participantEvaluation) {
        Object[][] data = new Object[participantEvaluation.size()][PARTICIPANT_EVALUATION_COLS.length];
        
        for (int i = 0; i < participantEvaluation.size(); i++) {
            EvaluationParticipant eval = participantEvaluation.get(i);
            data[i] = new Object[] {eval.rank, eval.participant.getName(), 
                eval.wins,  eval.loses,      eval.ties, 
                eval.score, eval.rivalScore, eval.total};
        }
        return data;
    }
    
    
}
