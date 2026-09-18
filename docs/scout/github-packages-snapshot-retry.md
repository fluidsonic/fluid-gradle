# GitHub Packages snapshot publish retry works around a non-idempotent PUT

`retryGithubSnapshotPublications()` (`sources/utility/GithubPackagesPublishing.kt`, called from `PublishingConfigurator.configure` right after the `publishing {}` block) wraps every `PublishToMavenRepository` task named `*ToGithubRepository`, replacing its action so a failed SNAPSHOT publish retries up to 3 attempts with a `5s * attempt` backoff.

Why: GitHub Packages answers a repeated `PUT` to an already-stored path with `409 Conflict`, and sometimes stores the file but drops the response. Gradle's upload retry (`NetworkOperationBackOffAndRetry`, 3 attempts / 1000 ms) resends on 5xx/socket errors, turning a lost success response into a 409 that fails the task.

Retrying is safe only for snapshots: GitHub Packages generates the versioned `maven-metadata.xml` server-side from whatever files are already stored, so a republish reads the partial upload's metadata and takes the next build number — nothing is re-uploaded to the same path. Against a Maven server that only serves what was PUT, a retry would repeat the same build number and 409 again. Release versions have fixed paths and are never retried; Sonatype tasks and `publishToMavenLocal` are untouched.

The wrapper calls the public `PublishToMavenRepository.publish()` (Gradle 9.7.1), capturing only the task and `project.provider { publication.version }` (`publicationVersion` in `sources/utility/GithubPackagesPublishing.kt`) — never `actions.toList()`. `PublishToMavenRepository.publication`/`.repository` are `Transient.Var` fields Gradle discards on configuration-cache serialization; reading `publication.version` after a cache reload throws "the value of this property has been discarded during serialization", so it must be captured via a provider at configuration time.

Known limitation: the plugin's real `github` repository (`sources/PublishingConfigurator.kt`) uses explicit `credentials { username/password }`, which Gradle currently marks `PublishToMavenRepository` incompatible with the configuration cache regardless of this wrapper; `credentials(PasswordCredentials::class)` would avoid it.

Duplicated in `buildSrc` (identical logic, different package/visibility) — see `buildsrc-duplication.md`. Tested via a fake server — see `github-packages-retry-testing.md`.
