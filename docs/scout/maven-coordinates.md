# Maven group uses the unprefixed name; URLs use the prefixed fullName

A subtle split in how a published module's coordinates versus its POM/repository URLs are derived.

In `configureBasics` (`sources/LibraryModuleConfigurator.kt`) the module's Maven `group` is set to `io.fluidsonic.${libraryConfiguration.name}` — the **unprefixed** name. So a library declared as `fluidLibrary(name = "country")` publishes under group `io.fluidsonic.country`, not `io.fluidsonic.fluid-country`.

But everything URL-shaped uses `fullName` (the `fluid-`-prefixed form): the GitHub Packages repository URL, and the POM `name`/`url`/`scm` fields in `configurePublishing` all interpolate `libraryConfiguration.fullName` -> `.../fluidsonic/fluid-country`.

So group and repository/URL identity diverge for the same module. Related: `dependency-notation.md` (the `fluid()` helper derives a dependency's group by the same unprefixed rule, but from a substring of the module name).
