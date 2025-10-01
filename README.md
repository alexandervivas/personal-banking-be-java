# Personal Banking API

API for the multi‑tenant Personal Banking project. **Java 21**, **Spring Boot**, **Gradle**, **PostgreSQL**. Frontend work is out of scope for the current iterations; we’re focusing on a complete, secure, and stable API.

> Docs-as-code live under `docs/` (ADRs, C4, OpenAPI, governance). Deployment notes: `docs/ops/deployment-render.md`.

---

## Table of Contents

- [Development Environment](#development-environment)\n* [First‑time Setup](#first-time-setup)\n* [Day‑to‑day Commands](#day-to-day-commands)\n* [Conventional Commits](#conventional-commits)\n* [Pull Request Template](#pull-request-template)\n* [Continuous Integration](#continuous-integration)\n* [Editor/IDE Tips](#editoride-tips)\n\* [Troubleshooting](#troubleshooting)

---

## Development Environment

This repository contains the **API** only. Tooling and docs below reflect that.

**Prerequisites**

- **Java 21** (Temurin recommended)
- **Gradle Wrapper** (`./gradlew`) — included in the repo
- **Python + pipx** (for `pre-commit`)
- **Node.js ≥ 18** (for local Conventional Commits validation via `commitlint`)
- (Optional) **Docker** for local infra (used in later iterations)

> **Why Node?** We use `commitlint` via `npx` to enforce **Conventional Commits** in the local `commit-msg` hook.

### Tooling specifics

- **pre-commit config file**: `.pre-commit-config.yaml` (repo root).
- **Prettier (Node hook)**: formats `*.json`, `*.md`, `*.yaml`/`*.yml` via a local Node hook pinned to `prettier@3.3.3`.
- **Google Java Format**: enforced via local script `scripts/google-java-format.sh`. The script downloads the JAR from Maven Central on first run and caches it under `.git-hooks-cache/`. Override the version by exporting `GJF_VERSION` (default `1.22.0`).
- **MkDocs YAML**: `mkdocs.yml` uses a Python constructor tag for Mermaid and is intentionally **excluded** from the `check-yaml` hook to avoid false positives.

---

## First‑time Setup

Install the git hooks (runs once per machine):

```bash
# Install pre-commit (recommended via pipx)
pipx install pre-commit  # or: pip install --user pre-commit

# Ensure formatter script is executable (one time)
chmod +x scripts/google-java-format.sh || true

# Install local git hooks (pre-commit + commit-msg)
make pre-commit-install
```

> If you change hook definitions, re-run: `pre-commit clean && pre-commit install`.

---

## Day‑to‑day Commands

The repo ships a minimal **Makefile** that wraps Gradle and common hygiene tasks.

```bash
make build     # Gradle build
make test      # Gradle test
make check     # Gradle check (extend as needed)
make lint      # Run all pre-commit hooks across the repo
make format    # Apply formatting hooks (JSON/Markdown/Java) via pre-commit
```

> Prefer the **Gradle wrapper** in CI and locally: `./gradlew`.

---

## Conventional Commits

All commit messages **must** follow **Conventional Commits**. Examples:

- `feat(account): enforce currency validation (USD/EUR/COP)`
- `fix(transaction): handle duplicate sourceId on ingestion`
- `chore(ci): enable Gradle cache`

The local `commit-msg` hook validates messages. If it fails, amend:

```bash
git commit --amend
```

---

## Pull Request Template

Every PR should include a short summary and tick the relevant checklist items:

- Scope/ADRs updated when applicable
- **OpenAPI** + contract **CHANGELOG** updated if the API surface changed
- Tests updated (unit / integration / CDC)
- Security considerations reviewed (headers, authz, tenant scoping)

> The template lives at `.github/PULL_REQUEST_TEMPLATE.md` and appears automatically when opening a PR.

---

## Continuous Integration

Minimal GitHub Actions workflow (`.github/workflows/ci.yml`) runs on push/PR:

- Java 21 setup
- `./gradlew test`
- `pre-commit run --all-files`

Keep builds green; fix style/format issues locally with `make format`.

---

## Editor/IDE Tips

- Enable **“format on save”** if your IDE supports Google Java Format.
- Ensure the IDE uses **Java 21** and the project **Gradle wrapper**.
- Consider installing a **Conventional Commits** plugin/extension.

---

## Troubleshooting

- **Hooks don’t run** → re‑install: `make pre-commit-install`.
- **commitlint fails** → ensure Node ≥ 18 (`node -v`) and re‑try.
- **Gradle not found** → always use the wrapper `./gradlew`.
- **CI fails on pre-commit** → run `make lint`, commit the fixes, push again.
- **YAML constructor error in `mkdocs.yml`** → this file is excluded from the `check-yaml` hook because it uses `!!python/name:` for Mermaid. If you still see warnings, run `pre-commit clean && pre-commit install`.
- **Java files not reformatted** → ensure the formatter script is executable (`chmod +x scripts/google-java-format.sh`). The JAR is cached under `.git-hooks-cache/`. Bump the version temporarily with `GJF_VERSION=1.23.0 pre-commit run --all-files`.

---

### Paths of interest

- Contracts (OpenAPI): `docs/contracts/openapi/v1/openapi.yaml`
- ADRs: `docs/architecture/decisions/`
- Governance: `docs/governance/`
- Deployment: `docs/ops/deployment-render.md`

---

> Status: _API‑first iterations_. Frontend lives in a separate repository and will be addressed in future sprints.
