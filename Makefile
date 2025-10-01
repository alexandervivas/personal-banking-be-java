GRADLE := $(if $(wildcard ./gradlew),./gradlew,gradle)

.PHONY: help build test check lint format pre-commit-install

help:
	@echo "Targets:"
	@echo "  build               - Gradle build"
	@echo "  test                - Gradle test"
	@echo "  check               - Gradle check"
	@echo "  lint                - pre-commit en todo el repo"
	@echo "  format              - formatea (hooks pre-commit)"
	@echo "  pre-commit-install  - instala hooks (incluye commit-msg)"

build:
	$(GRADLE) -q build

test:
	$(GRADLE) -q test

check:
	$(GRADLE) -q check

lint:
	pre-commit run --all-files || true

format:
	pre-commit run --all-files --hook-stage manual prettier pretty-format-json google-java-format || true

pre-commit-install:
	pre-commit install
	pre-commit install --hook-type commit-msg
	@echo "✔ Hooks instalados"
