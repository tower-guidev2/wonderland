# Session Protocol — Non-Negotiable

---

## Session Start

Before any work, perform a full project audit:

1. Read CLAUDE.md and all referenced rules/skills files. Confirm understanding.
2. Check current branch: `git branch --show-current`
3. Review working tree state: `git status` and `git diff --stat`
4. Read recent commit history: `git log --oneline -20`
5. Identify any uncommitted changes, stashes, or unresolved conflicts.
6. Cross-check the current task against open TODOs or FIXMEs in the working area.
7. Summarise findings: current branch, pending changes, last completed work, next logical step.
8. Do NOT begin work until this summary is presented and the user has confirmed the plan.

---

## Session End

Before closing, complete the following:

1. Summarise all work done: files changed, patterns applied, decisions made.
2. List every mistake, correction, or false start that occurred during this session.
3. For each mistake, state the root cause and the concrete rule that prevents recurrence.
4. Check: does a rule already exist in CLAUDE.md or a rules file that covers it?
   - **YES and it was violated** — flag as a compliance failure. No new rule needed.
   - **NO** — draft a new rule in the exact format used by existing rules files.
5. Present all proposed rule additions/updates for user approval before writing them.
6. Once approved, apply the changes to the relevant files immediately.
7. Confirm all changes are committed with a clear commit message.
8. State the recommended starting point for the next session.
