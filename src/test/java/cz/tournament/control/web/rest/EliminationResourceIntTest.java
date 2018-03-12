package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.enumeration.EliminationType;
import cz.tournament.control.repository.EliminationRepository;
import cz.tournament.control.service.EliminationService;
import cz.tournament.control.web.rest.errors.ExceptionTranslator;
import java.time.Instant;
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
 * Test class for the EliminationResource REST controller.
 *
 * @see EliminationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournamentControlApp.class)
public class EliminationResourceIntTest {
    
    private static final ZonedDateTime DEFAULT_CREATED = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final String DEFAULT_NAME="elimination test";

    private static final EliminationType DEFAULT_TYPE = EliminationType.SINGLE;
    private static final EliminationType UPDATED_TYPE = EliminationType.DOUBLE;

    private static final Boolean DEFAULT_BRONZE_MATCH = false;
    private static final Boolean UPDATED_BRONZE_MATCH = true;

    @Autowired
    private EliminationRepository eliminationRepository;

    @Autowired
    private EliminationService eliminationService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restEliminationMockMvc;

    private Elimination elimination;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EliminationResource eliminationResource = new EliminationResource(eliminationService);
        this.restEliminationMockMvc = MockMvcBuilders.standaloneSetup(eliminationResource)
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
    public static Elimination createEntity(EntityManager em) {
        Elimination elimination = new Elimination()
            .type(DEFAULT_TYPE)
            .bronzeMatch(DEFAULT_BRONZE_MATCH);
        elimination.name(DEFAULT_NAME);
        elimination.created(DEFAULT_CREATED);
            
        return elimination;
    }

    @Before
    public void initTest() {
        elimination = createEntity(em);
    }

    @Test
    @Transactional
    public void createElimination() throws Exception {
        int databaseSizeBeforeCreate = eliminationRepository.findAll().size();

        // Create the Elimination
        restEliminationMockMvc.perform(post("/api/eliminations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(elimination)))
            .andExpect(status().isCreated());

        // Validate the Elimination in the database
        List<Elimination> eliminationList = eliminationRepository.findAll();
        assertThat(eliminationList).hasSize(databaseSizeBeforeCreate + 1);
        Elimination testElimination = eliminationList.get(eliminationList.size() - 1);
        assertThat(testElimination.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testElimination.getBronzeMatch()).isEqualTo(DEFAULT_BRONZE_MATCH);
    }

    @Test
    @Transactional
    public void createEliminationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eliminationRepository.findAll().size();

        // Create the Elimination with an existing ID
        elimination.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEliminationMockMvc.perform(post("/api/eliminations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(elimination)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Elimination> eliminationList = eliminationRepository.findAll();
        assertThat(eliminationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllEliminations() throws Exception {
        // Initialize the database
        eliminationRepository.saveAndFlush(elimination);

        // Get all the eliminationList
        restEliminationMockMvc.perform(get("/api/eliminations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(elimination.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].bronzeMatch").value(hasItem(DEFAULT_BRONZE_MATCH.booleanValue())));
    }

    @Test
    @Transactional
    public void getElimination() throws Exception {
        // Initialize the database
        eliminationRepository.saveAndFlush(elimination);

        // Get the elimination
        restEliminationMockMvc.perform(get("/api/eliminations/{id}", elimination.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(elimination.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.bronzeMatch").value(DEFAULT_BRONZE_MATCH.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingElimination() throws Exception {
        // Get the elimination
        restEliminationMockMvc.perform(get("/api/eliminations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateElimination() throws Exception {
        // Initialize the database
        eliminationService.save(elimination);

        int databaseSizeBeforeUpdate = eliminationRepository.findAll().size();

        // Update the elimination
        Elimination updatedElimination = eliminationRepository.findOne(elimination.getId());
        updatedElimination
            .type(UPDATED_TYPE)
            .bronzeMatch(UPDATED_BRONZE_MATCH);

        restEliminationMockMvc.perform(put("/api/eliminations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedElimination)))
            .andExpect(status().isOk());

        // Validate the Elimination in the database
        List<Elimination> eliminationList = eliminationRepository.findAll();
        assertThat(eliminationList).hasSize(databaseSizeBeforeUpdate);
        Elimination testElimination = eliminationList.get(eliminationList.size() - 1);
        assertThat(testElimination.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testElimination.getBronzeMatch()).isEqualTo(UPDATED_BRONZE_MATCH);
    }

    @Test
    @Transactional
    public void updateNonExistingElimination() throws Exception {
        int databaseSizeBeforeUpdate = eliminationRepository.findAll().size();

        // Create the Elimination

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restEliminationMockMvc.perform(put("/api/eliminations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(elimination)))
            .andExpect(status().isCreated());

        // Validate the Elimination in the database
        List<Elimination> eliminationList = eliminationRepository.findAll();
        assertThat(eliminationList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteElimination() throws Exception {
        // Initialize the database
        eliminationService.save(elimination);

        int databaseSizeBeforeDelete = eliminationRepository.findAll().size();

        // Get the elimination
        restEliminationMockMvc.perform(delete("/api/eliminations/{id}", elimination.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Elimination> eliminationList = eliminationRepository.findAll();
        assertThat(eliminationList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Elimination.class);
    }
}
