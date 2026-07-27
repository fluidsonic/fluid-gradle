# The test summary line is a `TestListener` class, not an `afterSuite { }` closure

Why the one-line-per-test-task summary is implemented the way it is, and the two constraints that must hold if it is touched. See `test-console-output.md` for the `testLogging` side.

`TestSummaryLogger` (`sources/utility/TestSummaryLogger.kt`) is registered by `configureTestTask` in `sources/LibraryModuleConfigurator.kt` through `AbstractTestTask.addTestListener`.

**Do not simplify it into `afterSuite { }`.** `AbstractTestTask.afterSuite(Closure)` and `beforeSuite(Closure)` are deprecated in Gradle 9 and, per their own deprecation wiring, removed in Gradle 10; their javadoc names `addTestListener(TestListener)` — which is not deprecated — as the replacement.

**The listener must capture no Gradle model object.** It ends up in the configuration-cache entry, so it holds only the task path `String` and obtains its logger from the static `Logging.getLogger`, never from the task or the project. Handing it the task, `project.logger`, or anything reachable from them breaks the configuration cache for every consumer. `jvmTest_summarySurvivesConfigurationCache` in `tests/io/fluidsonic/gradle/LibraryModuleConfiguratorFunctionalTest.kt` guards this with a two-run build.

Pitfalls: `afterSuite` fires for every class and engine suite, so the summary is emitted only for the root suite (the one whose `parent` is null). And a configuration-cache functional test must pass `--rerun` on *both* runs — otherwise the second run finds the test task up to date, the listener never fires, and the assertion fails for the wrong reason.
