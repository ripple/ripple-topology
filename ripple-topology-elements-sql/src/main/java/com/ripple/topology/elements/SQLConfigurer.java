package com.ripple.topology.elements;

import com.google.common.base.Preconditions;
import com.ripple.topology.PropertiesAware;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import com.ripple.topology.io.Content;
import com.ripple.topology.utils.HealthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.sql.Driver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jfulton
 */
public class SQLConfigurer implements Configurer, VariableResolverAware, PropertiesAware<SQLConfigurer> {

    private static final Logger logger = LoggerFactory.getLogger(SQLConfigurer.class);
    private static final Duration MAX_WAIT = Duration.ofMinutes(3);
    private static final Duration RETRY_PAUSE = Duration.ofMillis(500);
    private Class<? extends Driver> driverClass;
    private String jdbcUrl;
    private SQLCredentials credentials;
    private List<String> statements = new ArrayList<>();
    private List<String> statementFiles = new ArrayList<>();
    private VariableResolver resolver;
    private Properties properties = new Properties();

    public SQLConfigurer() {
        // Jackson Only
    }

    public SQLConfigurer(final Class<? extends Driver> driverClass, final String jdbcUrl,
        final SQLCredentials credentials) {
        this.driverClass = Preconditions.checkNotNull(driverClass, "driverClass");
        this.jdbcUrl = Preconditions.checkNotNull(jdbcUrl, "jdbcUrl");
        Preconditions.checkArgument(jdbcUrl.length() > 0, "jdbcUrl");
        this.credentials = Preconditions.checkNotNull(credentials, "credentials");
    }

    @Override
    public void configure(final Topology topology) {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(getDriverClass());
        dataSource.setUrl(getJdbcUrl());
        dataSource.setUsername(credentials.getUsername());
        dataSource.setPassword(credentials.getPassword());
        dataSource.setConnectionProperties(properties);

        AtomicReference<JdbcTemplate> jdbcTemplateRef = new AtomicReference<>();
        HealthUtils.waitForHealth(MAX_WAIT, RETRY_PAUSE, "'" + getJdbcUrl() + "'", () -> {
            try {
                jdbcTemplateRef.set(new JdbcTemplate(dataSource, false));
                return true;
            } catch (Exception ex) {
                return false;
            }
        });

        if (jdbcTemplateRef.get() != null) {
            logger.info("JDBC Connection established to '{}'", getJdbcUrl());
        } else {
            logger.warn("JDBC Connection to '{}' could not be established within {} millis", getJdbcUrl(), MAX_WAIT.toMillis());
        }

        for (String statement : statements) {
            jdbcTemplateRef.get().execute(statement);
        }

        for (String statementFile : statementFiles) {
            Content content = topology.contentLoader().getContent(statementFile);
            if (content.exists()) {
                jdbcTemplateRef.get().execute(resolver.resolve(content.asUTF8String()));
            }
        }
    }

    public SQLCredentials getCredentials() {
        return credentials;
    }

    public SQLConfigurer setCredentials(final SQLCredentials credentials) {
        this.credentials = Objects.requireNonNull(credentials);
        return this;
    }

    public Class<? extends Driver> getDriverClass() {
        return driverClass;
    }

    public SQLConfigurer setDriverClass(final Class<? extends Driver> driverClass) {
        this.driverClass = Objects.requireNonNull(driverClass);
        return this;
    }

    public List<String> getStatements() {
        return statements;
    }

    public SQLConfigurer addStatement(String statement) {
        getStatements().add(statement);
        return this;
    }

    /**
     * A list of paths, resolvable by a {@link com.ripple.topology.io.ContentLoader}, containing executable SQL
     * statements.  Both the file names, as well as the file contents support variable resolution.
     *
     * @return SQL file paths
     */
    public List<String> getStatementFiles() {
        return statementFiles;
    }

    /**
     * A convenience builder method for adding SQL file paths to {@link #getStatementFiles()}.
     *
     * @param statementFile a path resolvable by a {@link com.ripple.topology.io.ContentLoader}.
     * @return this
     */
    public SQLConfigurer addStatementfile(String statementFile) {
        getStatementFiles().add(statementFile);
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public SQLConfigurer setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl);
        return this;
    }

    @Override
    public void resolveVariables(final Topology topology, final VariableResolver resolver) {
        this.resolver = resolver;
        for (int i = 0; i < statements.size(); i++) {
            statements.set(i, resolver.resolve(statements.get(i)));
        }
        for (int i = 0; i < statementFiles.size(); i++) {
            statementFiles.set(i, resolver.resolve(statementFiles.get(i)));
        }
        jdbcUrl = resolver.resolve(jdbcUrl);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
