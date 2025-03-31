.PHONY: build
build:
	@echo "Choose a target"

.PHONY: validate
validate:
	@./gradlew validatePlugins

.PHONY: publish
publish:
	@./gradlew -Pfull-publish=true build publishPluginMavenPublicationToGitHubRepository publishContainerUtilsPluginMarkerMavenPublicationToGitHubRepository

.PHONY: publish-local
publish-local:
	@./gradlew build publishPluginMavenPublicationToMavenLocal publishContainerUtilsPluginMarkerMavenPublicationToMavenLocal
