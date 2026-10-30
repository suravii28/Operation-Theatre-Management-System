#!/usr/bin/env bash
# Enables the repository's git hooks for this clone.
set -euo pipefail

root=$(git rev-parse --show-toplevel)
cd "$root"

chmod +x .githooks/pre-push scripts/*.sh
git config core.hooksPath .githooks

echo "Git hooks enabled (core.hooksPath=.githooks)."
echo "Backend and frontend tests now run automatically before every push."
echo ""
echo "Next steps once application code exists:"
echo "  cd backend && ./mvnw -q -DskipTests dependency:go-offline"
echo "  cd frontend && npm ci"
