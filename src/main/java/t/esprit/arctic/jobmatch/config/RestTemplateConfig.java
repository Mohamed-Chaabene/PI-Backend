<<<<<<< HEAD
// src/main/java/t/esprit/arctic/jobmatch/config/RestTemplateConfig.java
=======
// Fichier: config/RestTemplateConfig.java
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
package t.esprit.arctic.jobmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}