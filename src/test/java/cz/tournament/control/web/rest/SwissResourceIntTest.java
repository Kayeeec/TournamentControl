package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.repository.SwissRepository;
import cz.tournament.control.service.SwissService;
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
 * Test class for the SwissResource REST controller.
 *
 * @see SwissResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournamentControlApp.class)
public class SwissResourceIntTest {

    private static final Integer DEFAULT_ROUNDS = 1;
    private static final Integer UPDATED_ROUNDS = 2;

    private static final Integer DEFAULT_ROUNDS_TO_GENERATE = 1;
    private static final Integer UPDATED_ROUNDS_TO_GENERATE = 2;

    private static final Boolean DEFAULT_COLOR = false;
    private static final Boolean UPDATED_COLOR = true;
    
    private static final ZonedDateTime DEFAULT_CREATED = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final String DEFAULT_NAME="swiss test";

    @Autowired
    private SwissRepository swissRepository;

    @Autowired
    private SwissService swissService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSwissMockMvc;

    private Swiss swiss;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SwissResource swissResource = new SwissResource(swissService);
        this.restSwissMockMvc = MockMvcBuilders.standaloneSetup(swissResource)
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
    public static Swiss createEntity(EntityManager em) {
        Swiss swiss = new Swiss()
            .rounds(DEFAULT_ROUNDS)
            .roundsToGenerate(DEFAULT_ROUNDS_TO_GENERATE)
            .color(DEFAULT_COLOR);
        swiss.name(DEFAULT_NAME);
        swiss.created(DEFAULT_CREATED);
        return swiss;
    }

    @Before
    public void initTest() {
        swiss = createEntity(em);
    }

    @Test
    @Transactional
    public void createSwiss() throws Exception {
        int databaseSizeBeforeCreate = swissRepository.findAll().size();

        // Create the Swiss
        restSwissMockMvc.perform(post("/api/swisses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(swiss)))
            .andExpect(status().isCreated());

        // Validate the Swiss in the database
        List<Swiss> swissList = swissRepository.findAll();
        assertThat(swissList).hasSize(databaseSizeBeforeCreate + 1);
        Swiss testSwiss = swissList.get(swissList.size() - 1);
        assertThat(testSwiss.getRounds()).isEqualTo(DEFAULT_ROUNDS);
        assertThat(testSwiss.getRoundsToGenerate()).isEqualTo(DEFAULT_ROUNDS_TO_GENERATE);
        assertThat(testSwiss.isColor()).isEqualTo(DEFAULT_COLOR);
    }

    @Test
    @Transactional
    public void createSwissWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = swissRepository.findAll().size();

        // Create the Swiss with an existing ID
        swiss.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSwissMockMvc.perform(post("/api/swisses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(swiss)))
            .andExpect(status().isBadRequest());

        // Validate the Swiss in the database
        List<Swiss> swissList = swissRepository.findAll();
        assertThat(swissList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllSwisses() throws Exception {
        // Initialize the database
        swissRepository.saveAndFlush(swiss);

        // Get all the swissList
        restSwissMockMvc.perform(get("/api/swisses?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(swiss.getId().intValue())))
            .andExpect(jsonPath("$.[*].rounds").value(hasItem(DEFAULT_ROUNDS)))
            .andExpect(jsonPath("$.[*].roundsToGenerate").value(hasItem(DEFAULT_ROUNDS_TO_GENERATE)))
            .andExpect(jsonPath("$.[*].color").value(hasItem(DEFAULT_COLOR.booleanValue())));
    }

    @Test
    @Transactional
    public void getSwiss() throws Exception {
        // Initialize the database
        swissRepository.saveAndFlush(swiss);

        // Get the swiss
        restSwissMockMvc.perform(get("/api/swisses/{id}", swiss.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(swiss.getId().intValue()))
            .andExpect(jsonPath("$.rounds").value(DEFAULT_ROUNDS))
            .andExpect(jsonPath("$.roundsToGenerate").value(DEFAULT_ROUNDS_TO_GENERATE))
            .andExpect(jsonPath("$.color").value(DEFAULT_COLOR.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingSwiss() throws Exception {
        // Get the swiss
        restSwissMockMvc.perform(get("/api/swisses/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSwiss() throws Exception {
        // Initialize the database
        swissService.save(swiss);

        int databaseSizeBeforeUpdate = swissRepository.findAll().size();

        // Update the swiss
        Swiss updatedSwiss = swissRepository.findOne(swiss.getId());
        updatedSwiss
            .rounds(UPDATED_ROUNDS)
            .roundsToGenerate(UPDATED_ROUNDS_TO_GENERATE)
            .color(UPDATED_COLOR);

        restSwissMockMvc.perform(put("/api/swisses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedSwiss)))
            .andExpect(status().isOk());

        // Validate the Swiss in the database
        List<Swiss> swissList = swissRepository.findAll();
        assertThat(swissList).hasSize(databaseSizeBeforeUpdate);
        Swiss testSwiss = swissList.get(swissList.size() - 1);
        assertThat(testSwiss.getRounds()).isEqualTo(UPDATED_ROUNDS);
        assertThat(testSwiss.getRoundsToGenerate()).isEqualTo(UPDATED_ROUNDS_TO_GENERATE);
        assertThat(testSwiss.isColor()).isEqualTo(UPDATED_COLOR);
    }

    @Test
    @Transactional
    public void updateNonExistingSwiss() throws Exception {
        int databaseSizeBeforeUpdate = swissRepository.findAll().size();

        // Create the Swiss

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSwissMockMvc.perform(put("/api/swisses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(swiss)))
            .andExpect(status().isCreated());

        // Validate the Swiss in the database
        List<Swiss> swissList = swissRepository.findAll();
        assertThat(swissList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSwiss() throws Exception {
        // Initialize the database
        swissService.save(swiss);

        int databaseSizeBeforeDelete = swissRepository.findAll().size();

        // Get the swiss
        restSwissMockMvc.perform(delete("/api/swisses/{id}", swiss.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Swiss> swissList = swissRepository.findAll();
        assertThat(swissList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Swiss.class);
        Swiss swiss1 = new Swiss();
        swiss1.setId(1L);
        Swiss swiss2 = new Swiss();
        swiss2.setId(swiss1.getId());
        assertThat(swiss1).isEqualTo(swiss2);
        swiss2.setId(2L);
        assertThat(swiss1).isNotEqualTo(swiss2);
        swiss1.setId(null);
        assertThat(swiss1).isNotEqualTo(swiss2);
    }
}
