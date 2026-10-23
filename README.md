# Workbench Lens

Workbench Lens is a local browser interface for asking questions about workshop tools using an uploaded image, a local Ollama vision model, and an Oracle catalogue.

## Run the browser app

Requirements: Java 17+, Maven, Oracle Database with the existing `WORKBENCH` user and `tools` table, and Ollama with the `minicpm-v` vision model available locally. Install the model with `ollama pull minicpm-v`.

1. Start Oracle and Ollama on the local machine.
2. Build the project with `mvn package` and resolve its Maven dependencies.
3. Run `java -jar target/workbench-lens-1.0-SNAPSHOT.jar`.
4. Open `http://localhost:8080`.

The app binds only to the loopback interface. Set the `PORT` environment variable to choose another local port. Before starting the app, set `WORKBENCH_DB_PASSWORD` in the server process environment to the Oracle `WORKBENCH` database password. Application accounts authenticate against `APP_USERS`; registration and login require this database connection. The user then separately connects the catalogue through the browser. No database password is stored in source code.

The first account registered while `APP_USERS` is empty receives the `ADMIN` role; later registrations receive `USER`. Admin accounts can view the registered-user list. Account passwords are stored as PBKDF2-HMAC-SHA256 hashes with per-account salts embedded in the stored hash string. This is a local prototype and does not provide production account recovery, throttling/lockout, or a hardened deployment security model.

The browser accepts PNG, JPEG, GIF, or WebP images up to 8 MB. Image analysis and generated answers are marked unverified. Catalogue records display the verification status saved in Oracle. Keep Oracle and Ollama restricted to trusted local use.

The existing console `App` and `LocalVisionTest` entry points remain available. To run the console vision test from a terminal, use `java -cp "target/classes;target/workbench-lens-1.0-SNAPSHOT.jar" com.workbenchlens.LocalVisionTest` on Windows (use `:` instead of `;` as the classpath separator on macOS/Linux).
