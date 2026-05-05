# Developer Setup Guide

> Complete these steps **once** before starting development. Each step includes what it is, why it's needed, the exact command, and how to verify it worked.

---

## Prerequisites Checklist

Work through these in order. Check each off as you complete it.

---

### 1. Java 21

**What:** The Java runtime and compiler. This project requires Java 21 specifically (for virtual threads).
**Why:** Spring Boot 3.x + Java 21 virtual threads are core to the load-testing module.

- [ ] Download: https://adoptium.net/temurin/releases/?version=21 (choose Windows x64 `.msi`)
- [ ] Run the installer — accept defaults; let it set `JAVA_HOME`
- [ ] Verify:
  ```powershell
  java -version
  # Expected: openjdk version "21.x.x" ...
  javac -version
  # Expected: javac 21.x.x
  ```
- [ ] **Windows gotcha:** If you have multiple Java versions, make sure Java 21 appears first in your `PATH`. Check `$env:JAVA_HOME` in PowerShell.

---

### 2. Node.js 20+

**What:** JavaScript runtime. Needed for the frontend (React/Vite) and the Node.js sample API.
**Why:** Frontend build tooling (Vite, npm) requires Node. The sample API is a Node.js app.

- [ ] Download: https://nodejs.org/en/download (choose "LTS" → Windows Installer)
- [ ] Verify:
  ```powershell
  node --version   # Expected: v20.x.x or higher
  npm --version    # Expected: 10.x.x or higher
  ```

---

### 3. Docker Desktop for Windows (WSL2 backend)

**What:** Runs all infrastructure services (Postgres, Redis, Kafka, Keycloak, etc.) in containers.
**Why:** The project uses 13+ services locally. Docker Compose manages them all.

- [ ] Download: https://www.docker.com/products/docker-desktop/
- [ ] During install — enable **WSL2 integration** when prompted
- [ ] After install, open Docker Desktop → Settings → Resources → **set Memory to at least 10 GB**
  - The full stack (Postgres + Redis + Kafka + Keycloak + MongoDB + observability) needs ~8–9 GB
  - The lite stack (Postgres + Redis + MongoDB only) needs ~2 GB
- [ ] Verify:
  ```powershell
  docker --version          # Expected: Docker version 25.x.x or higher
  docker-compose --version  # Expected: Docker Compose version 2.x.x
  docker run hello-world    # Expected: "Hello from Docker!"
  ```
- [ ] **Windows gotcha:** WSL2 must be installed. If the Docker installer doesn't handle it: `wsl --install` in an admin PowerShell, then reboot.
- [ ] **Antivirus gotcha:** Some antivirus software interferes with Docker networking. If containers can't reach each other, temporarily disable real-time protection during setup.

---

### 4. Git for Windows

**What:** Version control.
**Why:** Clone the repo, commit checkpoints, push to GitHub.

- [ ] Download: https://git-scm.com/download/win (64-bit setup)
- [ ] Recommended installer options: use Git from the command line, use bundled OpenSSH, use the default branch name `main`
- [ ] Configure your identity:
  ```powershell
  git config --global user.name "Shazaanashraff"
  git config --global user.email "mohamedshazaan7@gmail.com"
  git config --global core.autocrlf true
  ```
- [ ] Verify:
  ```powershell
  git --version  # Expected: git version 2.x.x.windows.x
  ```

---

### 5. IntelliJ IDEA (Java backend)

**What:** IDE for writing Spring Boot / Java code.
**Why:** Best Java IDE. Has built-in Spring Boot support, Maven integration, and debugger.

- [ ] Download Community Edition (free): https://www.jetbrains.com/idea/download/ → Community
- [ ] Install the **google-java-format** plugin:
  IntelliJ → Settings → Plugins → search "google-java-format" → Install → Restart
- [ ] Enable "Reformat code on save": Settings → Tools → Actions on Save → tick "Reformat code"
- [ ] Set Project SDK to Java 21: File → Project Structure → SDK → add the Java 21 installation

---

### 6. Visual Studio Code (Frontend + Node.js sample API)

**What:** Editor for TypeScript/React and the Node.js sample API.
**Why:** VS Code has best-in-class JS/TS tooling and is free.

- [ ] Download: https://code.visualstudio.com/
- [ ] Install these extensions (Ctrl+Shift+X → search each):
  - **ESLint** (by Microsoft)
  - **Prettier - Code formatter** (by Prettier)
  - **Tailwind CSS IntelliSense** (by Tailwind Labs)
  - **Docker** (by Microsoft)
  - **REST Client** (by Huachao Mao) — useful for quick API testing

---

### 7. MongoDB Compass (optional but recommended)

**What:** GUI for browsing MongoDB data.
**Why:** The Node.js sample API uses MongoDB. Compass makes it easy to inspect documents and verify seed data.

- [ ] Download: https://www.mongodb.com/try/download/compass
- [ ] Connection string (once MongoDB is running via Docker Compose):
  `mongodb://apiforge:apiforge_secret@localhost:27017/apiforge_samples`

---

### 8. GitHub Account & Repository

**What:** Public GitHub account and repo for the project.
**Why:** Portfolio piece — must be public.

- [ ] Ensure you have a GitHub account: https://github.com/
- [ ] The repo is already created: `https://github.com/Shazaanashraff/APIForge`
- [ ] Generate a Personal Access Token (for pushing from the CLI):
  GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token
  - Scopes: `repo` (all), `workflow`
  - Save the token somewhere safe — you'll need it when `git push` asks for a password

---

### 9. First-Time Project Setup

Once all tools above are installed:

- [ ] Navigate to the project directory:
  ```powershell
  cd S:\apiforge
  ```

- [ ] Copy the environment template:
  ```powershell
  copy .env.example .env
  ```
  Open `.env` in a text editor and review — the defaults work for local dev without changes.

- [ ] Pull all Docker images (this downloads ~6–8 GB; do it on a good connection):
  ```powershell
  docker-compose pull
  ```

- [ ] Start the **lite** stack (deps only — fastest for daily dev):
  ```powershell
  docker-compose -f docker-compose.lite.yml up -d
  ```
  Verify all three containers are healthy:
  ```powershell
  docker-compose -f docker-compose.lite.yml ps
  # Expected: postgres, redis, mongodb all show "healthy"
  ```

- [ ] Open the backend in IntelliJ: File → Open → select `S:\apiforge\backend\`
  Let IntelliJ download all Maven dependencies (first time takes 2–5 min on a good connection).

- [ ] Open the frontend in VS Code: File → Open Folder → `S:\apiforge\frontend\`
  ```powershell
  cd S:\apiforge\frontend
  npm install
  ```

- [ ] Open the Node.js sample API in VS Code: File → Open Folder → `S:\apiforge\sample-target-api-node\`
  ```powershell
  cd S:\apiforge\sample-target-api-node
  npm install
  ```

---

### 10. Verify the Backend Starts

```powershell
cd S:\apiforge\backend
.\mvnw.cmd spring-boot:run
```

Expected output (after 15–30 seconds):
```
Started ApiForgeApplication in X.XXX seconds
```

Visit: http://localhost:8081/actuator/health
Expected: `{"status":"UP"}`

---

### 11. Verify the Frontend Starts

```powershell
cd S:\apiforge\frontend
npm run dev
```

Visit: http://localhost:5173
Expected: APIForge login page (or placeholder during early sections)

---

### 12. First-Time Full Stack (Optional — needs 10 GB Docker memory)

```powershell
cd S:\apiforge
docker-compose up -d
.\scripts\verify-system.ps1
```

This starts ALL services including Keycloak, Kafka, and observability. Takes 3–5 minutes for everything to become healthy.

#### Keycloak First-Time Setup
1. Open http://localhost:8080
2. Log in: username `admin`, password `admin` (from `.env`)
3. Verify the `apiforge` realm has been imported (shown in the top-left realm selector)

#### Grafana First-Time Login
1. Open http://localhost:3001
2. Log in: `admin` / `admin`
3. You will be prompted to change the password
4. Navigate to Dashboards — pre-built APIForge dashboards should be visible

#### MongoDB Seed Data
```powershell
.\scripts\seed-mongo.ps1
```
Verify in MongoDB Compass or:
```powershell
docker exec -it apiforge-mongodb mongosh --eval "db.getSiblingDB('apiforge_samples').users.countDocuments()"
# Expected: a number > 0
```

---

## Common Windows Gotchas

| Problem | Fix |
|---|---|
| `.\mvnw.cmd` says "Access is denied" | Run PowerShell as Administrator, or check file permissions on `S:\` |
| Docker containers won't start | Check Docker Desktop is running and WSL2 is enabled |
| Port already in use (e.g. 5432) | Check for locally installed PostgreSQL: `netstat -ano \| findstr :5432` |
| `npm install` fails with EACCES | Don't run npm as Administrator; fix npm prefix: `npm config set prefix "$env:APPDATA\npm"` |
| Java not found after install | Restart PowerShell; verify `$env:JAVA_HOME` points to Java 21 |
| Git push asks for username/password | Use your GitHub token as the password |
| Docker Desktop using too much memory | Reduce the memory limit, then use the lite compose file |

---

## What's Next

Once setup is complete, see [RUNBOOK.md](RUNBOOK.md) for day-to-day running, health checks, and troubleshooting.
