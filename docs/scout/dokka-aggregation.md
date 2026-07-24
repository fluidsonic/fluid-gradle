# Dokka v2 cross-module aggregation wires itself with no lifecycle hook

How the aggregated Dokka site is assembled across modules, and why the wiring works without any registry or `projectsEvaluated` callback.

Two halves collaborate:
- The **root** applies `org.jetbrains.dokka` (in `configure()` in `sources/LibraryConfigurator.kt`), gated on `configuration.defaultModuleConfiguration.isDokkaEnabled` — the `allModules {}` default. Applying the plugin is what creates the root's `dokka` configuration. So `allModules { disableDokka() }` makes the root `dokka` configuration (and the aggregating task) simply not exist.
- Each **Dokka-enabled module** registers itself into that root configuration via `rootProject.dependencies.add("dokka", rootProject.dependencies.project(project.path))`, inside the `if (configuration.isDokkaEnabled)` guard in `configureBasics()` in `sources/LibraryModuleConfigurator.kt`.

The load-bearing, non-obvious fact: Gradle always configures the root project before its subprojects, so the root `dokka` configuration already exists when any module evaluates. No registry, no `projectsEvaluated`, no lifecycle hooks are needed. Both opt-outs fall out of the AND-merge of `isDokkaEnabled` (see `configuration-merge.md`).

Verification trap: Dokka's "Couldn't resolve link" warnings come from per-module analysis (the module's compile classpath), not from aggregation. Counting those warnings does **not** confirm this feature — verify via the aggregated HTML (all modules present, cross-module hyperlinks work). The aggregating task is `dokkaGeneratePublicationHtml` (also consumed by the javadoc jar; see `publishing.md`).

version: 2.2.0 (as of 2026-07, `gradle/libs.versions.toml`)
