package cz.tournament.control.web.rest;

import cz.tournament.control.TournamentControlApp;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.repository.SetSettingsRepository;
import cz.tournament.control.service.SetSettingsService;
import static cz.tournament.control.web.rest.TestUtil.createFormattingConversionService;
import cz.tournament.control.web.rest.errors.ExceptionTranslator;
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
 * Test class for the SetSettingsResource REST controller.
 *
 * @see SetSettingsResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TournamentControlApp.class)
public class SetSettingsResourceIntTest {

    private static final Integer DEFAULT_MAX_SCORE = 1;
    private static final Integer UPDATED_MAX_SCORE = 2;

    private static final Integer DEFAULT_MIN_REACHED_SCORE = 0;
    private static final Integer UPDATED_MIN_REACHED_SCORE = 1;

    private static final Integer DEFAULT_LEAD_BY_POINTS = 1;
    private static final Integer UPDATED_LEAD_BY_POINTS = 2;

    @Autowired
    private SetSettingsRepository setSettingsRepository;

    @Autowired
    private SetSettingsService setSettingsService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSetSettingsMockMvc;

    private SetSettings setSettings;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SetSettingsResource setSettingsResource = new SetSettingsResource(setSettingsService);
        this.restSetSettingsMockMvc = MockMvcBuilders.standaloneSetup(setSettingsResource)
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
    public static SetSettings createEntity(EntityManager em) {
        SetSettings setSettings = new SetSettings()
            .maxScore(DEFAULT_MAX_SCORE)
            .minReachedScore(DEFAULT_MIN_REACHED_SCORE)
            .leadByPoints(DEFAULT_LEAD_BY_POINTS);
        return setSettings;
    }

    @Before
    public void initTest() {
        setSettings = createEntity(em);
    }

    @Test
    @Transactional
    public void createSetSettings() throws Exception {
        int databaseSizeBeforeCreate = setSettingsRepository.findAll().size();

        // Create the SetSettings
        restSetSettingsMockMvc.perform(post("/api/set-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(setSettings)))
            .andExpect(status().isCreated());

        // Validate the SetSettings in the database
        List<SetSettings> setSettingsList = setSettingsRepository.findAll();
        assertThat(setSettingsList).hasSize(databaseSizeBeforeCreate + 1);
        SetSettings testSetSettings = setSettingsList.get(setSettingsList.size() - 1);
        assertThat(testSetSettings.getMaxScore()).isEqualTo(DEFAULT_MAX_SCORE);
        assertThat(testSetSettings.getMinReachedScore()).isEqualTo(DEFAULT_MIN_REACHED_SCORE);
        assertThat(testSetSettings.getLeadByPoints()).isEqualTo(DEFAULT_LEAD_BY_POINTS);
    }

    @Test
    @Transactional
    public void createSetSettingsWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = setSettingsRepository.findAll().size();

        // Create the SetSettings with an existing ID
        setSettings.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSetSettingsMockMvc.perform(post("/api/set-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(setSettings)))
            .andExpect(status().isBadRequest());

        // Validate the SetSettings in the database
        List<SetSettings> setSettingsList = setSettingsRepository.findAll();
        assertThat(setSettingsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllSetSettings() throws Exception {
        // Initialize the database
        setSettingsRepository.saveAndFlush(setSettings);

        // Get all the setSettingsList
        restSetSettingsMockMvc.perform(get("/api/set-settings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(setSettings.getId().intValue())))
            .andExpect(jsonPath("$.[*].maxScore").value(hasItem(DEFAULT_MAX_SCORE)))
            .andExpect(jsonPath("$.[*].minReachedScore").value(hasItem(DEFAULT_MIN_REACHED_SCORE)))
            .andExpect(jsonPath("$.[*].leadByPoints").value(hasItem(DEFAULT_LEAD_BY_POINTS)));
    }

    @Test
    @Transactional
    public void getSetSettings() throws Exception {
        // Initialize the database
        setSettingsRepository.saveAndFlush(setSettings);

        // Get the setSettings
        restSetSettingsMockMvc.perform(get("/api/set-settings/{id}", setSettings.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(setSettings.getId().intValue()))
            .andExpect(jsonPath("$.maxScore").value(DEFAULT_MAX_SCORE))
            .andExpect(jsonPath("$.minReachedScore").value(DEFAULT_MIN_REACHED_SCORE))
            .andExpect(jsonPath("$.leadByPoints").value(DEFAULT_LEAD_BY_POINTS));
    }

    @Test
    @Transactional
    public void getNonExistingSetSettings() throws Exception {
        // Get the setSettings
        restSetSettingsMockMvc.perform(get("/api/set-settings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSetSettings() throws Exception {
        // Initialize the database
        setSettingsService.save(setSettings);

        int databaseSizeBeforeUpdate = setSettingsRepository.findAll().size();

        // Update the setSettings
        SetSettings updatedSetSettings = setSettingsRepository.findOne(setSettings.getId());
        updatedSetSettings
            .maxScore(UPDATED_MAX_SCORE)
            .minReachedScore(UPDATED_MIN_REACHED_SCORE)
            .leadByPoints(UPDATED_LEAD_BY_POINTS);

        restSetSettingsMockMvc.perform(put("/api/set-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedSetSettings)))
            .andExpect(status().isOk());

        // Validate the SetSettings in the database
        List<SetSettings> setSettingsList = setSettingsRepository.findAll();
        assertThat(setSettingsList).hasSize(databaseSizeBeforeUpdate);
        SetSettings testSetSettings = setSettingsList.get(setSettingsList.size() - 1);
        assertThat(testSetSettings.getMaxScore()).isEqualTo(UPDATED_MAX_SCORE);
        assertThat(testSetSettings.getMinReachedScore()).isEqualTo(UPDATED_MIN_REACHED_SCORE);
        assertThat(testSetSettings.getLeadByPoints()).isEqualTo(UPDATED_LEAD_BY_POINTS);
    }

    @Test
    @Transactional
    public void updateNonExistingSetSettings() throws Exception {
        int databaseSizeBeforeUpdate = setSettingsRepository.findAll().size();

        // Create the SetSettings

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSetSettingsMockMvc.perform(put("/api/set-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(setSettings)))
            .andExpect(status().isCreated());

        // Validate the SetSettings in the database
        List<SetSettings> setSettingsList = setSettingsRepository.findAll();
        assertThat(setSettingsList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSetSettings() throws Exception {
        // Initialize the database
        setSettingsService.save(setSettings);

        int databaseSizeBeforeDelete = setSettingsRepository.findAll().size();

        // Get the setSettings
        restSetSettingsMockMvc.perform(delete("/api/set-settings/{id}", setSettings.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<SetSettings> setSettingsList = setSettingsRepository.findAll();
        assertThat(setSettingsList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SetSettings.class);
        SetSettings setSettings1 = new SetSettings();
        setSettings1.setId(1L);
        SetSettings setSettings2 = new SetSettings();
        setSettings2.setId(setSettings1.getId());
        assertThat(setSettings1).isEqualTo(setSettings2);
        setSettings2.setId(2L);
        assertThat(setSettings1).isNotEqualTo(setSettings2);
        setSettings1.setId(null);
        assertThat(setSettings1).isNotEqualTo(setSettings2);
    }
}
