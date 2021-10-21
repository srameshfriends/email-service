package samooha.email.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.AsyncResponse;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;

@Component
public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @PostConstruct
    public void execute() {
        AppsUtil.reload(true);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void executeTask() {
        AppsConfig config = AppsUtil.getAppsConfig();
        if(config == null) {
            logger.error("Application configuration not found.");
        } else if (config.getMailer() == null) {
            logger.error("Email configuration invalid, email scheduler not perform their action and exited.");
        } else {
            AppsUtil.log(" ****************** EMAIL SERVICE STARTED  ****************** ");
            final Map<Path, FileDataSource> dataSourceMap = AppsUtil.getAttachments();
            if (dataSourceMap.size() == 0) {
                AppsUtil.log("Attachments not found to send email.");
            } else {
                MailPostEvent event = new MailPostEvent(config, dataSourceMap);
                event.send();
            }
        }
    }

    private static final class MailPostEvent {
        private final Map<Path, FileDataSource> dataSourceMap;
        private final AppsConfig config;

        MailPostEvent(AppsConfig config, Map<Path, FileDataSource> fileDataMap) {
            this.config = config;
            this.dataSourceMap = fileDataMap;
        }

        void send() {
            EmailPopulatingBuilder builder = EmailBuilder.startingBlank()
                    .from(config.getServiceBy(), config.getFromAddress())
                    .to("", config.getToAddresses());
            if (config.getCcAddresses() != null) {
                builder.cc("", config.getCcAddresses());
            }
            builder.withSubject(config.getSubject());
            if (config.isHtmlContent()) {
                builder.appendTextHTML(AppsUtil.getEmailContent());
            } else {
                builder.appendText(AppsUtil.getEmailContent());
            }
            if (!dataSourceMap.isEmpty()) {
                dataSourceMap.forEach((path, dataSource) -> {
                    AppsUtil.log("Attachment: " + path.getFileName().toString());
                    builder.withAttachment(path.getFileName().toString(), dataSource);
                });
            }
            try {
                AsyncResponse asyncResponse = config.getMailer().sendMail(builder.buildEmail(), true);
                if (asyncResponse == null) {
                    AppsUtil.log("Async response returned with empty.");
                } else {
                    asyncResponse.onSuccess(() -> {
                        afterSendEvent(true);
                        AppsUtil.log("Successfully completed the email service task.");
                    });
                    asyncResponse.onException(error -> {
                        afterSendEvent(false);
                        AppsUtil.log("ERROR: " + error.getMessage());
                    });
                }
            } catch (Exception ex) {
                afterSendEvent(false);
                AppsUtil.log("ERROR: " + ex.getMessage());
            }
        }

        void afterSendEvent(boolean status) {
            Path path = Paths.get(config.getAttachmentsArchiveDir());
            if (Files.notExists(path)) {
                if (status) {
                    deleteFiles(dataSourceMap.keySet());
                }
            } else {
                if (status && config.isDeleteArchive()) {
                    deleteFiles(dataSourceMap.keySet());
                } else if (status) {
                    move(path.toFile(), dataSourceMap.keySet());
                }
            }
        }

        void deleteFiles(Set<Path> pathSet) {
            for (Path path : pathSet) {
                try {
                    Files.delete(path);
                } catch (IOException ex) {
                    AppsUtil.log("DELETE ERROR: After send attachments would not be delete file, "
                            + path + " \t " + ex.getMessage());
                }
            }
        }

        void move(File target, Set<Path> pathSet) {
            for (Path path : pathSet) {
                try {
                    Files.move(path, Paths.get(target.getPath(), path.toFile().getName()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    AppsUtil.log("DELETE ERROR: After send attachments would not be move file, "
                            + path + " \t " + ex.getMessage());
                }
            }
        }
    }
}
