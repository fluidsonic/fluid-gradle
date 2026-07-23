# Plugin activation and the fluidLibrary root-project contract

How this plugin actually gets applied to a consumer project, and the hard runtime contract on the root project.

Applying the Gradle plugin id `io.fluidsonic.gradle` alone does **nothing**: `sources/LibraryPlugin.kt` (`LibraryPlugin.apply`) is an empty marker. All configuration runs only when the consumer calls the extension function `fluidLibrary(...)` (root `build.gradle.kts`) or `fluidLibraryModule(...)` (each subproject), defined in `sources/LibraryDsl.kt` and `sources/LibraryModuleDsl.kt`.

`fluidLibrary()` enforces three `check`s that fail at configuration time, not compile time:
- `project.name` must exactly equal the computed `fullName`. Because `prefixName` defaults to `true`, `fullName` is `"fluid-$name"`, so a project for `fluidLibrary(name = "country")` must be named `fluid-country` (see `settings.gradle.kts` `rootProject.name`). A directory/project named just `country` fails.
- must be the root project (`project.parent == null`).
- may be used only once (no existing `LibraryPluginExtension`).

`fluidLibrary()` also silently injects two experimental opt-ins into **every** module before applying the user block: `kotlin.contracts.ExperimentalContracts` and `kotlin.experimental.ExperimentalTypeInference` (via `allModules { language { withExperimentalApi(...) } }`). Consumers get these without declaring them; do not re-add them.

The resolved `LibraryConfiguration` is stashed on the root project as `LibraryPluginExtension`; modules read it back through `Project.fluidLibrary` in `sources/LibraryPluginExtension.kt`.
