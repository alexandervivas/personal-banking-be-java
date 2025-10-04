.PHONY: help build test check lint format pre-commit-install

help:
	@echo "Targets:"
	@echo "  build <module>      - Gradle build a specific module"
	@echo "  test                - Gradle test"
	@echo "  check               - Gradle check"
	@echo "  lint                - pre-commit in the whole repo"
	@echo "  format              - formatea (hooks pre-commit)"
	@echo "  pre-commit-install  - instala hooks (incluye commit-msg)"

build:
	./gradlew :modules:$(module):bootJar

test:
	./gradlew -q test

check:
	./gradlew -q check

lint:
	pre-commit run --all-files || true

format:
	pre-commit run --all-files --hook-stage manual prettier pretty-format-json google-java-format || true

pre-commit-install:
	pre-commit install
	pre-commit install --hook-type commit-msg
	@echo "✔ Hooks instalados"
