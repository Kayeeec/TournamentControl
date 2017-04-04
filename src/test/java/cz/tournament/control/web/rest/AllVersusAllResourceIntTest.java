package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;

import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.AllVersusAllRepository;
import cz.tournament.control.service.AllVersusAllService;
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
 * Test class for the AllVersusAllResource REST controller.
 *
 * @see AllVersusAllResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournamentControlApp.class)
public class AllVersusAllResourceIntTest {

    private static final Integer DEFAULT_NUMBER_OF_MUTUAL_MATCHES = 1;
    private static final Integer UPDATED_NUMBER_OF_MUTUAL_MATCHES = 2;

    @Autowired
    private AllVersusAllRepository allVersusAllRepository;

    @Autowired
    private AllVersusAllService allVersusAllService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restAllVersusAllMockMvc;

    private AllVersusAll allVersusAll;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AllVersusAllResource allVersusAllResource = new AllVersusAllResource(allVersusAllService);
        this.restAllVersusAllMockMvc = MockMvcBuilders.standaloneSetup(allVersusAllResource)
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
    public static AllVersusAll createEntity(EntityManager em) {
        AllVersusAll allVersusAll = new AllVersusAll()
            .numberOfMutualMatches(DEFAULT_NUMBER_OF_MUTUAL_MATCHES);
        return allVersusAll;
    }

    @Before
    public void initTest() {
        allVersusAll = createEntity(em);
    }

    @Test
    @Transactional
    public void createAllVersusAll() throws Exception {
        int databaseSizeBeforeCreate = allVersusAllRepository.findAll().size();

        // Create the AllVersusAll
        restAllVersusAllMockMvc.perform(post("/api/all-versus-alls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(allVersusAll)))
            .andExpect(status().isCreated());

        // Validate the AllVersusAll in the database
        List<AllVersusAll> allVersusAllList = allVersusAllRepository.findAll();
        assertThat(allVersusAllList).hasSize(databaseSizeBeforeCreate + 1);
        AllVersusAll testAllVersusAll = allVersusAllList.get(allVersusAllList.size() - 1);
        assertThat(testAllVersusAll.getNumberOfMutualMatches()).isEqualTo(DEFAULT_NUMBER_OF_MUTUAL_MATCHES);
    }

    @Test
    @Transactional
    public void createAllVersusAllWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = allVersusAllRepository.findAll().size();

        // Create the AllVersusAll with an existing ID
        allVersusAll.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAllVersusAllMockMvc.perform(post("/api/all-versus-alls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(allVersusAll)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<AllVersusAll> allVersusAllList = allVersusAllRepository.findAll();
        assertThat(allVersusAllList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNumberOfMutualMatchesIsRequired() throws Exception {
        int databaseSizeBeforeTest = allVersusAllRepository.findAll().size();
        // set the field null
        allVersusAll.setNumberOfMutualMatches(null);

        // Create the AllVersusAll, which fails.

        restAllVersusAllMockMvc.perform(post("/api/all-versus-alls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(allVersusAll)))
            .andExpect(status().isBadRequest());

        List<AllVersusAll> allVersusAllList = allVersusAllRepository.findAll();
        assertThat(allVersusAllList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAllVersusAlls() throws Exception {
        // Initialize the database
        allVersusAllRepository.saveAndFlush(allVersusAll);

        // Get all the allVersusAllList
        restAllVersusAllMockMvc.perform(get("/api/all-versus-alls?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(allVersusAll.getId().intValue())))
            .andExpect(jsonPath("$.[*].numberOfMutualMatches").value(hasItem(DEFAULT_NUMBER_OF_MUTUAL_MATCHES)));
    }

    @Test
    @Transactional
    public void getAllVersusAll() throws Exception {
        // Initialize the database
        allVersusAllRepository.saveAndFlush(allVersusAll);

        // Get the allVersusAll
        restAllVersusAllMockMvc.perform(get("/api/all-versus-alls/{id}", allVersusAll.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(allVersusAll.getId().intValue()))
            .andExpect(jsonPath("$.numberOfMutualMatches").value(DEFAULT_NUMBER_OF_MUTUAL_MATCHES));
    }

    @Test
    @Transactional
    public void getNonExistingAllVersusAll() throws Exception {
        // Get the allVersusAll
        restAllVersusAllMockMvc.perform(get("/api/all-versus-alls/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAllVersusAll() throws Exception {
        // Initialize the database
        allVersusAllService.save(allVersusAll);

        int databaseSizeBeforeUpdate = allVersusAllRepository.findAll().size();

        // Update the allVersusAll
        AllVersusAll updatedAllVersusAll = allVersusAllRepository.findOne(allVersusAll.getId());
        updatedAllVersusAll
            .numberOfMutualMatches(UPDATED_NUMBER_OF_MUTUAL_MATCHES);

        restAllVersusAllMockMvc.perform(put("/api/all-versus-alls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAllVersusAll)))
            .andExpect(status().isOk());

        // Validate the AllVersusAll in the database
        List<AllVersusAll> allVersusAllList = allVersusAllRepository.findAll();
        assertThat(allVersusAllList).hasSize(databaseSizeBeforeUpdate);
        AllVersusAll testAllVersusAll = allVersusAllList.get(allVersusAllList.size() - 1);
        assertThat(testAllVersusAll.getNumberOfMutualMatches()).isEqualTo(UPDATED_NUMBER_OF_MUTUAL_MATCHES);
    }

    @Test
    @Transactional
    public void updateNonExistingAllVersusAll() throws Exception {
        int databaseSizeBeforeUpdate = allVersusAllRepository.findAll().size();

        // Create the AllVersusAll

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restAllVersusAllMockMvc.perform(put("/api/all-versus-alls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(allVersusAll)))
            .andExpect(status().isCreated());

        // Validate the AllVersusAll in the database
        List<AllVersusAll> allVersusAllList = allVersusAllRepository.findAll();
        assertThat(allVersusAllList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteAllVersusAll() throws Exception {
        // Initialize the database
        allVersusAllService.save(allVersusAll);

        int databaseSizeBeforeDelete = allVersusAllRepository.findAll().size();

        // Get the allVersusAll
        restAllVersusAllMockMvc.perform(delete("/api/all-versus-alls/{id}", allVersusAll.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<AllVersusAll> allVersusAllList = allVersusAllRepository.findAll();
        assertThat(allVersusAllList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AllVersusAll.class);
    }
}
