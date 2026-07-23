# Consumer source-set directory layout uses target suffixes

Where a consuming module's sources must live — this differs from a normal Kotlin Multiplatform default and from this plugin's own build layout.

`configureSourceSetBasics` in `sources/LibraryModuleConfigurator.kt` remaps every source set's directories by a `pathSuffix`:
- main -> `sources<suffix>/` and `resources<suffix>/`
- test -> `tests<suffix>/` and `test-resources<suffix>/`

The suffix is `""` for the common target, `-jvm` for JVM, `-js` for JS. So common code goes in `sources/`, JVM code in `sources-jvm/`, JS tests in `tests-js/`, etc. There is no `src/main/kotlin`.

Note the mismatch: this plugin's **own** build (`build.gradle.kts`, the `sourceSets` block) maps its `main` to `sources` and `test` to `tests` flat, because the plugin itself is a plain JVM project, not a consumer of its own multiplatform layout. Do not use the plugin's own layout as a template for a consumer module.
