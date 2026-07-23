# fluid()/kotlinx() coordinate derivation is asymmetric

The `fluid()` and `kotlinx()` dependency-notation helpers derive group and artifact by different rules from the same argument — non-obvious from their signatures.

In `sources/LibraryModuleConfigurationBuilder.kt` (`DependenciesBuilder`):

`fluid(simpleModuleName, version, usePrefix = true)` builds `io.fluidsonic.<segment>:fluid-<simpleModuleName>:<version>`, where `<segment>` is only the part of the name **before the first hyphen** (`substringBefore('-')`), while the artifact keeps the full name. So `fluid("country-data", "1.0")` yields group `io.fluidsonic.country` but artifact `fluid-country-data`. `usePrefix = false` drops only the `fluid-` artifact prefix (group unchanged): `fluid("json", "1.0", usePrefix = false)` -> `io.fluidsonic.json:json:1.0`.

`kotlinx(simpleModuleName, version, usePrefix = true)` always uses the constant group `org.jetbrains.kotlinx` and prepends `kotlinx-` to the artifact: `kotlinx("coroutines-core", "1.7")` -> `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7`. `usePrefix = false` drops only the `kotlinx-` artifact prefix; the group is fixed regardless.

These derivations encode the fluidsonic ecosystem naming convention and are pinned by `tests/io/fluidsonic/gradle/DependenciesBuilderTest.kt` (`fluid_handlesModuleNameWithHyphen`, `kotlinx_returnsCorrectCoordinateWithoutPrefix`).
