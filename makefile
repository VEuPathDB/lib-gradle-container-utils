.PHONY: build
build:
	@echo "Choose a target"

.PHONY: validate
validate:
	@./gradlew validatePlugins

.PHONY: publish
publish:
	@./gradlew publishPluginMavenPublicationToGitHubRepository publishContainer-utilsPluginMarkerMavenPublicationToGitHubRepository

