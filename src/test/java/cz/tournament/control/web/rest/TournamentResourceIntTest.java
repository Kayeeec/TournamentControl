package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;

import cz.tournament.control.domain.Tournament;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.service.TournamentService;
/**
 * Test class for the TournamentResource REST controller.
 *
 * @see TournamentResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournamentControlApp.class)
public class TournamentResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    private static final Integer DEFAULT_POINTS_FOR_WINNING = 1;
    private static final Integer UPDATED_POINTS_FOR_WINNING = 2;

    private static final Integer DEFAULT_POINTS_FOR_LOSING = 1;
    private static final Integer UPDATED_POINTS_FOR_LOSING = 2;

    private static final Integer DEFAULT_POINTS_FOR_TIE = 1;
    private static final Integer UPDATED_POINTS_FOR_TIE = 2;

    private static final TournamentType DEFAULT_TOURNAMENT_TYPE = TournamentType.ALL_VERSUS_ALL;
    private static final TournamentType UPDATED_TOURNAMENT_TYPE = TournamentType.PLAY_OFF;

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TournamentService tournamentService;
    

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restTournamentMockMvc;

    private Tournament tournament;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TournamentResource tournamentResource = new TournamentResource(tournamentRepository, userRepository, tournamentService);
        this.restTournamentMockMvc = MockMvcBuilders.standaloneSetup(tournamentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tournament createEntity(EntityManager em) {
        Tournament tournament = new Tournament()
            .name(DEFAULT_NAME)
            .note(DEFAULT_NOTE)
            .pointsForWinning(DEFAULT_POINTS_FOR_WINNING)
            .pointsForLosing(DEFAULT_POINTS_FOR_LOSING)
            .pointsForTie(DEFAULT_POINTS_FOR_TIE)
            .tournamentType(DEFAULT_TOURNAMENT_TYPE);
        return tournament;
    }

    @Before
    public void initTest() {
        tournament = createEntity(em);
    }

    @Test
    @Transactional
    public void createTournament() throws Exception {
        int databaseSizeBeforeCreate = tournamentRepository.findAll().size();

        // Create the Tournament
        restTournamentMockMvc.perform(post("/api/tournaments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tournament)))
            .andExpect(status().isCreated());

        // Validate the Tournament in the database
        List<Tournament> tournamentList = tournamentRepository.findAll();
        assertThat(tournamentList).hasSize(databaseSizeBeforeCreate + 1);
        Tournament testTournament = tournamentList.get(tournamentList.size() - 1);
        assertThat(testTournament.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTournament.getNote()).isEqualTo(DEFAULT_NOTE);
        assertThat(testTournament.getPointsForWinning()).isEqualTo(DEFAULT_POINTS_FOR_WINNING);
        assertThat(testTournament.getPointsForLosing()).isEqualTo(DEFAULT_POINTS_FOR_LOSING);
        assertThat(testTournament.getPointsForTie()).isEqualTo(DEFAULT_POINTS_FOR_TIE);
        assertThat(testTournament.getTournamentType()).isEqualTo(DEFAULT_TOURNAMENT_TYPE);
    }

    @Test
    @Transactional
    public void createTournamentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = tournamentRepository.findAll().size();

        // Create the Tournament with an existing ID
        tournament.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTournamentMockMvc.perform(post("/api/tournaments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tournament)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Tournament> tournamentList = tournamentRepository.findAll();
        assertThat(tournamentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = tournamentRepository.findAll().size();
        // set the field null
        tournament.setName(null);

        // Create the Tournament, which fails.

        restTournamentMockMvc.perform(post("/api/tournaments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tournament)))
            .andExpect(status().isBadRequest());

        List<Tournament> tournamentList = tournamentRepository.findAll();
        assertThat(tournamentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTournamentTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = tournamentRepository.findAll().size();
        // set the field null
        tournament.setTournamentType(null);

        // Create the Tournament, which fails.

        restTournamentMockMvc.perform(post("/api/tournaments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tournament)))
            .andExpect(status().isBadRequest());

        List<Tournament> tournamentList = tournamentRepository.findAll();
        assertThat(tournamentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllTournaments() throws Exception {
        // Initialize the database
        tournamentRepository.saveAndFlush(tournament);

        // Get all the tournamentList
        restTournamentMockMvc.perform(get("/api/tournaments?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tournament.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE.toString())))
            .andExpect(jsonPath("$.[*].pointsForWinning").value(hasItem(DEFAULT_POINTS_FOR_WINNING)))
            .andExpect(jsonPath("$.[*].pointsForLosing").value(hasItem(DEFAULT_POINTS_FOR_LOSING)))
            .andExpect(jsonPath("$.[*].pointsForTie").value(hasItem(DEFAULT_POINTS_FOR_TIE)))
            .andExpect(jsonPath("$.[*].tournamentType").value(hasItem(DEFAULT_TOURNAMENT_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getTournament() throws Exception {
        // Initialize the database
        tournamentRepository.saveAndFlush(tournament);

        // Get the tournament
        restTournamentMockMvc.perform(get("/api/tournaments/{id}", tournament.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(tournament.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE.toString()))
            .andExpect(jsonPath("$.pointsForWinning").value(DEFAULT_POINTS_FOR_WINNING))
            .andExpect(jsonPath("$.pointsForLosing").value(DEFAULT_POINTS_FOR_LOSING))
            .andExpect(jsonPath("$.pointsForTie").value(DEFAULT_POINTS_FOR_TIE))
            .andExpect(jsonPath("$.tournamentType").value(DEFAULT_TOURNAMENT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingTournament() throws Exception {
        // Get the tournament
        restTournamentMockMvc.perform(get("/api/tournaments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTournament() throws Exception {
        // Initialize the database
        tournamentRepository.saveAndFlush(tournament);
        int databaseSizeBeforeUpdate = tournamentRepository.findAll().size();

        // Update the tournament
        Tournament updatedTournament = tournamentRepository.findOne(tournament.getId());
        updatedTournament
            .name(UPDATED_NAME)
            .note(UPDATED_NOTE)
            .pointsForWinning(UPDATED_POINTS_FOR_WINNING)
            .pointsForLosing(UPDATED_POINTS_FOR_LOSING)
            .pointsForTie(UPDATED_POINTS_FOR_TIE)
            .tournamentType(UPDATED_TOURNAMENT_TYPE);

        restTournamentMockMvc.perform(put("/api/tournaments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedTournament)))
            .andExpect(status().isOk());

        // Validate the Tournament in the database
        List<Tournament> tournamentList = tournamentRepository.findAll();
        assertThat(tournamentList).hasSize(databaseSizeBeforeUpdate);
        Tournament testTournament = tournamentList.get(tournamentList.size() - 1);
        assertThat(testTournament.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTournament.getNote()).isEqualTo(UPDATED_NOTE);
        assertThat(testTournament.getPointsForWinning()).isEqualTo(UPDATED_POINTS_FOR_WINNING);
        assertThat(testTournament.getPointsForLosing()).isEqualTo(UPDATED_POINTS_FOR_LOSING);
        assertThat(testTournament.getPointsForTie()).isEqualTo(UPDATED_POINTS_FOR_TIE);
        assertThat(testTournament.getTournamentType()).isEqualTo(UPDATED_TOURNAMENT_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingTournament() throws Exception {
        int databaseSizeBeforeUpdate = tournamentRepository.findAll().size();

        // Create the Tournament

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restTournamentMockMvc.perform(put("/api/tournaments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tournament)))
            .andExpect(status().isCreated());

        // Validate the Tournament in the database
        List<Tournament> tournamentList = tournamentRepository.findAll();
        assertThat(tournamentList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteTournament() throws Exception {
        // Initialize the database
        tournamentRepository.saveAndFlush(tournament);
        int databaseSizeBeforeDelete = tournamentRepository.findAll().size();

        // Get the tournament
        restTournamentMockMvc.perform(delete("/api/tournaments/{id}", tournament.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Tournament> tournamentList = tournamentRepository.findAll();
        assertThat(tournamentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Tournament.class);
    }
}
