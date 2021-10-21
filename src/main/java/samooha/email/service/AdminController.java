package samooha.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ApplicationContext context;

    @Autowired
    public AdminController(ApplicationContext context) {
        this.context = context;
    }

    @RequestMapping(value="/reload/settings", method = RequestMethod.GET)
    public @ResponseBody String reloadSettings() {
         String status = AppsUtil.reload(true);
         return  status == null ? "Application configuration loaded successfully." : status;
    }

    @RequestMapping("/shutdown")
    public void shutdown() {
        SpringApplication.exit(context, () -> 0);
    }
}
