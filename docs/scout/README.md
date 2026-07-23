<!-- scout-format: 1 -->
<!-- This file is generated and maintained by the genki-dev `scout` agent. It is the
     human-facing entry point for the knowledge base; the agent rewrites it from the
     shipped template on bootstrap. Edit the template in the plugin, not this copy. -->

# Scout knowledge base

This directory is **Scout** — a small, curated knowledge base for this repository,
written *for* the AI agents that work on it. It captures the non-obvious, repo-specific
knowledge a developer accumulates over time: conventions worth following, traps that
aren't visible in the code, how features flow end-to-end, how a library is *actually*
used here, and how the project ships. Anything a capable model could re-derive by
reading the code for a few minutes does not belong here.

A dedicated `scout` agent writes and curates these files; you generally don't edit them
by hand. They're committed and reviewed in pull requests like any other change. If a
note ever disagrees with the code, **trust the code** — it may have gone stale.

**Turning Scout off:** create an empty `.scout-disabled` file at the repository root.
While it exists, Scout stays dormant; delete it to re-enable. (Removing `docs/scout/`
entirely also disables Scout.)
