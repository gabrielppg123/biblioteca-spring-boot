
package br.com.gabrielppg.biblioteca.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ConfiguracaoBancoDados {

    @Bean
    public DataSource hikariDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driver,
            @Value("${spring.datasource.hikari.maximum-pool-size:10}") int tamanhoMaximo,
            @Value("${spring.datasource.hikari.minimum-idle:1}") int minimoOcioso,
            @Value("${spring.datasource.hikari.max-lifetime:600000}") long tempoMaximoVida,
            @Value("${spring.datasource.hikari.connection-timeout:30000}") long tempoEsperaConexao
    ) {

        HikariConfig config = new HikariConfig();

        // Configuração de conexão
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);

        // Configuração manual do pool
        config.setPoolName("biblioteca-api-pool");
        config.setMaximumPoolSize(tamanhoMaximo);
        config.setMinimumIdle(minimoOcioso);
        config.setMaxLifetime(tempoMaximoVida);
        config.setConnectionTimeout(tempoEsperaConexao);

        return new HikariDataSource(config);
    }
}
