package samooha.email.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplejavamail.api.mailer.Mailer;

import javax.mail.internet.InternetAddress;
import java.util.Properties;

public class AppsConfig {
    private static final Logger logger = LogManager.getLogger(AppsConfig.class);
    private final String contentFile, attachmentDir, attachmentsArchiveDir, logsDir;
    private Properties mailProperties;
    private String serviceBy, subject;
    private InternetAddress fromAddress;
    private InternetAddress[] toAddresses, ccAddresses;
    private Mailer mailer;
    private boolean isDeleteArchive;

    public AppsConfig(String logsDir, String contentFile, String attachmentDir, String attachmentsArchiveDir) {
        this.logsDir = logsDir;
        this.contentFile = contentFile;
        this.attachmentDir = attachmentDir;
        this.attachmentsArchiveDir = attachmentsArchiveDir;
        this.serviceBy = "";
    }

    public void setMailProperties(Properties mailProperties) {
        this.mailProperties = mailProperties;
    }

    public String getContentFile() {
        return contentFile;
    }

    public String getAttachmentDir() {
        return attachmentDir;
    }

    public String getAttachmentsArchiveDir() {
        return attachmentsArchiveDir;
    }

    public String getLogsDir() {
        return logsDir;
    }

    public String getMailHost() {
        return mailProperties.getProperty("mail.smtp.host");
    }

    public int getMailPort() {
        try {
            return Integer.parseInt(mailProperties.getProperty("mail.smtp.port"));
        } catch (Exception ex) {
            logger.info(mailProperties.getProperty("mail.smtp.port") + ", Email host port parse error, "
                    + ex.getMessage());
        }
        return 587;
    }

    public String getServiceBy() {
        return serviceBy;
    }

    public void setServiceBy(String serviceBy) {
        this.serviceBy = serviceBy == null ? "" : serviceBy;
    }

    public boolean isDeleteArchive() {
        return isDeleteArchive;
    }

    public void setDeleteArchive(boolean deleteArchive) {
        isDeleteArchive = deleteArchive;
    }

    public String getMailUser() {
        return mailProperties.getProperty("mail.smtp.user");
    }

    public String getMailPassword() {
        return mailProperties.getProperty("mail.smtp.password");
    }

    public InternetAddress getFromAddress() {
        return fromAddress;
    }

    public InternetAddress[] getToAddresses() {
        return toAddresses;
    }

    public InternetAddress[] getCcAddresses() {
        return ccAddresses;
    }

    public void setAddress(InternetAddress fromAddress, InternetAddress[] toAddresses, InternetAddress[] ccAddresses) {
        this.fromAddress = fromAddress;
        this.toAddresses = toAddresses;
        this.ccAddresses = ccAddresses;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Properties getMailProperties() {
        return mailProperties;
    }

    public Mailer getMailer() {
        return mailer;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public boolean isHtmlContent() {
        return getContentFile().endsWith(".html");
    }
}