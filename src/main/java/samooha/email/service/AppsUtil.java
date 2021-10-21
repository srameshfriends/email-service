package samooha.email.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;

import javax.activation.FileDataSource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AppsUtil {
    private static final Logger logger = LogManager.getLogger(AppsUtil.class);
    private static final SimpleDateFormat LOG_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MMM");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static AppsConfig appsConfig;

    public static String reload(boolean isRefresh) {
        if(isRefresh) {
            appsConfig = null;
        }
        AppsConfig config = getAppsConfig();
        if(config == null){
            logger.error("Application configuration should not be empty");
            return "Application configuration should not be empty";
        }
        if (config.getFromAddress() == null) {
            AppsUtil.log("From email address should not be empty.");
            return "From email address should not be empty.";
        } else if (config.getToAddresses() == null) {
            AppsUtil.log("To email address should not be empty.");
            return "To email address should not be empty.";
        } else if (config.getMailHost() == null) {
            AppsUtil.log("Email host not found.");
            return "Email host not found.";
        } else if (config.getMailUser() == null) {
            AppsUtil.log("Email user not found.");
            return "Email user not found.";
        } else if (config.getMailPassword() == null) {
            AppsUtil.log("Email password not found.");
            return "Email password not found.";
        }
        Mailer mailer = MailerBuilder.withSMTPServer(config.getMailHost(), config.getMailPort(), config.getMailUser(),
                config.getMailPassword())
                .buildMailer();
        config.setMailer(mailer);
        return null;
    }

    public static void log(String content) {
        Path logPath;
        if(appsConfig == null) {
            Properties prop = getConfigurationProperties();
            logPath = getLoggerFile(prop.getProperty("mail.logs.dir"));
        } else {
            logPath = getLoggerFile(appsConfig.getLogsDir());
        }
        if(logPath == null) {
            logger.info("Activity Log: " + content);
        } else {
            try {
                content = LOG_DATE_TIME.format(new Date()) + "\t" + content + LINE_SEPARATOR;
                Files.write(logPath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch(IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    public static String getLog(Date date) {
        return getLog(getAppsConfig(), date);
    }

    public static String getLog(AppsConfig config, Date date) {
        if(config == null) {
            return "Application configuration file not found to read logs.";
        } else if(date == null) {
            return "Log date should not be empty.";
        }
        try {
            String logSubDir = MONTH_FORMAT.format(date);
            String fileName = DATE_FORMAT.format(date);
            Path path = Paths.get(config.getLogsDir(), logSubDir, fileName + ".log");
            if(Files.notExists(path)){
                return "No logs has recorded on " + DATE_FORMAT.format(date);
            }
            return Files.readString(path);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return "No logs has recorded on " + DATE_FORMAT.format(date);
    }

    public static String getEmailContent() {
        try {
            Path path = Paths.get(getAppsConfig().getContentFile());
            if(Files.notExists(path) && Files.isRegularFile(path)) {
                return "This is auto generated mail, no need to be reply.";
            }
            return Files.readString(path);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return "This is auto generated mail, no need to be reply.";
    }

    private static AppsConfig createAppsConfig(Properties prop) {
        String logDir = prop.getProperty("mail.logs.dir");
        String contentFile = prop.getProperty("mail.content.file");
        String attachmentsDir = prop.getProperty("mail.attachments.dir");
        String attachmentsArchiveDir = prop.getProperty("mail.attachments-archive.dir");
        if(logDir == null) {
            logger.error("Log directory config missing.");
            return null;
        }
        if(contentFile == null) {
            logger.error("Content file config missing.");
            return null;
        } else if(attachmentsDir == null) {
            logger.error("Attachments directory config missing.");
            return null;
        }
        boolean isDeleteAttachment = isTrue(prop.getProperty("mail.attachments.delete"));
        String fromAddress = prop.getProperty("mail.from");
        String subject = prop.getProperty("mail.subject");
        subject = subject == null ? "Auto Generated" : subject;
        String tempAddress = prop.getProperty("mail.to");
        String[] tempArray = tempAddress == null ? null : tempAddress.split(",");
        List<String> toAddresses = null;
        if(tempArray != null) {
            toAddresses = new ArrayList<>();
            for(String add : tempArray) {
                if(!add.isBlank()) {
                    toAddresses.add(add.trim());
                }
            }
        }
        String ccAdd = prop.getProperty("mail.cc");
        tempArray = tempAddress == null ? null : ccAdd.split(",");
        List<String> ccAddresses = null;
        if(tempArray != null) {
            ccAddresses = new ArrayList<>();
            for(String cdd : tempArray) {
                ccAddresses.add(cdd.trim());
            }
        }
        AppsConfig config = new AppsConfig(logDir, contentFile, attachmentsDir, attachmentsArchiveDir);
        config.setServiceBy(prop.getProperty("mail.service.by"));
        config.setDeleteArchive(isDeleteAttachment);
        config.setSubject(subject);
        try {
            config.setAddress(getAddress(fromAddress), getAddresses(toAddresses), getAddresses(ccAddresses));
            AppsUtil.log(" ****************** EMAIL CONFIGURATION ****************** ");
            AppsUtil.log("Service By : " + config.getServiceBy());
            AppsUtil.log("Subject : " + config.getSubject());
            AppsUtil.log("From : " + config.getFromAddress().getAddress());
            AppsUtil.log("To : " + getAddressText(config.getToAddresses()));
            AppsUtil.log("CC : " + getAddressText(config.getCcAddresses()));
        } catch (AddressException exe) {
            exe.printStackTrace();
        }
        return config;
    }

    private static String getAddressText(InternetAddress[] addresses) {
        if(addresses != null) {
            StringBuilder builder = new StringBuilder();
            for(InternetAddress address : addresses) {
                builder.append(address.getAddress()).append(", ");
            }
            if(builder.toString().endsWith(", ")) {
                return builder.substring(0, builder.length() - 2);
            }
            return builder.toString();
        }
        return "";
    }

    private static InternetAddress getAddress(String text) throws AddressException {
        if(text != null && !text.isEmpty()) {
            return new InternetAddress(text);
        }
        return null;
    }

    private static InternetAddress[] getAddresses(List<String> addressList) throws AddressException {
        if(addressList != null && !addressList.isEmpty()) {
            List<InternetAddress> addresses = new ArrayList<>();
            for(String text : addressList) {
                Collections.addAll(addresses, new InternetAddress(text.trim()));
            }
            return addresses.isEmpty() ? null : addresses.toArray(new InternetAddress[0]);
        }
        return null;
    }

    public static AppsConfig getAppsConfig() {
        if(appsConfig == null) {
            AppsConfig config = createAppsConfig(getConfigurationProperties());
            if(config != null) {
                Path emailPropPath = Paths.get(System.getProperty("user.dir"), "config", "email.properties");
                config.setMailProperties(loadProperties(emailPropPath));
                appsConfig = config;
            }
        }
        return appsConfig;
    }

    private static Properties getConfigurationProperties() {
        Path appsProPath = Paths.get(System.getProperty("user.dir"), "config", "apps.properties");
        return AppsUtil.loadProperties(appsProPath);
    }

    public static Properties loadProperties(Path path) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(path.toFile())){
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Map<Path, FileDataSource> getAttachments() {
        Map<Path, FileDataSource> dataSourceHashMap = new HashMap<>();
        try {
            Path attachmentPath = Paths.get(getAppsConfig().getAttachmentDir());
            if(Files.notExists(attachmentPath)) {
                log("Email attachments directory are missing.");
            } else {
                DirectoryStream<Path> paths = Files.newDirectoryStream(attachmentPath);
                for (Path path : paths) {
                    dataSourceHashMap.put(path, new FileDataSource(path.toFile()));
                }
            }
        } catch (IOException etc) {
            etc.printStackTrace();
        }
        return dataSourceHashMap;
    }

    private static Path getLoggerFile(String logDir) {
        if(logDir == null) {
            return null;
        }
        try {
            Path path = createOrRetrieveDirectory(logDir, MONTH_FORMAT.format(new Date()));
            if(path != null) {
                return createOrRetrieveFile(path, DATE_FORMAT.format(new Date()) + ".log");
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    private static Path createOrRetrieveFile(Path dir, String fileName) throws IOException {
        Path path = Paths.get(dir.toString(), fileName);
        if(Files.notExists(path)){
            logger.info("File (" + path + ") has created.");
            return Files.createFile(path);
        }
        return path;
    }

    private static Path createOrRetrieveDirectory(String target, String subDir) throws IOException {
        Path path = subDir == null ? Paths.get(target) : Paths.get(target, subDir);
        if(Files.notExists(path)){
            return Files.createDirectories(path);
        }
        return path;
    }

    private static boolean isTrue(String text) {
        return text != null && text.equalsIgnoreCase("true");
    }
}
