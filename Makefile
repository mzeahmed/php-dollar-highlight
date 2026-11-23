# PHP Dollar Highlight - common commands
# Override on the command line, e.g.: make build LOCAL_IDE=/opt/phpstorm
# Set LOCAL_IDE= (empty) to download PhpStorm instead of using a local install.

JAVA_HOME_25 ?= /usr/lib/jvm/java-25-openjdk-amd64
LOCAL_IDE    ?= $(HOME)/.local/share/JetBrains/Toolbox/apps/phpstorm-3

GRADLE := ./gradlew --console=plain -Dorg.gradle.java.home=$(JAVA_HOME_25) \
          $(if $(LOCAL_IDE),-PlocalIde=$(LOCAL_IDE))

ZIP := $(wildcard build/distributions/*.zip)

.DEFAULT_GOAL := help
.PHONY: help build test verify all run clean zip

help: ## Show this help
	@grep -E '^[a-z-]+:.*##' $(MAKEFILE_LIST) | awk -F':.*## ' '{printf "  make %-8s %s\n", $$1, $$2}'

build: ## Build the installable ZIP (build/distributions/)
	$(GRADLE) buildPlugin

test: ## Run the automated tests
	$(GRADLE) test

verify: ## Run the JetBrains Plugin Verifier
	$(GRADLE) verifyPlugin

all: ## Clean, test, build and verify
	$(GRADLE) clean test buildPlugin verifyPlugin

run: ## Launch a sandbox PhpStorm with the plugin
	$(GRADLE) runIde

clean: ## Remove build outputs
	$(GRADLE) clean

zip: ## Print the path of the built ZIP
	@ls -1 build/distributions/*.zip
