package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;

import cz.tournament.control.domain.GameSet;
import cz.tournament.control.repository.GameSetRepository;
import cz.tournament.control.service.GameSetService;
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

/**
 * Test class for the GameSetResource REST controller.
 *
 * @see GameSetResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournamentControlApp.class)
public class GameSetResourceIntTest {

    private static final Integer DEFAULT_SCORE_A = 1;
    private static final Integer UPDATED_SCORE_A = 2;

    private static final Integer DEFAULT_SCORE_B = 1;
    private static final Integer UPDATED_SCORE_B = 2;

    private static final Boolean DEFAULT_FINISHED = false;
    private static final Boolean UPDATED_FINISHED = true;

    @Autowired
    private GameSetRepository gameSetRepository;

    @Autowired
    private GameSetService gameSetService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restGameSetMockMvc;

    private GameSet gameSet;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        GameSetResource gameSetResource = new GameSetResource(gameSetService);
        this.restGameSetMockMvc = MockMvcBuilders.standaloneSetup(gameSetResource)
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
    public static GameSet createEntity(EntityManager em) {
        GameSet gameSet = new GameSet()
            .scoreA(DEFAULT_SCORE_A)
            .scoreB(DEFAULT_SCORE_B)
            .finished(DEFAULT_FINISHED);
        return gameSet;
    }

    @Before
    public void initTest() {
        gameSet = createEntity(em);
    }

    @Test
    @Transactional
    public void createGameSet() throws Exception {
        int databaseSizeBeforeCreate = gameSetRepository.findAll().size();

        // Create the GameSet
        restGameSetMockMvc.perform(post("/api/game-sets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gameSet)))
            .andExpect(status().isCreated());

        // Validate the GameSet in the database
        List<GameSet> gameSetList = gameSetRepository.findAll();
        assertThat(gameSetList).hasSize(databaseSizeBeforeCreate + 1);
        GameSet testGameSet = gameSetList.get(gameSetList.size() - 1);
        assertThat(testGameSet.getScoreA()).isEqualTo(DEFAULT_SCORE_A);
        assertThat(testGameSet.getScoreB()).isEqualTo(DEFAULT_SCORE_B);
        assertThat(testGameSet.isFinished()).isEqualTo(DEFAULT_FINISHED);
    }

    @Test
    @Transactional
    public void createGameSetWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = gameSetRepository.findAll().size();

        // Create the GameSet with an existing ID
        gameSet.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGameSetMockMvc.perform(post("/api/game-sets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gameSet)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<GameSet> gameSetList = gameSetRepository.findAll();
        assertThat(gameSetList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllGameSets() throws Exception {
        // Initialize the database
        gameSetRepository.saveAndFlush(gameSet);

        // Get all the gameSetList
        restGameSetMockMvc.perform(get("/api/game-sets?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gameSet.getId().intValue())))
            .andExpect(jsonPath("$.[*].scoreA").value(hasItem(DEFAULT_SCORE_A)))
            .andExpect(jsonPath("$.[*].scoreB").value(hasItem(DEFAULT_SCORE_B)))
            .andExpect(jsonPath("$.[*].finished").value(hasItem(DEFAULT_FINISHED.booleanValue())));
    }

    @Test
    @Transactional
    public void getGameSet() throws Exception {
        // Initialize the database
        gameSetRepository.saveAndFlush(gameSet);

        // Get the gameSet
        restGameSetMockMvc.perform(get("/api/game-sets/{id}", gameSet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(gameSet.getId().intValue()))
            .andExpect(jsonPath("$.scoreA").value(DEFAULT_SCORE_A))
            .andExpect(jsonPath("$.scoreB").value(DEFAULT_SCORE_B))
            .andExpect(jsonPath("$.finished").value(DEFAULT_FINISHED.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingGameSet() throws Exception {
        // Get the gameSet
        restGameSetMockMvc.perform(get("/api/game-sets/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGameSet() throws Exception {
        // Initialize the database
        gameSetService.save(gameSet);

        int databaseSizeBeforeUpdate = gameSetRepository.findAll().size();

        // Update the gameSet
        GameSet updatedGameSet = gameSetRepository.findOne(gameSet.getId());
        updatedGameSet
            .scoreA(UPDATED_SCORE_A)
            .scoreB(UPDATED_SCORE_B)
            .finished(UPDATED_FINISHED);

        restGameSetMockMvc.perform(put("/api/game-sets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedGameSet)))
            .andExpect(status().isOk());

        // Validate the GameSet in the database
        List<GameSet> gameSetList = gameSetRepository.findAll();
        assertThat(gameSetList).hasSize(databaseSizeBeforeUpdate);
        GameSet testGameSet = gameSetList.get(gameSetList.size() - 1);
        assertThat(testGameSet.getScoreA()).isEqualTo(UPDATED_SCORE_A);
        assertThat(testGameSet.getScoreB()).isEqualTo(UPDATED_SCORE_B);
        assertThat(testGameSet.isFinished()).isEqualTo(UPDATED_FINISHED);
    }

    @Test
    @Transactional
    public void updateNonExistingGameSet() throws Exception {
        int databaseSizeBeforeUpdate = gameSetRepository.findAll().size();

        // Create the GameSet

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restGameSetMockMvc.perform(put("/api/game-sets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gameSet)))
            .andExpect(status().isCreated());

        // Validate the GameSet in the database
        List<GameSet> gameSetList = gameSetRepository.findAll();
        assertThat(gameSetList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteGameSet() throws Exception {
        // Initialize the database
        gameSetService.save(gameSet);

        int databaseSizeBeforeDelete = gameSetRepository.findAll().size();

        // Get the gameSet
        restGameSetMockMvc.perform(delete("/api/game-sets/{id}", gameSet.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<GameSet> gameSetList = gameSetRepository.findAll();
        assertThat(gameSetList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GameSet.class);
    }
}
