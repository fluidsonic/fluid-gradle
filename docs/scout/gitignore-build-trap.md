# `.gitignore` `build` rule silently drops new files under any `build/` path component

A footgun that can make newly-added source files never get committed, with no error.

The repo `.gitignore` contains a bare `build` line (no leading slash, no trailing slash). A gitignore pattern without a slash matches a file or directory of that name at **any depth**, not just the top-level Gradle `build/` output directory. `.gitignore` affects only **untracked** files: a new, untracked file whose path contains a `build/` component is silently omitted — `git add` skips it and `git status` never lists it, with no error or signal. Already-tracked files are unaffected: git keeps showing them and never hides their modifications, regardless of the rule.

This concretely bites brand-new Kotlin source packages: a newly-added package named `io.fluidsonic.gradle.build` produces a `.../build/...` directory, so its (untracked) source files never get committed and nothing warns you. The `buildSrc` support package was named `buildsupport` (not `build`) specifically to escape this rule; see `buildsrc-duplication.md`.

Rule: never use `build` (or any other bare `.gitignore` token here — `out`, `.gradle`, `.DS_Store`) as a directory or Kotlin package-name component anywhere in this repo. If new source unexpectedly fails to commit, check it against `.gitignore` first.
