package samooha.email.service;

import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@RequestMapping("/api/log")
public class EmailLogController {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value="/read", method = RequestMethod.GET)
    public @ResponseBody String load(@RequestParam("date") String loggingDate) {
        try {
            Date date = dateFormat.parse(loggingDate);
            return AppsUtil.getLog(date);
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
}
