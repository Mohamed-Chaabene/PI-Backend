package t.esprit.arctic.jobmatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client-freelance")
public class ClientFreelanceController {

    @GetMapping("/test")
    public String test() {
        return "CLIENT_FREELANCE OK";
    }
}

