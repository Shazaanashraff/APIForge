# Contributing to APIForge

> This guide describes how to work on APIForge day-to-day, even as a solo developer. Following these conventions makes the project portfolio-ready and teaches real-world team practices.

---

## Git Workflow

### Branch Naming

| Pattern | Use |
|---|---|
| `feature/sXX-short-description` | New section work (e.g. `feature/s05-schema-parser`) |
| `feature/short-description` | A feature not tied to a specific section |
| `fix/short-description` | Bug fix |
| `docs/short-description` | Documentation only |
| `chore/short-description` | Build, config, CI |
| `refactor/short-description` | Refactoring |

Always branch from `main`. Merge back to `main` only when the checkpoint's Definition of Done is complete.

### Commit Message Format (Conventional Commits)

```
<type>(<scope>): <subject>

[optional body — explain WHY, not what]
```

**Types:**
- `feat` — new feature or capability
- `fix` — bug fix
- `docs` — documentation changes only
- `style` — formatting, no logic change
- `refactor` — code change that neither fixes a bug nor adds a feature
- `test` — adding or fixing tests
- `chore` — build system, dependencies, config
- `perf` — performance improvement
- `ci` — CI/CD pipeline changes
- `build` — build system changes
- `wip` — work in progress (ONLY on feature branches, NEVER on main)

**Scopes** (use the module name):
`schema-parser` · `test-gen` · `data-gen` · `code-gen` · `executor` · `validator` · `load-tester` · `reporter` · `project` · `auth` · `tenancy` · `shared` · `frontend` · `docker` · `ci` · `scripts` · `db` · `samples`

**Examples:**
```
feat(test-gen): add pagination test generator with cursor-style detection

Handles three pagination styles: offset/limit, page/size, and cursor-based.
Cursor detection looks for query params named cursor, nextToken, or pageToken.

chore(backend): add Maven project skeleton with all module packages

docs(plans): add per-section implementation plans S01-S23

fix(executor): handle 401 on first request before entering auth refresh loop

Previously, if the very first request returned 401 (e.g. expired token at
session start), the refresh loop was skipped. Now we detect and refresh first.
```

### One Commit Per Checkpoint

Each checkpoint in `PROGRESS.md` maps to exactly one commit. Don't bundle multiple checkpoints into a single commit — the git log is our changelog and resume trail.

### Pull Requests (Even Solo)

Even as a solo developer, use PRs for significant features. This:
- Forces you to review your own changes
- Produces a clean GitHub commit history
- Makes the repo look professional for portfolio

Use the PR template at `.github/PULL_REQUEST_TEMPLATE.md`.

---

## Updating PROGRESS.md

After every checkpoint commit, update `PROGRESS.md`:
1. Move the checkpoint from "In Progress" → "Completed" with the commit hash
2. Update "Last Checkpoint" section
3. Update "Next file to work on" and "Resume instructions"

This is part of the **Definition of Done**. A checkpoint is incomplete without it.

---

## Writing an ADR (Architecture Decision Record)

When you make a significant architectural decision:
1. Copy the template from `docs/adr/TEMPLATE.md`
2. Name it `docs/adr/NNNN-short-title.md` (NNNN = zero-padded number, e.g. `0005-use-tempo-for-tracing.md`)
3. Fill in Context, Decision, Alternatives Considered, and Consequences
4. Commit it alongside the code that implements the decision

Rules:
- ADRs are **immutable** once accepted. Never edit an existing ADR's decision.
- To change a decision, write a NEW ADR that supersedes the old one.
- Status must be one of: `Proposed` | `Accepted` | `Superseded by ADR-XXXX`

---

## Code Style

### Java
- Enforced by **Spotless + Google Java Format** via `mvnw spotless:apply`
- Run before every commit: `.\mvnw.cmd spotless:apply`
- Static analysis: SpotBugs + PMD run on `mvnw verify`

### TypeScript/JavaScript
- Enforced by **ESLint + Prettier**
- Run before every commit: `npm run lint:fix && npm run format`

### IDE Setup
- IntelliJ: install "google-java-format" plugin, enable "Reformat code on save"
- VS Code: install ESLint and Prettier extensions

---

## Definition of Done (Required Before Every Commit)

- [ ] Code compiles and runs without errors
- [ ] Unit tests pass (`mvnw test` or `npm test`)
- [ ] Relevant integration tests pass
- [ ] Code is formatted (Spotless / Prettier)
- [ ] Docs updated (module README, inline comment if new concept, ADR if architectural)
- [ ] `PROGRESS.md` updated
- [ ] Commit message follows Conventional Commits

---

## Asking Questions

This is a solo learning project, but if you're stuck:
- Check the relevant `docs/plans/SXX-*.md` for the intended approach
- Check `docs/adr/` to understand why a technology was chosen
- Check `LEARNING.md` for concept explanations and links
