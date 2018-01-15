package cz.tournament.control.config;

import io.github.jhipster.config.JHipsterProperties;
import java.util.concurrent.TimeUnit;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
@AutoConfigureAfter(value = { MetricsConfiguration.class })
@AutoConfigureBefore(value = { WebConfigurer.class, DatabaseConfiguration.class })
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache =
            jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(ehcache.getTimeToLiveSeconds(), TimeUnit.SECONDS)))
                .build());
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            cm.createCache(cz.tournament.control.domain.User.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Authority.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.User.class.getName() + ".authorities", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.PersistentToken.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.User.class.getName() + ".persistentTokens", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Game.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Participant.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Player.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Player.class.getName() + ".teams", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Team.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Team.class.getName() + ".members", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Tournament.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Tournament.class.getName() + ".matches", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Tournament.class.getName() + ".participants", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.AllVersusAll.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Elimination.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.GameSet.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Game.class.getName() + ".sets", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.SetSettings.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Swiss.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Combined.class.getName(), jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Combined.class.getName() + ".allParticipants", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.Combined.class.getName() + ".groups", jcacheConfiguration);
            cm.createCache(cz.tournament.control.domain.SetSettings.class.getName() + ".tournaments", jcacheConfiguration);
            // jhipster-needle-ehcache-add-entry
        };
    }
}
