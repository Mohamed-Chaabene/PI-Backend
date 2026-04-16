package t.esprit.arctic.jobmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD

@SpringBootApplication
=======
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
public class JobmatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobmatchApplication.class, args);
    }

}
