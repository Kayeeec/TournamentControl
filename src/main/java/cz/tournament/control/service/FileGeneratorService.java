/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.repository.UserRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
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

    private String[] getCrossColumns(AllVersusAll tournament) {
        List<String> result = new ArrayList<>();
        result.add("");
        for (Participant participant : tournament.getParticipants()) {
            if (participant.getPlayer() != null) {
                result.add(participant.getPlayer().getName());
            } else {
                result.add(participant.getTeam().getName());
            }
        }
        return result.toArray(new String[0]);
    }

    public File generateAllVersusAllODS(AllVersusAll tournament) throws FileNotFoundException, IOException {
        String name = "tournament_" + tournament.getId().toString();
        File templateFile = new File(getClass().getResource("/allVersusAllTemplate.ods").getFile());
        File temp = File.createTempFile(name, ".ods");

        final Object[][] classicData = getClassicData(tournament);
//        final Object[][] crossData = getCrossData(tournament);
        

        /* set columns */
        String[] classicColumns = new String[]{"Rank", "Participant", "Matches", "Wins", "Loses", "Ties", "Score", "Points"};
//        String[] crossColumns = getCrossColumns(tournament);
        String[] matchesColumns = new String[]{"Round", "Rival A", ":", "Rival B", "Score A", ":", "Score B"};

        TableModel classicModel = new DefaultTableModel(classicData, classicColumns);
//        TableModel cross = new DefaultTableModel(crossData, crossColumns);
        TableModel[] matchModels = getMatchModels(tournament, matchesColumns);

        /* create spreadsheet document */
        int cols = Integer.max(10, tournament.getParticipants().size() + 6);
        int rows = Integer.max(tournament.getParticipants().size() + 1, tournament.getMatches().size() + 2);
        SpreadSheet spreadSheet = SpreadSheet.createFromFile(templateFile);
        /* set sheet names */
        spreadSheet.getSheet(0).setName("classic table");
        spreadSheet.getSheet(1).setName("matches");
        
        /* merge table model into appropriate sheet */
        spreadSheet.getSheet(0).merge(classicModel, 0, 0, true);
        Sheet matches = spreadSheet.getSheet("matches");
        int headingRow = 0;
        int rs = tournament.getMatches().size()/tournament.getNumberOfMutualMatches();
        for (int p = 0; p < matchModels.length; p++) {
            String heading = "Period " + (p+1);
            matches.setValueAt(heading, 0 , headingRow);
            matches.merge(matchModels[p], 0, headingRow+1, true);
            
            headingRow += rs + 2;
        }

        /* save spreadsheet as file and return it */
        return spreadSheet.saveAs(temp);
    }

    private List<EvalClassic> evaluateParticipants(AllVersusAll tournament) {
        int participants = tournament.getParticipants().size();
        List<Participant> participantList = new ArrayList<>(tournament.getParticipants());
        List<EvalClassic> evals = new ArrayList<>();

        //evaluate
        for (int i = 0; i < participants; i++) {
            EvalClassic eval = new EvalClassic();
            eval.setParticipant(participantList.get(i));
            for (Game match : tournament.getMatches()) {
                if (match.isFinished()) {
                    if (match.getRivalA().equals(participantList.get(i))) {
                        if (match.getSumOfScoresA() > match.getSumOfScoresB()) {
                            eval.wins += 1;
                        }
                        if (match.getSumOfScoresA() < match.getSumOfScoresB()) {
                            eval.loses += 1;
                        }
                        if (match.getSumOfScoresA() == match.getSumOfScoresB()) {
                            eval.ties += 1;
                        }
                        eval.scoreHis += match.getSumOfScoresA();
                        eval.scoreRival += match.getSumOfScoresB();
                        eval.matches += 1;
                    }
                    if (match.getRivalB().equals(participantList.get(i))) {
                        if (match.getSumOfScoresA() > match.getSumOfScoresB()) {
                            eval.loses += 1;
                        }
                        if (match.getSumOfScoresA() < match.getSumOfScoresB()) {
                            eval.wins += 1;
                        }
                        if (match.getSumOfScoresA() == match.getSumOfScoresB()) {
                            eval.ties += 1;
                        }
                        eval.scoreHis += match.getSumOfScoresB();
                        eval.scoreRival += match.getSumOfScoresA();
                        eval.matches += 1;
                    }
                }
            }
            eval.points = countPoints(tournament, eval.wins, eval.loses, eval.ties);
            evals.add(eval);
        }

        //sort
        Collections.sort(evals, new Comparator<EvalClassic>() {
            @Override
            public int compare(EvalClassic p1, EvalClassic p2) {
                return Double.compare(p1.points, p2.points);
            }
        });

        //determine rank 
        int rank = 1;
        EvalClassic eval = evals.get(participants - 1);
        eval.rank = rank;
        for (int i = 1; i < participants; i++) {
            EvalClassic prev = eval;
            eval = evals.get(participants - 1 - i);
            if (prev.points != eval.points) {
                rank += 1;
            }
            eval.rank = rank;
        }

        return evals;
    }

    private Object[][] getClassicData(AllVersusAll tournament) {
        int participants = tournament.getParticipants().size();
        Object[][] result = new Object[participants][8];
        List<EvalClassic> evals = evaluateParticipants(tournament);

        //fill result
        for (int i = 0; i < participants; i++) {
            EvalClassic eval = evals.get(participants - 1 - i);
            result[i] = new Object[]{eval.rank, eval.participant.getName(), eval.matches, eval.wins, eval.loses, eval.ties, eval.scoreHis, eval.points};
        }

        return result;

    }

    private Double countPoints(AllVersusAll tournament, int wins, int loses, int ties) {
        return (wins * tournament.getPointsForWinning()) + (ties * tournament.getPointsForTie()) - (loses * tournament.getPointsForLosing());
    }
    
   


/*    {"Round", "Rival A", ":", "Rival B", "Score A", ":", "Score B"} */
    private TableModel[] getMatchModels(AllVersusAll tournament, String[] matchesColumns) {
        int periods = tournament.getNumberOfMutualMatches();
        List<Game> matches = new ArrayList<>(tournament.getMatches());
        int rows = matches.size()/periods;
        
        //sort matches by period, then round 
        Collections.sort(matches, Game.PeriodRoundComparator);
        
        int position = 0;
        TableModel[] period = new TableModel[periods];
        for (int p = 0; p < periods; p++) {
            Object[][] data = new Object[rows][7];
            for (int r = 0; r < rows; r++) {
                Game m = matches.get(position);
                data[r] = new Object[]{m.getRound(), m.getRivalA().getName(), ":", m.getRivalB().getName(), m.getSumOfScoresA(), ":", m.getSumOfScoresB()};
                position += 1;
            }
            period[p] = new DefaultTableModel(data, matchesColumns);
        }
        return period;
    }
}
