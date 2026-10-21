# Workbench Lens

Workbench Lens is a local browser interface for asking questions about workshop tools using an uploaded image, a local Ollama vision model, and an Oracle catalogue.

## Run the browser app

Requirements: Java 17+, Maven, Oracle Database with the existing `WORKBENCH` user and `tools` table, and Ollama with the `minicpm-v` vision model available locally. Install the model with `ollama pull minicpm-v`.

1. Start Oracle and Ollama on the local machine.
2. Build the project with `mvn package` and resolve its Maven dependencies.
3. Run `java -jar target/workbench-lens-1.0-SNAPSHOT.jar`.
4. Open `http://localhost:8080`.

The app binds only to the loopback interface. Set the `PORT` environment variable to choose another local port. Enter the Oracle password in the browser to connect; the app server uses it only for the current in-memory session. No database password is stored in source code.

The browser accepts PNG, JPEG, GIF, or WebP images up to 8 MB. Image analysis and generated answers are marked unverified. Catalogue records display the verification status saved in Oracle. Keep Oracle and Ollama restricted to trusted local use.

The existing console `App` and `LocalVisionTest` entry points remain available. To run the console vision test from a terminal, use `java -cp "target/classes;target/workbench-lens-1.0-SNAPSHOT.jar" com.workbenchlens.LocalVisionTest` on Windows (use `:` instead of `;` as the classpath separator on macOS/Linux).
