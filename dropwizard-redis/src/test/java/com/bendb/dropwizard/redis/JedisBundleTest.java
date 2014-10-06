package com.bendb.dropwizard.redis;

import com.bendb.dropwizard.redis.jersey.JedisInjectableProvider;
import com.bendb.dropwizard.redis.jersey.JedisPoolInjectableProvider;
import com.bendb.dropwizard.redis.jersey.JedisResourceMethodDispatchAdapter;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.JedisPool;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JedisBundleTest {
    static class TestConfig extends Configuration {}

    @Mock Environment environment;
    @Mock JerseyEnvironment jerseyEnvironment;
    @Mock LifecycleEnvironment lifecycleEnvironment;
    @Mock HealthCheckRegistry healthChecks;
    @Mock TestConfig config;
    @Mock JedisFactory jedisFactory;
    @Mock JedisPool pool;

    private JedisBundle<TestConfig> bundle;

    @Before
    public void setup() {
        bundle = new JedisBundle<TestConfig>() {
            @Override
            public JedisFactory getJedisFactory(TestConfig configuration) {
                return jedisFactory;
            }
        };

        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

        when(jedisFactory.build(environment)).thenReturn(pool);
    }

    @Test
    public void createsJedisPoolFromFactory() throws Exception {
        bundle.run(config, environment);
        verify(jedisFactory).build(environment);
        assertThat(bundle.getPool()).is(pool);
    }

    @Test
    public void registersHealthCheck() throws Exception {
        bundle.run(config, environment);

        ArgumentCaptor<JedisHealthCheck> captor = ArgumentCaptor.forClass(JedisHealthCheck.class);
        verify(healthChecks).register(eq("redis"), captor.capture());

        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersJedisInjectableProvider() throws Exception {
        bundle.run(config, environment);

        ArgumentCaptor<JedisInjectableProvider> captor = ArgumentCaptor.forClass(JedisInjectableProvider.class);
        verify(jerseyEnvironment, atLeastOnce()).register(captor.capture());

        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersJedisPoolInjectableProvider() throws Exception {
        bundle.run(config, environment);

        ArgumentCaptor<JedisPoolInjectableProvider> captor = ArgumentCaptor.forClass(JedisPoolInjectableProvider.class);
        verify(jerseyEnvironment, atLeastOnce()).register(captor.capture());

        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersMethodAdapter() throws Exception {
        bundle.run(config, environment);

        ArgumentCaptor<JedisResourceMethodDispatchAdapter> captor = ArgumentCaptor.forClass(JedisResourceMethodDispatchAdapter.class);
        verify(jerseyEnvironment, atLeastOnce()).register(captor.capture());

        assertThat(captor.getValue()).isNotNull();
    }
}