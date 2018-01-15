package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;
import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.repository.CombinedRepository;
import cz.tournament.control.service.CombinedService;
import static cz.tournament.control.web.rest.TestUtil.createFormattingConversionService;
import cz.tournament.control.web.rest.errors.ExceptionTranslator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Test class for the CombinedResource REST controller.
 *
 * @see CombinedResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournamentControlApp.class)
public class CombinedResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_NUMBER_OF_WINNERS_TO_PLAYOFF = 0;
    private static final Integer UPDATED_NUMBER_OF_WINNERS_TO_PLAYOFF = 1;

    private static final Integer DEFAULT_NUMBER_OF_GROUPS = 1;
    private static final Integer UPDATED_NUMBER_OF_GROUPS = 2;

    private static final TournamentType DEFAULT_PLAYOFF_TYPE = TournamentType.ALL_VERSUS_ALL;
    private static final TournamentType UPDATED_PLAYOFF_TYPE = TournamentType.ELIMINATION_SINGLE;

    private static final TournamentType DEFAULT_IN_GROUP_TOURNAMENT_TYPE = TournamentType.ALL_VERSUS_ALL;
    private static final TournamentType UPDATED_IN_GROUP_TOURNAMENT_TYPE = TournamentType.ELIMINATION_SINGLE;

    @Autowired
    private CombinedRepository combinedRepository;

    @Autowired
    private CombinedService combinedService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restCombinedMockMvc;

    private Combined combined;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CombinedResource combinedResource = new CombinedResource(combinedService);
        this.restCombinedMockMvc = MockMvcBuilders.standaloneSetup(combinedResource)
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
    public static Combined createEntity(EntityManager em) {
        Combined combined = new Combined()
            .name(DEFAULT_NAME)
            .note(DEFAULT_NOTE)
            .created(DEFAULT_CREATED)
            .numberOfWinnersToPlayoff(DEFAULT_NUMBER_OF_WINNERS_TO_PLAYOFF)
            .numberOfGroups(DEFAULT_NUMBER_OF_GROUPS)
            .playoffType(DEFAULT_PLAYOFF_TYPE)
            .inGroupTournamentType(DEFAULT_IN_GROUP_TOURNAMENT_TYPE);
        return combined;
    }

    @Before
    public void initTest() {
        combined = createEntity(em);
    }

    @Test
    @Transactional
    public void createCombined() throws Exception {
        int databaseSizeBeforeCreate = combinedRepository.findAll().size();

        // Create the Combined
        restCombinedMockMvc.perform(post("/api/combineds")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(combined)))
            .andExpect(status().isCreated());

        // Validate the Combined in the database
        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeCreate + 1);
        Combined testCombined = combinedList.get(combinedList.size() - 1);
        assertThat(testCombined.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCombined.getNote()).isEqualTo(DEFAULT_NOTE);
        assertThat(testCombined.getCreated()).isEqualTo(DEFAULT_CREATED);
        assertThat(testCombined.getNumberOfWinnersToPlayoff()).isEqualTo(DEFAULT_NUMBER_OF_WINNERS_TO_PLAYOFF);
        assertThat(testCombined.getNumberOfGroups()).isEqualTo(DEFAULT_NUMBER_OF_GROUPS);
        assertThat(testCombined.getPlayoffType()).isEqualTo(DEFAULT_PLAYOFF_TYPE);
        assertThat(testCombined.getInGroupTournamentType()).isEqualTo(DEFAULT_IN_GROUP_TOURNAMENT_TYPE);
    }

    @Test
    @Transactional
    public void createCombinedWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = combinedRepository.findAll().size();

        // Create the Combined with an existing ID
        combined.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCombinedMockMvc.perform(post("/api/combineds")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(combined)))
            .andExpect(status().isBadRequest());

        // Validate the Combined in the database
        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = combinedRepository.findAll().size();
        // set the field null
        combined.setName(null);

        // Create the Combined, which fails.

        restCombinedMockMvc.perform(post("/api/combineds")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(combined)))
            .andExpect(status().isBadRequest());

        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNumberOfWinnersToPlayoffIsRequired() throws Exception {
        int databaseSizeBeforeTest = combinedRepository.findAll().size();
        // set the field null
        combined.setNumberOfWinnersToPlayoff(null);

        // Create the Combined, which fails.

        restCombinedMockMvc.perform(post("/api/combineds")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(combined)))
            .andExpect(status().isBadRequest());

        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNumberOfGroupsIsRequired() throws Exception {
        int databaseSizeBeforeTest = combinedRepository.findAll().size();
        // set the field null
        combined.setNumberOfGroups(null);

        // Create the Combined, which fails.

        restCombinedMockMvc.perform(post("/api/combineds")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(combined)))
            .andExpect(status().isBadRequest());

        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCombineds() throws Exception {
        // Initialize the database
        combinedRepository.saveAndFlush(combined);

        // Get all the combinedList
        restCombinedMockMvc.perform(get("/api/combineds?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(combined.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE.toString())))
            .andExpect(jsonPath("$.[*].created").value(hasItem(DEFAULT_CREATED.toString())))
            .andExpect(jsonPath("$.[*].numberOfWinnersToPlayoff").value(hasItem(DEFAULT_NUMBER_OF_WINNERS_TO_PLAYOFF)))
            .andExpect(jsonPath("$.[*].numberOfGroups").value(hasItem(DEFAULT_NUMBER_OF_GROUPS)))
            .andExpect(jsonPath("$.[*].playoffType").value(hasItem(DEFAULT_PLAYOFF_TYPE.toString())))
            .andExpect(jsonPath("$.[*].inGroupTournamentType").value(hasItem(DEFAULT_IN_GROUP_TOURNAMENT_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getCombined() throws Exception {
        // Initialize the database
        combinedRepository.saveAndFlush(combined);

        // Get the combined
        restCombinedMockMvc.perform(get("/api/combineds/{id}", combined.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(combined.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE.toString()))
            .andExpect(jsonPath("$.created").value(DEFAULT_CREATED.toString()))
            .andExpect(jsonPath("$.numberOfWinnersToPlayoff").value(DEFAULT_NUMBER_OF_WINNERS_TO_PLAYOFF))
            .andExpect(jsonPath("$.numberOfGroups").value(DEFAULT_NUMBER_OF_GROUPS))
            .andExpect(jsonPath("$.playoffType").value(DEFAULT_PLAYOFF_TYPE.toString()))
            .andExpect(jsonPath("$.inGroupTournamentType").value(DEFAULT_IN_GROUP_TOURNAMENT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCombined() throws Exception {
        // Get the combined
        restCombinedMockMvc.perform(get("/api/combineds/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCombined() throws Exception {
        // Initialize the database
        combinedService.save(combined);

        int databaseSizeBeforeUpdate = combinedRepository.findAll().size();

        // Update the combined
        Combined updatedCombined = combinedRepository.findOne(combined.getId());
        updatedCombined
            .name(UPDATED_NAME)
            .note(UPDATED_NOTE)
            .created(UPDATED_CREATED)
            .numberOfWinnersToPlayoff(UPDATED_NUMBER_OF_WINNERS_TO_PLAYOFF)
            .numberOfGroups(UPDATED_NUMBER_OF_GROUPS)
            .playoffType(UPDATED_PLAYOFF_TYPE)
            .inGroupTournamentType(UPDATED_IN_GROUP_TOURNAMENT_TYPE);

        restCombinedMockMvc.perform(put("/api/combineds")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCombined)))
            .andExpect(status().isOk());

        // Validate the Combined in the database
        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeUpdate);
        Combined testCombined = combinedList.get(combinedList.size() - 1);
        assertThat(testCombined.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCombined.getNote()).isEqualTo(UPDATED_NOTE);
        assertThat(testCombined.getCreated()).isEqualTo(UPDATED_CREATED);
        assertThat(testCombined.getNumberOfWinnersToPlayoff()).isEqualTo(UPDATED_NUMBER_OF_WINNERS_TO_PLAYOFF);
        assertThat(testCombined.getNumberOfGroups()).isEqualTo(UPDATED_NUMBER_OF_GROUPS);
        assertThat(testCombined.getPlayoffType()).isEqualTo(UPDATED_PLAYOFF_TYPE);
        assertThat(testCombined.getInGroupTournamentType()).isEqualTo(UPDATED_IN_GROUP_TOURNAMENT_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingCombined() throws Exception {
        int databaseSizeBeforeUpdate = combinedRepository.findAll().size();

        // Create the Combined

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restCombinedMockMvc.perform(put("/api/combineds")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(combined)))
            .andExpect(status().isCreated());

        // Validate the Combined in the database
        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteCombined() throws Exception {
        // Initialize the database
        combinedService.save(combined);

        int databaseSizeBeforeDelete = combinedRepository.findAll().size();

        // Get the combined
        restCombinedMockMvc.perform(delete("/api/combineds/{id}", combined.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Combined> combinedList = combinedRepository.findAll();
        assertThat(combinedList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Combined.class);
        Combined combined1 = new Combined();
        combined1.setId(1L);
        Combined combined2 = new Combined();
        combined2.setId(combined1.getId());
        assertThat(combined1).isEqualTo(combined2);
        combined2.setId(2L);
        assertThat(combined1).isNotEqualTo(combined2);
        combined1.setId(null);
        assertThat(combined1).isNotEqualTo(combined2);
    }
}
