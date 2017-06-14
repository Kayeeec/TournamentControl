package cz.tournament.control;

import cz.tournament.control.config.ApplicationProperties;
import cz.tournament.control.config.DefaultProfileUtil;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.ParticipantRepository;
import cz.tournament.control.repository.PlayerRepository;
import cz.tournament.control.repository.UserRepository;

import io.github.jhipster.config.JHipsterConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

@ComponentScan
@EnableAutoConfiguration(exclude = {MetricFilterAutoConfiguration.class, MetricRepositoryAutoConfiguration.class})
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
public class TournamentControlApp {

    private static final Logger log = LoggerFactory.getLogger(TournamentControlApp.class);

    private final Environment env;
    
    private final UserRepository userRepository;
    
    private final PlayerRepository playerRepository;
    
    private final ParticipantRepository participantRepository;

    public TournamentControlApp(Environment env, UserRepository userRepository, PlayerRepository playerRepository, ParticipantRepository participantRepository) {
        this.env = env;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.participantRepository = participantRepository;
    }
    
    /**
     * Initializes TournamentControl.
     * <p>
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href="http://jhipster.github.io/profiles/">http://jhipster.github.io/profiles/</a>.
     */
    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " +
                "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not" +
                "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
        generateUsersPlayersIfNone();
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(TournamentControlApp.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\t{}://localhost:{}\n\t" +
                "External: \t{}://{}:{}\n\t" +
                "Profile(s): \t{}\n----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            protocol,
            env.getProperty("server.port"),
            protocol,
            InetAddress.getLocalHost().getHostAddress(),
            env.getProperty("server.port"),
            env.getActiveProfiles());
    }
    
    private void generateUsersPlayersIfNone(){
        User user = userRepository.findOneByLogin("user").get();
        if(playerRepository.findByUser(user).isEmpty()){
            log.info("No existing players for 'User', creating players.");
            String[] names = {"Ursula", "Utrecht", "Uher", "Uma", "Ulman", "Ulisses", "Xaver", "Xena", "Oxer"};
            for (String name : names) {
                Player player = playerRepository.save(new Player().name(name).user(user));
                Participant participant = participantRepository.save(new Participant(player, user));
            }
        }  
    }
    
}
