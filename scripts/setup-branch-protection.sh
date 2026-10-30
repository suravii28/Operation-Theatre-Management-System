#!/usr/bin/env bash
# Applies branch protection to main. Requires GitHub CLI authenticated as an admin.
# Usage: ./scripts/setup-branch-protection.sh
set -euo pipefail

REPO="${REPO:-suravii28/Operation-Theatre-Management-System}"
BRANCH="${BRANCH:-main}"
REQUIRED_CHECK="${REQUIRED_CHECK:-CI Success}"

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required. Install it with: brew install gh" >&2
  echo "Then run: gh auth login" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "Not authenticated with GitHub. Run: gh auth login" >&2
  exit 1
fi

echo "Protecting ${REPO}@${BRANCH} (required check: ${REQUIRED_CHECK}) ..."

gh api --method PUT "repos/${REPO}/branches/${BRANCH}/protection" \
  --input - <<JSON
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["${REQUIRED_CHECK}"]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "required_approving_review_count": 1,
    "require_last_push_approval": true
  },
  "restrictions": null,
  "required_linear_history": false,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true
}
JSON

gh api --method PATCH "repos/${REPO}" -F delete_branch_on_merge=true >/dev/null

echo "Done."
echo "Verify: gh api repos/${REPO}/branches/${BRANCH}/protection"
