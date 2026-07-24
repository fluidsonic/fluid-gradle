# Wrapper checksum pinning: the doFirst fetch is mandatory, not cosmetic

Why the `Wrapper` task carries a `doFirst` that downloads the distribution checksum, and what breaks if it is removed.

Once `gradle/wrapper/gradle-wrapper.properties` contains `distributionSha256Sum`, Gradle's `wrapper` task **fails** when its configuration does not define a sum — previously every Gradle version bump required passing `--gradle-distribution-sha256-sum` by hand. The `doFirst` block (in the `wrapper {}` block of `sources/LibraryConfigurator.kt` for consumers, and duplicated in this repo's own `build.gradle.kts`) fetches `${distributionUrl}.sha256` via `fetchGradleDistributionSha256Sum`, validates it with `parseSha256Checksum` (exactly 64 lowercase hex characters; pinned by `tests/io/fluidsonic/gradle/ParseSha256ChecksumTest.kt`), and assigns `distributionSha256Sum`. Removing the block does not merely drop verification — it makes every future wrapper regeneration fail.

The fetch relies on a redirect: `https://services.gradle.org/distributions/<dist>.sha256` responds 301 to `https://downloads.gradle.org/distributions/<dist>.sha256`. Java's `HttpURLConnection` follows same-protocol redirects by default, which the helper depends on; reproducing the request with `curl` needs `-L`.

Trust model: trust-on-first-use over TLS at bump time — the sum is logged at lifecycle level so a human can cross-check it against https://gradle.org/release-checksums/ before committing; the committed pin then protects all downstream downloads.

The helper exists twice (`sources/utility/GradleWrapperChecksum.kt` and `buildSrc/.../buildsupport/GradleWrapperChecksum.kt`) for structural reasons — see `buildsrc-duplication.md`.
