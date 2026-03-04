package org.mthambipillai.checkerapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    private boolean devMode;
    private String exerciseName;
    private String subnet;
    private List<String> ipList;
}
