### Simple Email Service

- Java 11 language is used to communicate files via sftp protocol.
- application.properties is using springframework configuration.

#### Apps Configuration

- User Home /config/apps.properties

```

mail.service.by=Samooha
mail.logs.dir=/Users/ramesh/Samooha/sftp-email/target/matrix-star/logs
mail.content.file=/Users/ramesh/Samooha/sftp-email/target/matrix-star/bin/email-content.txt
mail.attachments.dir=/Users/ramesh/Samooha/sftp-email/target/matrix-star/outbox
mail.attachments-archive.dir=/Users/ramesh/Samooha/sftp-email/target/matrix-star/error
mail.attachments.delete=false
mail.subject=Samooha PO Import Error Files
mail.from=matrix-star@samooha.biz
mail.to=sample@gmail.com,sample@live.com
mail.cc=rameshselvaraj@samooha.com

```

#### Email Configuration

- User Home /config/email.properties

```

mail.smtp.writetimeout=60000
mail.smtp.from=simple@gmail.biz
mail.smtp.connectiontimeout=60000
mail.smtp.host=mail.simple.biz
mail.smtp.timeout=600000
mail.smtp.starttls.enable=true
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.ssl.trust=*
mail.smtp.user=matrix-star@gmail.biz
mail.smtp.password=my-password
mail.transport.protocol=smtp

```