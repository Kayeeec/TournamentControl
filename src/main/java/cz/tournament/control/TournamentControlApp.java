package cz.tournament.control;

import cz.tournament.control.config.ApplicationProperties;
import cz.tournament.control.config.DefaultProfileUtil;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.ParticipantRepository;
import cz.tournament.control.repository.PlayerRepository;
import cz.tournament.control.repository.TeamRepository;
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
import java.util.List;

@ComponentScan
@EnableAutoConfiguration(exclude = {MetricFilterAutoConfiguration.class, MetricRepositoryAutoConfiguration.class})
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
public class TournamentControlApp {

    private static final Logger log = LoggerFactory.getLogger(TournamentControlApp.class);

    private final Environment env;
    
    private final UserRepository userRepository;
    
    private final PlayerRepository playerRepository;
    
    private final TeamRepository teamRepository;
    
    private final ParticipantRepository participantRepository;

    public TournamentControlApp(Environment env, UserRepository userRepository, PlayerRepository playerRepository, TeamRepository teamRepository, ParticipantRepository participantRepository) {
        this.env = env;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
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
        
        
        generateUsersParticipantsIfNone();
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
    
    private void generateUsersParticipantsIfNone(){
        User user = userRepository.findOneByLogin("user").get();
        if(playerRepository.findByUser(user).isEmpty()){
            log.info("No existing players for 'User', creating players.");
            String[] names = {"Ursula", "Utrecht", "Uher", "Uma", "Ulman", "Ulisses",
                            "Xaver", "Xena","Xenie","Xander", "Xaya", "Xing",
                            "Oliver", "Oxer", "Olga", "Otto", "Oskar", "Olivia",
                            "Adam", "Eve", "Steve"};
            for (String name : names) {
                Player player = playerRepository.save(new Player().name(name).user(user));
                Participant participant = participantRepository.save(new Participant(player, user));
            }
        }
        
        if(teamRepository.findByUser(user).isEmpty()){
            log.info("No existing teams for 'User', creating teams.");
            Team u = teamRepository.save(new Team().name("U team").note("all players with u").user(user));
            String[] u_names = {"Ursula", "Utrecht", "Uher", "Uma", "Ulman", "Ulisses"};
            for (String name : u_names) {
                List<Player> fromDB = playerRepository.findByName(name);
                if(fromDB.isEmpty()){
                    fromDB.add(playerRepository.save(new Player().name(name).user(user)));
                }
                u = u.addMembers(fromDB.get(0));
            }
            teamRepository.save(u);
            participantRepository.save(new Participant(u, user));
            
            Team x = teamRepository.save(new Team().name("X team").note("all players with x").user(user));
            String[] x_names = {"Xaver", "Xena","Xenie","Xander", "Xaya", "Xing"};
            for (String name : x_names) {
                List<Player> fromDB = playerRepository.findByName(name);
                if(fromDB.isEmpty()){
                    fromDB.add(playerRepository.save(new Player().name(name).user(user)));
                }
                x.addMembers(fromDB.get(0));
            }
            teamRepository.save(x);
            participantRepository.save(new Participant(x, user));
            
            Team o = teamRepository.save(new Team().name("O team").note("all players with o").user(user));
            String[] o_names = {"Oliver", "Oxer", "Olga", "Otto", "Oskar", "Olivia"};
            for (String name : o_names) {
                List<Player> fromDB = playerRepository.findByName(name);
                if(fromDB.isEmpty()){
                    fromDB.add(playerRepository.save(new Player().name(name).user(user)));
                }
                o.addMembers(fromDB.get(0));
            }
            teamRepository.save(o);
            participantRepository.save(new Participant(o, user));
            
            Team ux = teamRepository.save(new Team().name("UX team").note("some u players an some x players").user(user));
            String[] ux_names = {"Ursula", "Utrecht","Xena","Xenie"};
            for (String name : ux_names) {
                List<Player> fromDB = playerRepository.findByName(name);
                if(fromDB.isEmpty()){
                    fromDB.add(playerRepository.save(new Player().name(name).user(user)));
                }
                ux.addMembers(fromDB.get(0));
            }
            teamRepository.save(ux);
            participantRepository.save(new Participant(ux, user));
            
            Team tmuxo = teamRepository.save(new Team().name("TMUXO team").note("one of each plus 3").user(user));
            String[] tmuxo_names = {"Adam", "Eve", "Steve", "Uma", "Xaya", "Olivia"};
            for (String name : tmuxo_names) {
                List<Player> fromDB = playerRepository.findByName(name);
                if(fromDB.isEmpty()){
                    fromDB.add(playerRepository.save(new Player().name(name).user(user)));
                }
                tmuxo.addMembers(fromDB.get(0));
            }
            teamRepository.save(tmuxo);
            participantRepository.save(new Participant(tmuxo, user));
            
        }
    }
    
}
