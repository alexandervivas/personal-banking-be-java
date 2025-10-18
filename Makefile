.PHONY: help build test check lint format pre-commit-install

# Build usage:
# - make build                -> build all modules (all bootJar tasks)
# - make build tenants api    -> build multiple modules
# - make build module=tenants -> legacy style still supported
# Collect all positional args after the target name
ARGS := $(wordlist 2,999,$(MAKECMDGOALS))

# Prevent make from treating positional args as targets
ifneq ($(strip $(ARGS)),)
  $(foreach a,$(ARGS),$(eval $(a):;@:))
endif

# Resolve modules: prefer positional args if provided, else fall back to module variable
MODULES := $(strip $(if $(ARGS),$(ARGS),$(module)))

help:
	@echo "Targets:"
	@echo "  build [<module>...] - Build all modules (no args) or one/many modules (positional or module=)"
	@echo "                       e.g., 'make build', 'make build tenants', 'make build tenants api', 'make build module=tenants'"
	@echo "  test                - Gradle test"
	@echo "  check               - Gradle check"
	@echo "  lint                - pre-commit in the whole repo"
	@echo "  format              - formatea (hooks pre-commit)"
	@echo "  pre-commit-install  - instala hooks (incluye commit-msg)"

build:
ifneq ($(strip $(MODULES)),)
	@set -e; \
	for m in $(MODULES); do \
		./gradlew :modules:$$m:bootJar; \
	done
else
	./gradlew bootJar
endif

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
	@echo "✔ Hooks successfully installed"
