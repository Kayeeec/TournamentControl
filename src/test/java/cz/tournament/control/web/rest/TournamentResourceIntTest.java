package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.service.TournamentService;
import static cz.tournament.control.web.rest.TestUtil.createFormattingConversionService;
import static cz.tournament.control.web.rest.TestUtil.sameInstant;
import cz.tournament.control.web.rest.errors.ExceptionTranslator;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
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

    private static final ZonedDateTime DEFAULT_CREATED = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Integer DEFAULT_SETS_TO_WIN = 1;
    private static final Integer UPDATED_SETS_TO_WIN = 2;

    private static final Boolean DEFAULT_TIES_ALLOWED = false;
    private static final Boolean UPDATED_TIES_ALLOWED = true;

    private static final Integer DEFAULT_PLAYING_FIELDS = 1;
    private static final Integer UPDATED_PLAYING_FIELDS = 2;

    private static final Double DEFAULT_POINTS_FOR_WINNING = 1D;
    private static final Double UPDATED_POINTS_FOR_WINNING = 2D;

    private static final Double DEFAULT_POINTS_FOR_TIE = 1D;
    private static final Double UPDATED_POINTS_FOR_TIE = 2D;

    private static final Double DEFAULT_POINTS_FOR_LOSING = 1D;
    private static final Double UPDATED_POINTS_FOR_LOSING = 2D;

    private static final Boolean DEFAULT_IN_COMBINED = false;
    private static final Boolean UPDATED_IN_COMBINED = true;

    private static final TournamentType DEFAULT_TOURNAMENT_TYPE = TournamentType.ALL_VERSUS_ALL;
    private static final TournamentType UPDATED_TOURNAMENT_TYPE = TournamentType.SWISS;

    @Autowired
    private TournamentRepository tournamentRepository;

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
        TournamentResource tournamentResource = new TournamentResource(tournamentService);
        this.restTournamentMockMvc = MockMvcBuilders.standaloneSetup(tournamentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
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
            .created(DEFAULT_CREATED)
            .setsToWin(DEFAULT_SETS_TO_WIN)
            .tiesAllowed(DEFAULT_TIES_ALLOWED)
            .playingFields(DEFAULT_PLAYING_FIELDS)
            .pointsForWinning(DEFAULT_POINTS_FOR_WINNING)
            .pointsForTie(DEFAULT_POINTS_FOR_TIE)
            .pointsForLosing(DEFAULT_POINTS_FOR_LOSING)
            .inCombined(DEFAULT_IN_COMBINED)
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
        assertThat(testTournament.getCreated()).isEqualTo(DEFAULT_CREATED);
        assertThat(testTournament.getSetsToWin()).isEqualTo(DEFAULT_SETS_TO_WIN);
        assertThat(testTournament.getTiesAllowed()).isEqualTo(DEFAULT_TIES_ALLOWED);
        assertThat(testTournament.getPlayingFields()).isEqualTo(DEFAULT_PLAYING_FIELDS);
        assertThat(testTournament.getPointsForWinning()).isEqualTo(DEFAULT_POINTS_FOR_WINNING);
        assertThat(testTournament.getPointsForTie()).isEqualTo(DEFAULT_POINTS_FOR_TIE);
        assertThat(testTournament.getPointsForLosing()).isEqualTo(DEFAULT_POINTS_FOR_LOSING);
        assertThat(testTournament.isInCombined()).isEqualTo(DEFAULT_IN_COMBINED);
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

        // Validate the Tournament in the database
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
    public void checkCreatedIsRequired() throws Exception {
        int databaseSizeBeforeTest = tournamentRepository.findAll().size();
        // set the field null
        tournament.setCreated(null);

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
            .andExpect(jsonPath("$.[*].created").value(hasItem(sameInstant(DEFAULT_CREATED))))
            .andExpect(jsonPath("$.[*].setsToWin").value(hasItem(DEFAULT_SETS_TO_WIN)))
            .andExpect(jsonPath("$.[*].tiesAllowed").value(hasItem(DEFAULT_TIES_ALLOWED.booleanValue())))
            .andExpect(jsonPath("$.[*].playingFields").value(hasItem(DEFAULT_PLAYING_FIELDS)))
            .andExpect(jsonPath("$.[*].pointsForWinning").value(hasItem(DEFAULT_POINTS_FOR_WINNING.doubleValue())))
            .andExpect(jsonPath("$.[*].pointsForTie").value(hasItem(DEFAULT_POINTS_FOR_TIE.doubleValue())))
            .andExpect(jsonPath("$.[*].pointsForLosing").value(hasItem(DEFAULT_POINTS_FOR_LOSING.doubleValue())))
            .andExpect(jsonPath("$.[*].inCombined").value(hasItem(DEFAULT_IN_COMBINED.booleanValue())))
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
            .andExpect(jsonPath("$.created").value(sameInstant(DEFAULT_CREATED)))
            .andExpect(jsonPath("$.setsToWin").value(DEFAULT_SETS_TO_WIN))
            .andExpect(jsonPath("$.tiesAllowed").value(DEFAULT_TIES_ALLOWED.booleanValue()))
            .andExpect(jsonPath("$.playingFields").value(DEFAULT_PLAYING_FIELDS))
            .andExpect(jsonPath("$.pointsForWinning").value(DEFAULT_POINTS_FOR_WINNING.doubleValue()))
            .andExpect(jsonPath("$.pointsForTie").value(DEFAULT_POINTS_FOR_TIE.doubleValue()))
            .andExpect(jsonPath("$.pointsForLosing").value(DEFAULT_POINTS_FOR_LOSING.doubleValue()))
            .andExpect(jsonPath("$.inCombined").value(DEFAULT_IN_COMBINED.booleanValue()))
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
            .created(UPDATED_CREATED)
            .setsToWin(UPDATED_SETS_TO_WIN)
            .tiesAllowed(UPDATED_TIES_ALLOWED)
            .playingFields(UPDATED_PLAYING_FIELDS)
            .pointsForWinning(UPDATED_POINTS_FOR_WINNING)
            .pointsForTie(UPDATED_POINTS_FOR_TIE)
            .pointsForLosing(UPDATED_POINTS_FOR_LOSING)
            .inCombined(UPDATED_IN_COMBINED)
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
        assertThat(testTournament.getCreated()).isEqualTo(UPDATED_CREATED);
        assertThat(testTournament.getSetsToWin()).isEqualTo(UPDATED_SETS_TO_WIN);
        assertThat(testTournament.getTiesAllowed()).isEqualTo(UPDATED_TIES_ALLOWED);
        assertThat(testTournament.getPlayingFields()).isEqualTo(UPDATED_PLAYING_FIELDS);
        assertThat(testTournament.getPointsForWinning()).isEqualTo(UPDATED_POINTS_FOR_WINNING);
        assertThat(testTournament.getPointsForTie()).isEqualTo(UPDATED_POINTS_FOR_TIE);
        assertThat(testTournament.getPointsForLosing()).isEqualTo(UPDATED_POINTS_FOR_LOSING);
        assertThat(testTournament.isInCombined()).isEqualTo(UPDATED_IN_COMBINED);
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
        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);
        Tournament tournament2 = new Tournament();
        tournament2.setId(tournament1.getId());
        assertThat(tournament1).isEqualTo(tournament2);
        tournament2.setId(2L);
        assertThat(tournament1).isNotEqualTo(tournament2);
        tournament1.setId(null);
        assertThat(tournament1).isNotEqualTo(tournament2);
    }
}
