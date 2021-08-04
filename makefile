.PHONY: build
build:
	@echo "Choose a target"

.PHONY: validate
validate:
	@./gradlew validatePlugins

.PHONY: publish
publish:
	@./gradlew -Pfull-publish=true publishPluginMavenPublicationToGitHubRepository publishContainer-utilsPluginMarkerMavenPublicationToGitHubRepository

.PHONY: publish-local
publish-local:
	@./gradlew publishPluginMavenPublicationToMavenLocal publishContainer-utilsPluginMarkerMavenPublicationToMavenLocal
