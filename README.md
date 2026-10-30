# Workbench Lens

Workbench Lens is a local Java web application for exploring workshop, kitchen, and garden tools. A user uploads a photo, asks a question, and selects a lens. The application uses a local Ollama vision model to interpret the image, then uses Oracle catalogue records to ground tool details and safety information.

The intended project use is educational and local. AI image descriptions and generated guidance are unverified; consult the tool maker's instructions for safety-critical or model-specific information.

## What it does

- Provides three themed catalogues: Workbench Lens, Kitchen Counter Lens, and Garden Lens.
- Accepts PNG, JPEG, GIF, or WebP images up to 8 MB.
- Uses the user's question to focus the model on a requested tool, including when several tools appear in one image.
- Asks the model to describe the requested tool's approximate image position (for example, upper-left or beside the hammer) and visible distinguishing features.
- Restricts each lens to its own Oracle catalogue records.
- Shows saved Oracle details separately from AI-generated, unverified image evidence and guidance.
- Supports account registration, login, logout, and an administrator view of registered accounts.

The image feature uses MiniCPM-V's visual reasoning through Ollama. It provides a text description of position and visible features; it is not a dedicated object detector and does not return bounding boxes or precise pixel coordinates. It may be uncertain when tools are small, obscured, visually similar, or crowded together.

## Technology

| Component | Use |
| --- | --- |
| Java 17 | Web server, application logic, image request handling, database access, and account workflows |
| Maven | Dependency management and executable JAR packaging |
| Oracle Database | Tool catalogue and application account storage |
| JDBC with Oracle `ojdbc17` | Java-to-Oracle connectivity |
| Ollama with `minicpm-v` | Local image interpretation and response generation |
| Jackson Databind | JSON request and response handling |
| HTML, CSS, and JavaScript | Browser interface, lens switching, photo upload, and API calls |
| Docker | Runs the local Oracle database container (`oracle-free`) |

The Maven project uses Jackson Databind 2.18.3, Oracle JDBC `ojdbc17` 23.26.3.0.0, and Maven Shade Plugin 3.6.0. The executable JAR is configured to start `com.workbenchlens.WebApp`.

## Architecture and request flow


Oracle does not analyze the photo, and the vision model does not connect to Oracle directly. Java loads the records from Oracle, filters them for the chosen lens, and includes those records in the model request. When a match is found, Java displays the saved catalogue details and asks the local model to answer using those details and the unverified visual evidence.

## Oracle data model

`TOOLS` is the shared parent catalogue. It stores common fields used across lenses, including the tool name, category, purpose, safety notes, and verification status. Tool IDs connect shared records to category-specific detail tables.

| Table | Purpose |
| --- | --- |
| `TOOLS` | Shared tool identity and common catalogue information |
| `ELECTRICAL_TOOLS` | Additional records for electrical tools |
| `HANDHELD_TOOLS` | Additional records for manually operated tools |
| `WATERWORKS_TOOLS` | Additional records for plumbing and water-related tools |
| `MEASURING_MISC_DANGEROUS` | Additional records for measuring, miscellaneous, and dangerous tools |
| `KITCHEN_TOOLS` | Kitchen-specific use, food-contact safety, and cleaning details |
| `GARDEN_TOOLS` | Garden use, plant or task, and maintenance details |
| `APP_USERS` | Website usernames, email addresses, roles, password hashes, and creation times |

The detail tables are associated with `TOOLS` through `TOOL_ID`. The application filters records into the selected lens: Workbench uses the workshop groups, Kitchen uses `KITCHEN_TOOLS`, and Garden uses `GARDEN_TOOLS`.

The database schema is expected to exist before the application runs. This repository does not currently provide a complete schema migration or seed-data script. Use the Oracle `WORKBENCH` schema in the `FREEPDB1` service, and create/populate the tables using the SQL prepared for your database setup.

### Useful SQL checks

Connect to the database from PowerShell:

```powershell
docker start oracle-free
docker exec -it oracle-free sqlplus WORKBENCH@//localhost:1521/FREEPDB1
```

At the `SQL>` prompt, list the project's catalogue and account tables:

```sql
SELECT table_name
FROM user_tables
WHERE table_name IN (
  'TOOLS',
  'ELECTRICAL_TOOLS',
  'HANDHELD_TOOLS',
  'WATERWORKS_TOOLS',
  'MEASURING_MISC_DANGEROUS',
  'KITCHEN_TOOLS',
  'GARDEN_TOOLS',
  'APP_USERS'
)
ORDER BY table_name;
```

Show the shared tool catalogue:

```sql
SELECT tool_id, name, category, purpose, safety_notes, verification_status
FROM tools
ORDER BY tool_id;
```

Show kitchen and garden records with their shared tool details:

```sql
SELECT t.tool_id, t.name, t.category, t.purpose,
       k.kitchen_use, k.food_contact_safe, k.cleaning_notes
FROM tools t
JOIN kitchen_tools k ON k.tool_id = t.tool_id
ORDER BY t.tool_id;
```

```sql
SELECT t.tool_id, t.name, t.category, t.purpose,
       g.garden_use, g.plant_or_task, g.maintenance_notes
FROM tools t
JOIN garden_tools g ON g.tool_id = t.tool_id
ORDER BY t.tool_id;
```

List account metadata without exposing password hashes:

```sql
SELECT user_id, username, email, user_role, created_at
FROM app_users
ORDER BY user_id;
```

## Accounts and passwords

Application accounts are stored in `APP_USERS`. The application uses PBKDF2-HMAC-SHA256 password hashes with per-account salts; passwords are not stored as readable text. The first account registered while `APP_USERS` is empty receives the `ADMIN` role; later registrations receive `USER`. Admin accounts can view the registered-user list.

There are two database-password uses in the local app setup:

1. The Java server reads `WORKBENCH_DB_PASSWORD` from its environment for account registration, login, and administrator database operations.
2. After logging in, the user enters the Oracle catalogue password in the browser to connect that session to the catalogue. It is held for the local session and is not stored in source code.

Use the same Oracle `WORKBENCH` password for both when connecting to the same local database. Do not put a real password in this README or commit it to Git.

This is a local educational prototype. It does not include production account recovery, rate limiting or lockout, TLS deployment, or a hardened production security setup. Keep Oracle and Ollama available only to trusted local applications.

## Requirements

- Java 17 or newer
- Maven
- Docker Desktop
- Oracle Database container named `oracle-free`, with `WORKBENCH` available in `FREEPDB1`
- Ollama installed locally and the `minicpm-v` model pulled
- Windows PowerShell commands below assume the project is at `E:\AIJAVADDM\workbench-lens` and Maven is installed at `E:\apache-maven-3.10.0`; adjust paths if yours differ

Pull the vision model once if it is not installed:

```powershell
ollama pull minicpm-v
ollama list
```

## Run the web application on Windows

Open PowerShell in the project folder. Start Oracle and make sure Ollama is running:

```powershell
Set-Location E:\AIJAVADDM\workbench-lens
docker start oracle-free
ollama list
```

Set the server's Oracle password for this PowerShell session. Replace the placeholder locally; do not save a real password in the repository:

```powershell
$env:WORKBENCH_DB_PASSWORD = "<your Oracle WORKBENCH password>"
$env:PORT = "8080"
```

Build the executable JAR. If `mvn` is on your PATH:

```powershell
mvn package
```

If PowerShell says `mvn` is not recognized and Maven is installed at the path used in this setup:

```powershell
& "E:\apache-maven-3.10.0\bin\mvn.cmd" package
```

After `BUILD SUCCESS`, start the web server in that terminal:

```powershell
java -jar target\workbench-lens-1.0-SNAPSHOT.jar
```

Open [http://localhost:8080](http://localhost:8080). Leave the server terminal running while you use the site. Press `Ctrl+C` in that terminal to stop it. To use a different port, set `PORT` before launching.

If Ollama is not already running in the background, start it in a separate terminal with `ollama serve`.

## Project files

| Java file | Responsibility |
| --- | --- |
| `WebApp.java` | Local HTTP endpoints, sessions, login/registration, Oracle connection, lens filtering, and AI request flow |
| `WebPage.java` | HTML, CSS, and browser-side JavaScript for the interface |
| `OllamaClient.java` | Requests to the local Ollama API |
| `ToolRepository.java` | JDBC connection and catalogue queries |
| `UserRepository.java` | User registration, password hashing, authentication, and admin user listing |
| `App.java` | Console catalogue entry point |
| `LocalVisionTest.java` | Console-based local image and catalogue demonstration |
| `DatabaseConnectionTest.java` | Database connectivity demonstration |

## Troubleshooting

- **`mvn` is not recognized:** use the full Maven command shown above or add Maven's `bin` folder to `PATH`.
- **ORA-12541 / no listener:** start `oracle-free`, wait for Oracle startup, and check that port 1521 is available.
- **ORA-01017 / invalid credentials:** confirm the Oracle `WORKBENCH` password and connect to `FREEPDB1`.
- **The app says `WORKBENCH_DB_PASSWORD` is missing:** set that variable in the same PowerShell terminal before launching Java.
- **Ollama cannot be reached:** check `ollama list`, confirm `minicpm-v` is installed, and start `ollama serve` if needed.
- **No catalogue match:** check that the tool exists in `TOOLS` and has a matching row in the selected lens detail table. A clear photo of the requested item helps the model distinguish it from similar or overlapping tools.

## Limitations

AI image evidence and generated guidance are unverified. Approximate locations are expressed in natural language, relative to the image or nearby items. They are not precise measurements, bounding boxes, or a substitute for checking the actual tool. Use manufacturer instructions for tool-specific operation and safety.
