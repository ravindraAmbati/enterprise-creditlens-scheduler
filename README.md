# Enterprise CreditLens Report Scheduler Engine

Enterprise CreditLens Report Scheduler Engine is a secure, DB-less Spring Boot scheduler for generating CreditLens reports on a Windows server. It authenticates with CreditLens, triggers asynchronous report generation, discovers the generated file from the CreditLens repository folder, validates the file, sends notifications, writes audit/job logs, and exposes operational APIs.

This guide is written for Java developers and Windows server operators who need to configure, deploy, run, and support the application.

## Contents

1. Application overview
2. Runtime and deployment prerequisites
3. Configuration files and profiles
4. Report/job/email configuration
5. Windows Credential Manager setup
6. Build and test
7. Run on Windows as a Java process
8. Install as a Windows Service
9. APIs, Swagger, and authentication
10. Logs, metrics, and health checks
11. Troubleshooting

## 1. Application Overview

The scheduler performs this workflow for each enabled job:

```text
Quartz cron trigger
  -> validate configuration
  -> read CreditLens credentials from Windows Credential Manager
  -> call CreditLens authentication API
  -> keep bearer token in memory only
  -> call CreditLens report generation API
  -> wait for asynchronous generation
  -> search generated report folder
  -> identify latest matching file
  -> validate file type, timestamp, readability, and size
  -> send business success email
  -> send IT failure email if any stage fails
  -> write application, audit, and job logs
  -> publish metrics
```

Main technologies:

| Area | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.x |
| Scheduler | Quartz |
| API client | Spring WebClient |
| Retry | Resilience4j |
| Email | Spring Mail and Thymeleaf templates |
| Security | Spring Security, service-account mode, LDAP/AD configuration knobs |
| Observability | Actuator, Micrometer, JSON logs |
| Build | Maven |
| Deployment | Windows Java process or Windows Service |

Project artifact:

```text
enterprise-creditlens-scheduler.jar
```

## 2. Runtime and Deployment Prerequisites

Install or prepare the following on the Windows server:

| Requirement | Notes |
|---|---|
| JDK 21 | Example path: `C:\softwares\jdk-21` |
| Maven 3.8+ | Required to build on the server; not required if deploying a prebuilt jar |
| Network access to CreditLens | Auth and report generation API endpoints must be reachable |
| Access to generated report folder | The service account running the app must read the CreditLens/MinIO repository path |
| SMTP relay | Internal SMTP host and port |
| Windows Credential Manager entry | One credential per configured job or shared target |
| Service account | Recommended name: `SA-SVC-creditlens-scheduler` |

Set Java 21 in the current PowerShell session:

```powershell
$env:JAVA_HOME="C:\softwares\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

java -version
mvn -version
```

Expected Java output should show version `21`.

## 3. Configuration Files and Profiles

Main configuration:

```text
src/main/resources/application.yml
```

Profile-specific configuration:

```text
src/main/resources/application-nonprod.yml
src/main/resources/application-prod.yml
```

Recommended profile usage:

| Environment | Spring profile | Security posture |
|---|---|---|
| Local development | default or `nonprod` | Service-account auth disabled |
| Test/UAT | `nonprod` | Service-account auth disabled by default, can be overridden |
| Production | `prod` | Service-account auth enabled |

Start with a profile:

```powershell
.\scripts\start-app.ps1 -Profile "prod"
```

Equivalent Java command:

```powershell
java -jar target\enterprise-creditlens-scheduler.jar --spring.profiles.active=prod
```

## 4. Report, Job, and Email Configuration

Jobs are configured under `scheduler.jobs`.

Example:

```yaml
scheduler:
  jobs:
    - name: customer-report
      enabled: true
      cron: "0 0 8 * * ?"
      credential-target: "CreditLens/customer-report"

      authentication:
        endpoint: "https://creditlens-host/api/security/authenticate"

      report:
        endpoint: "https://creditlens-host/api/reports/generate"
        parent-file-path: "reports"
        file-path: "customer"
        file-name: "customer-report"
        file-type: "xlsx"
        generated-report-base-path: "D:/MinioRepository/creditlensstorage/tenant/documents"

      request:
        timeout-seconds: 60

      retry:
        max-attempts: 5
        initial-delay-ms: 2000
        multiplier: 2.0

      validation:
        wait-before-validation-seconds: 10
        max-search-window-seconds: 120
        minimum-file-size-kb: 5
        fail-if-multiple-files-found: true

      report-input:
        report-id: "REPORT_001"
        is-portfolio: false
        report-params: []

      notifications:
        business-emails:
          - business@company.com
        it-support-emails:
          - itsupport@company.com
```

Important fields:

| Field | Description |
|---|---|
| `name` | Unique job name. Used in APIs, logs, audit records, and notifications. |
| `enabled` | Set `false` to keep the job configured but inactive. |
| `cron` | Quartz cron expression. Example: `0 0 8 * * ?` runs daily at 08:00. |
| `credential-target` | Windows Credential Manager target name. Must exist on the server. |
| `authentication.endpoint` | CreditLens authentication endpoint. |
| `report.endpoint` | CreditLens report generation base endpoint. |
| `parent-file-path` | Parent folder below the generated report base path. |
| `file-path` | CreditLens API path segment and generated folder segment. |
| `file-name` | CreditLens report API file name segment. |
| `file-type` | Expected output extension, such as `xlsx`, `pdf`, or `csv`. |
| `generated-report-base-path` | Base folder where CreditLens writes generated files. |
| `timeout-seconds` | Timeout for each critical stage. |
| `max-attempts` | Retry attempts per critical stage. |
| `initial-delay-ms` | First retry delay. Attempt 1 is immediate. |
| `multiplier` | Exponential backoff multiplier. |
| `wait-before-validation-seconds` | Delay after API returns `202 Accepted` before file discovery starts. |
| `max-search-window-seconds` | Maximum modified-time window for candidate files. |
| `minimum-file-size-kb` | Reject files smaller than this value. |
| `fail-if-multiple-files-found` | If `true`, multiple candidates fail the execution. |
| `business-emails` | Recipients for successful report generation. |
| `it-support-emails` | Recipients for failure alerts. |

### Generated File Search Path

The application searches:

```text
{generated-report-base-path}/{parent-file-path}/{file-path}
```

Example:

```text
D:/MinioRepository/creditlensstorage/tenant/documents/reports/customer
```

Because CreditLens report generation is asynchronous and does not return a file name, each execution should generate one report in one execution window. This prevents ambiguous file discovery.

### SMTP Configuration

Configure your internal SMTP relay:

```yaml
spring:
  mail:
    host: smtp.company.com
    port: 25
    test-connection: false
```

Optional startup email test:

```yaml
scheduler:
  startup-validation:
    smtp-test-email-enabled: true
    smtp-test-recipient: itsupport@company.com
```

Email templates:

```text
src/main/resources/templates/email/business-success.html
src/main/resources/templates/email/it-failure.html
```

## 5. Windows Credential Manager Setup

The application never stores CreditLens passwords in YAML, Git, logs, or a database. Credentials must be available in Windows Credential Manager.

The configured job field:

```yaml
credential-target: "CreditLens/customer-report"
```

must match the Credential Manager target exactly.

### Add Credential Using Windows UI

1. Open **Control Panel**.
2. Open **Credential Manager**.
3. Select **Windows Credentials**.
4. Select **Add a generic credential**.
5. Set **Internet or network address** to:

   ```text
   CreditLens/customer-report
   ```

6. Set **User name** to the CreditLens API username.
7. Set **Password** to the CreditLens API password.
8. Save.

### Add Credential Using Command Line

Run PowerShell as the same Windows account that will run the scheduler service:

```powershell
cmdkey /generic:"CreditLens/customer-report" /user:"clapi" /pass:"your-password"
```

Verify:

```powershell
cmdkey /list:"CreditLens/customer-report"
```

Delete:

```powershell
cmdkey /delete:"CreditLens/customer-report"
```

Important: if the application runs as a Windows Service under `SA-SVC-creditlens-scheduler`, create the credential while logged in as that service account, or use an approved enterprise process to create it for that account. Credentials stored under your personal Windows profile will not automatically be visible to the service account.

## 6. Build and Test

From the project root:

```powershell
cd C:\workspace\codex
```

Set Java:

```powershell
$env:JAVA_HOME="C:\softwares\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Build the jar:

```powershell
mvn clean package
```

Run unit and integration tests:

```powershell
mvn verify
```

Successful build output creates:

```text
target/enterprise-creditlens-scheduler.jar
```

## 7. Run on Windows as a Java Process

The project includes scripts under:

```text
scripts/
```

| Script | Purpose |
|---|---|
| `start-app.ps1` | Starts the application in the background and writes `app.pid`. |
| `stop-app.ps1` | Stops the PID in `app.pid`. |
| `restart-app.ps1` | Stops and starts the application. |

Start non-production:

```powershell
.\scripts\start-app.ps1 -Profile "nonprod"
```

Start production:

```powershell
.\scripts\start-app.ps1 -Profile "prod"
```

Stop:

```powershell
.\scripts\stop-app.ps1
```

Restart:

```powershell
.\scripts\restart-app.ps1 -Profile "prod"
```

Script defaults:

| Parameter | Default |
|---|---|
| `JavaHome` | `C:\softwares\jdk-21` |
| `JarPath` | `target\enterprise-creditlens-scheduler.jar` |
| `PidFile` | `app.pid` |
| `OutLog` | `logs\app.out.log` |
| `ErrLog` | `logs\app.err.log` |

Override example:

```powershell
.\scripts\start-app.ps1 `
  -JavaHome "C:\softwares\jdk-21" `
  -JarPath "target\enterprise-creditlens-scheduler.jar" `
  -Profile "prod"
```

## 8. Install as a Windows Service

The recommended service wrapper is WinSW.

### Example Folder Layout

```text
C:\apps\enterprise-creditlens-scheduler
  enterprise-creditlens-scheduler.jar
  enterprise-creditlens-scheduler.exe
  enterprise-creditlens-scheduler.xml
  logs\
```

### Example WinSW XML

Create `enterprise-creditlens-scheduler.xml` next to the WinSW executable:

```xml
<service>
  <id>enterprise-creditlens-scheduler</id>
  <name>Enterprise CreditLens Scheduler</name>
  <description>Schedules and monitors CreditLens report generation.</description>

  <executable>C:\softwares\jdk-21\bin\java.exe</executable>
  <arguments>-jar C:\apps\enterprise-creditlens-scheduler\enterprise-creditlens-scheduler.jar --spring.profiles.active=prod</arguments>

  <log mode="roll-by-size">
    <sizeThreshold>10485760</sizeThreshold>
    <keepFiles>10</keepFiles>
  </log>

  <onfailure action="restart" delay="10 sec"/>
  <startmode>Automatic</startmode>
</service>
```

Install service:

```powershell
.\enterprise-creditlens-scheduler.exe install
```

Start service:

```powershell
.\enterprise-creditlens-scheduler.exe start
```

Stop service:

```powershell
.\enterprise-creditlens-scheduler.exe stop
```

Restart service:

```powershell
.\enterprise-creditlens-scheduler.exe restart
```

Uninstall service:

```powershell
.\enterprise-creditlens-scheduler.exe uninstall
```

Production checklist before installing:

- JDK 21 is installed on the server.
- The jar exists in the service folder.
- The Windows service account has read access to the generated report repository.
- The Windows service account has access to the Credential Manager entries.
- SMTP relay is reachable.
- CreditLens API endpoints are reachable.
- `application-prod.yml` or external production config enables service-account authentication.

## 9. APIs, Swagger, and Authentication

Default local base URL:

```text
http://localhost:8080
```

### Security Behavior

Configuration:

```yaml
security:
  service-account-authentication-enabled: false
  ldap:
    enabled: false
    url: ldap://ad.company.com:389
    base-dn: DC=company,DC=com
    user-dn-pattern: "sAMAccountName={0}"
    service-account-group: SA-SVC-creditlens-scheduler
```

Profile defaults:

| Profile | Service-account auth |
|---|---|
| default | Disabled |
| `nonprod` | Disabled |
| `prod` | Enabled |

When service-account auth is enabled:

| Endpoint | Auth required |
|---|---|
| `GET /actuator/**` | No |
| `GET /jobs` | No |
| `GET /jobs/{jobName}` | No |
| `GET /swagger-ui.html` | Yes |
| `GET /swagger-ui/**` | Yes |
| `GET /v3/api-docs/**` | Yes |
| `POST /jobs/{jobName}/trigger` | Yes |

### Swagger UI

Open:

```text
http://localhost:8080/swagger-ui.html
```

In production, Swagger is protected. Use the configured service account.

Current built-in service-account username default:

```text
SA-SVC-creditlens-scheduler
```

The default development password in code is:

```text
change-me-at-deployment
```

Change or externalize this before production hardening.

### Operational API Examples

Health:

```powershell
curl http://localhost:8080/actuator/health
```

Metrics:

```powershell
curl http://localhost:8080/actuator/metrics
```

List jobs:

```powershell
curl http://localhost:8080/jobs
```

Get job details:

```powershell
curl http://localhost:8080/jobs/customer-report
```

Trigger job in non-production when auth is disabled:

```powershell
curl -X POST http://localhost:8080/jobs/customer-report/trigger
```

Trigger job in production when auth is enabled:

```powershell
curl -u SA-SVC-creditlens-scheduler:change-me-at-deployment `
  -X POST http://localhost:8080/jobs/customer-report/trigger
```

## 10. Logs, Metrics, and Health Checks

Application logs are written under:

```text
logs/
```

Log files:

| File | Purpose |
|---|---|
| `logs/application.log` | Spring Boot and application logs |
| `logs/audit.log` | Governance/security/audit events |
| `logs/jobs.log` | Scheduler execution logs |
| `logs/app.out.log` | Script-launched process standard output |
| `logs/app.err.log` | Script-launched process standard error |

Important audit behavior:

Whenever an external CreditLens API endpoint is called, the application logs the external host to `application.log` and writes an audit entry to `audit.log`.

Health endpoint:

```text
http://localhost:8080/actuator/health
```

Metrics endpoint:

```text
http://localhost:8080/actuator/metrics
```

Useful runtime metrics include scheduler job counts, failed jobs, SMTP failures, job duration, retry counts, and file discovery latency.

## 11. Troubleshooting

### Application Fails at Startup

Check:

- Invalid cron expression
- Invalid CreditLens endpoint URL
- Missing Windows Credential Manager entry
- SMTP connectivity failure
- Invalid AD/security configuration
- Generated report base path is inaccessible to the service account

Review:

```text
logs/application.log
logs/audit.log
logs/jobs.log
```

### Manual Trigger Returns 401 or 403

This happens when `security.service-account-authentication-enabled=true`.

Use service-account credentials:

```powershell
curl -u SA-SVC-creditlens-scheduler:change-me-at-deployment `
  -X POST http://localhost:8080/jobs/customer-report/trigger
```

### Report Generation Returns 401 from CreditLens

Check:

- Credential Manager target matches `credential-target`
- Credential username/password are valid
- The Windows account running the app can read the credential
- CreditLens auth endpoint is correct

### Report API Returns 202 but No File Is Found

Check:

- `generated-report-base-path`
- `parent-file-path`
- `file-path`
- `wait-before-validation-seconds`
- `max-search-window-seconds`
- File extension configured in `file-type`
- Whether another report execution created multiple matching files

### Business Email Not Sent

Check:

- `spring.mail.host`
- `spring.mail.port`
- SMTP relay allows the server/service account
- `business-emails` list is configured
- `logs/jobs.log` and `logs/application.log`

### IT Failure Email Not Sent

Check:

- `it-support-emails` list is configured
- SMTP relay settings
- SMTP failure metrics
- `logs/jobs.log`

## Quick Production Deployment Checklist

1. Build with Java 21:

   ```powershell
   mvn clean package
   ```

2. Copy jar to the server deployment folder.
3. Configure `application-prod.yml` or external production config.
4. Create Windows Credential Manager entries under the service account.
5. Confirm report repository folder permissions.
6. Confirm SMTP relay connectivity.
7. Confirm CreditLens API connectivity.
8. Install Windows Service with WinSW.
9. Start service.
10. Validate:

   ```powershell
   curl http://localhost:8080/actuator/health
   curl http://localhost:8080/jobs
   ```

11. Open Swagger:

   ```text
   http://localhost:8080/swagger-ui.html
   ```

