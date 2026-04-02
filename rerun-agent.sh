#!/usr/bin/env bash
# Usage: ./rerun-agent.sh <ISSUE_ID>
# Clears stale worktrees for an issue and re-runs the AgentFactory orchestrator.
set -euo pipefail

ISSUE="${1:?Usage: $0 <ISSUE_ID> (e.g. BAC-7)}"
AGENT_DIR="/workspace/agentfactory/my-agent"
WT_DIR="/workspace/agentfactory/my-agent.wt"

# 1. Make sure the my-agent main working tree is not on the target branch.
current_branch=$(git -C "$AGENT_DIR" branch --show-current)
if [[ "$current_branch" == "$ISSUE" ]]; then
  echo "INFO: my-agent is on branch '$ISSUE', switching to main..."
  git -C "$AGENT_DIR" checkout main
fi

# 2. Remove any stale worktrees for this issue.
for wt in "$WT_DIR/$ISSUE"-*; do
  [[ -d "$wt" ]] || continue
  echo "INFO: Removing stale worktree $wt ..."
  git -C "$AGENT_DIR" worktree remove --force "$wt"
done

# 3. Delete the local branch so the orchestrator can recreate it cleanly.
if git -C "$AGENT_DIR" show-ref --verify --quiet "refs/heads/$ISSUE"; then
  echo "INFO: Deleting local branch '$ISSUE'..."
  git -C "$AGENT_DIR" branch -D "$ISSUE"
fi

# 4. Run the orchestrator.
echo "INFO: Starting orchestrator for $ISSUE ..."
cd "$AGENT_DIR"
npx af-orchestrator --single "$ISSUE"
