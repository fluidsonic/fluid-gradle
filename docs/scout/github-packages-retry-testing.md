# Testing the GitHub Packages retry needs a fake server that mimics its metadata quirk

Applies when changing `GithubPackagesPublishRetryFunctionalTest.kt` or `FaultInjectingMavenServer.kt` (both under `tests/io/fluidsonic/gradle/`), which exercise `github-packages-snapshot-retry.md`'s wrapper end to end via `GradleRunner`.

`FaultInjectingMavenServer` stores `PUT` bodies in memory and answers `409` on a repeat `PUT` to an already-stored path — except `maven-metadata.xml` and its checksum siblings, which may always be overwritten (a real snapshot publish couldn't happen otherwise). For exactly one `PUT` matching its `faultPredicate`, it stores the body but answers `503`, simulating a lost success response. Its key trick: `GET` on the versioned SNAPSHOT `maven-metadata.xml` does not return whatever a client uploaded — it generates the metadata from the artifact files already stored in that directory, the same way GitHub Packages does. This is what lets a retry see the partial first build and take the next build number; a server that served stored content literally would report the failed build as nonexistent and defeat the test.

The functional test's module script must set `isAllowInsecureProtocol = true` on the repurposed `github` repository (the fake server is plain HTTP) and `signing { setRequired(false) }` with no signatory configured, which skips the `Sign` tasks so no `.asc` artifacts need publishing — that keeps the test project minimal.

`GithubPackagesPublishRetryFunctionalTest.githubPublishTask_isConfigurationCacheCompatible` uses a credential-less `github` repository (registered by the test module script, not the plugin) specifically to isolate the wrapper's own action from the plugin's explicit-credentials configuration-cache limitation.

It runs the publish task for real on both invocations, never `--dry-run`: dry-run stores and reloads the task graph without executing any action, so it would pass even if reading a discarded `Transient.Var` field crashed the real action (see `github-packages-snapshot-retry.md`).
