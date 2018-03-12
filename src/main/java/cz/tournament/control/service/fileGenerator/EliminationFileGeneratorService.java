package cz.tournament.control.service.fileGenerator;

import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.enumeration.EliminationType;
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
public class EliminationFileGeneratorService extends FileGeneratorService {
    
    public static final String WINNER = "";
//    public static final String WINNER = "winner bracket match";
    public static final String LOOSER = "looser bracket match";
    public static final String FINAL = "final match";
    public static final String BRONZE = "bronze match";
    public static final String SECOND_FINAL = "second final match";
    

    public EliminationFileGeneratorService() {
    }
    
    public File generateSpreadSheet(Elimination tournament) throws FileNotFoundException, IOException {
        String name = "elimination" + tournament.getName();
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

    public TableModel getMatchModel(Elimination tournament) {
        if (tournament.getType() == EliminationType.SINGLE) {
            return getSingleMatchModel(tournament);
        }
        return getDoubleMatchModel(tournament);
    }
    
    private boolean nullSafeIsBye(Participant rival){
        if(rival == null) return false;
        return rival.isBye();
    }
    
    /**
     * determining 
     *      final match: period == N - 1
     *      bronze match: period == N
     * no need to translate round 
     * BYE matches ignored 
     * matches with null rival(s) NOT ignored
     * @param tournament
     * @return 
     */
    private TableModel getSingleMatchModel(Elimination tournament) {
        List<Game> matches = new ArrayList<>(tournament.getMatches());
        int N = tournament.getN();
        
        Collections.sort(matches, (o1, o2) -> {
            int compare = roundDeterminant(o1, N).compareTo(roundDeterminant(o2, N));
            if (compare == 0) {
                compare = o1.getPeriod().compareTo(o2.getPeriod());
            }
            return compare;
        });
        
        List<Object[]> dataList = new ArrayList<>();
        int maxSetCount = 0;
        
        for (Game match : matches) {
//            if (!match.getRivalA().isBye() && !match.getRivalB().isBye()) { //rival can be null
            if (!nullSafeIsBye(match.getRivalA()) && !nullSafeIsBye(match.getRivalB())) { //rival can be null
                List<Object> objList = new ArrayList<>();
                objList.add(singleMatchDeterminant(match, N));
                objList.add(match.getRound());
//                objList.add(match.getRivalA().getName());
//                objList.add(match.getRivalB().getName());
                objList.add(nullSafeGetRivalName(match.getRivalA()));
                objList.add(nullSafeGetRivalName(match.getRivalB()));

                maxSetCount = Math.max(maxSetCount, match.getSets().size());

                for (GameSet set : match.getSets()) {
                    objList.add(set.getScoreA());
                    objList.add(set.getScoreB());
                }

                dataList.add(objList.toArray());
            }
        }
        
        String[] match_cols = getMatchCols(maxSetCount);
        Object[][] data = dataList.toArray(new Object[][] {});
        return new DefaultTableModel(data, match_cols);
    }
    
    private String singleMatchDeterminant(Game match, int N){
        if(match.getPeriod() == N-1) return FINAL;
        if(match.getPeriod() == N) return BRONZE;
        return WINNER;
    }
    
    private TableModel getDoubleMatchModel(Elimination tournament) {
        List<Game> matches = new ArrayList<>(tournament.getMatches());
        int N = tournament.getN();
        
        Collections.sort(matches, (o1, o2) -> {
            int compare = roundDeterminant(o1, N).compareTo(roundDeterminant(o2, N));
            if (compare == 0) {
                compare = o1.getPeriod().compareTo(o2.getPeriod());
            }
            return compare;
        });
        
        List<Object[]> dataList = new ArrayList<>();
        int maxSetCount = 0;
        
        for (Game match : matches) {
            if (!match.getRivalA().isBye() && !match.getRivalB().isBye()) {
                List<Object> objList = new ArrayList<>();
                objList.add(doubleMatchDeterminant(match, N));
                objList.add(roundDeterminant(match, N));
                objList.add(match.getRivalA().getName());
                objList.add(match.getRivalB().getName());

                maxSetCount = Math.max(maxSetCount, match.getSets().size());

                for (GameSet set : match.getSets()) {
                    objList.add(set.getScoreA());
                    objList.add(set.getScoreB());
                }

                dataList.add(objList.toArray());
            }
        }
        
        String[] match_cols = getMatchCols(maxSetCount);
        Object[][] data = dataList.toArray(new Object[][] {});
        return new DefaultTableModel(data, match_cols);
    }
    
    /**
     * P: 0..N-1: winner
     * -R: looser
     * R: log2(N)+2 : second final 
     * P: 2N - 2 : final match
     * P: 2N - 1: bronze 
     * 
     * @param match
     * @param N
     * @return 
     */
    private String doubleMatchDeterminant(Game match, int N){
        if(match.getRound() < 0) return LOOSER;
        if(match.getRound() > log(2,N)+1) return SECOND_FINAL;
        if (match.getPeriod() == (2*N) - 2) {
            return FINAL;
        }
        if (match.getPeriod() == (2*N) - 1) {
            return BRONZE;
        }
        //P: 0..N-1: winner
        return WINNER;
    }
    
    static int log(int base, int x){
        return (int) (Math.log(x) / Math.log(base));
    }
    
    private Integer roundDeterminant(Game match, int N){
        if(match.getRound() > 0){ // 1, 2, 3...
            return match.getRound();
        }
        if(match.getRound() < -20){ //-25, -30...
            return (int) Math.floor(match.getRound() * (-1) / 10.);
        }
        //-15, -20
        return 1;
    }
    
    private String[] getMatchCols(int maxSetCount) {
        List<String> header = new ArrayList<>();
        header.add("");
        header.add("Round");
        header.add("Rival A");
        header.add("Rival B");
        for (int i = 0; i < maxSetCount; i++) {
            header.add("Score A - set "+(i+1));
            header.add("Score B - set "+(i+1));
        }
        return header.toArray(new String[0]); //new optimizations
    }

    private String nullSafeGetRivalName(Participant rival) {
        if(rival == null)return "-";
        return rival.getName();
    }
    
    
}
