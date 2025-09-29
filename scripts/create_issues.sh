#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   scripts/create_issues.sh -f sprints/sprint-1.json [-r owner/repo] [-m "Iteration 1"] [--dry-run]
#
# If -r is not provided, tries to infer from `git remote origin`.
# If -m is not provided, uses each issue's "milestone" from the JSON.
#
# JSON format: array of objects with fields:
#   title, body, labels (array of strings), assignees (array, optional), milestone (string)

# --- CLI args ---
REPO=""
MILESTONE_OVERRIDE=""
JSON_FILE=""
DRY_RUN="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    -r|--repo) REPO="$2"; shift 2;;
    -m|--milestone) MILESTONE_OVERRIDE="$2"; shift 2;;
    -f|--file) JSON_FILE="$2"; shift 2;;
    --dry-run) DRY_RUN="true"; shift;;
    *) echo "Unknown arg: $1"; exit 1;;
  esac
done

if [[ -z "${JSON_FILE}" ]]; then
  echo "Error: provide -f path/to/issues.json"
  exit 1
fi

# --- prerequisites ---
command -v gh >/dev/null 2>&1 || { echo "Error: gh CLI is required (https://cli.github.com/)"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Error: jq is required"; exit 1; }
if ! gh auth status >/dev/null 2>&1; then
  echo "Error: gh is not authenticated. Run: gh auth login"
  exit 1
fi

# --- resolve repo ---
if [[ -z "${REPO}" ]]; then
  origin_url="$(git config --get remote.origin.url || true)"
  if [[ -z "${origin_url}" ]]; then
    echo "Error: cannot infer repo. Pass -r owner/repo"
    exit 1
  fi
  # handle ssh and https remotes
  if [[ "${origin_url}" =~ ^git@github.com:(.+)/(.+)\.git$ ]]; then
    REPO="${BASH_REMATCH[1]}/${BASH_REMATCH[2]}"
  elif [[ "${origin_url}" =~ ^https://github.com/(.+)/(.+)\.git$ ]]; then
    REPO="${BASH_REMATCH[1]}/${BASH_REMATCH[2]}"
  elif [[ "${origin_url}" =~ ^https://github.com/(.+)/(.+)$ ]]; then
    REPO="${BASHREMATCH[1]}/${BASHREMATCH[2]}"
  else
    echo "Error: unrecognized remote URL '${origin_url}'. Pass -r owner/repo"
    exit 1
  fi
fi

echo "Using repo: ${REPO}"

# --- helpers ---
label_color() {
  local name="$1"
  case "$name" in
    area/*) echo "1f6feb" ;;      # blue
    module/*) echo "b6e3ff" ;;    # light blue
    type/feature) echo "2ea44f" ;;# green
    type/chore) echo "fbca04" ;;  # yellow
    type/test) echo "5319e7" ;;   # purple
    prio/must) echo "d73a4a" ;;   # red
    prio/should) echo "0e8a16" ;; # green darker
    prio/could) echo "c2e0c6" ;;  # gray/green
    *) echo "ededed" ;;           # gray
  esac
}

ensure_label() {
  local label="$1"
  # Check existence
  if gh api --silent "repos/${REPO}/labels/$(python3 - <<EOF
import urllib.parse,sys
print(urllib.parse.quote(sys.argv[1]))
EOF
"${label}")" >/dev/null 2>&1; then
    return 0
  fi
  # Create if missing
  local color
  color="$(label_color "$label")"
  echo "Creating label '${label}' (${color})"
  gh api -X POST "repos/${REPO}/labels" \
    -f name="$label" -f color="$color" \
    -f description="Auto-created by create_issues.sh" >/dev/null
}

# Cache milestone numbers by title
declare -A MILESTONE_MAP

ensure_milestone() {
  local title="$1"
  if [[ -n "${MILESTONE_MAP[$title]+x}" ]]; then
    echo "${MILESTONE_MAP[$title]}"
    return 0
  fi
  # Try to find
  local number
  number="$(gh api "repos/${REPO}/milestones?state=all&per_page=100" | jq -r ".[] | select(.title==\"${title}\") | .number" | head -n1 || true)"
  if [[ -z "$number" ]]; then
    echo "Creating milestone '${title}'"
    number="$(gh api -X POST "repos/${REPO}/milestones" -f title="$title" | jq -r '.number')"
  fi
  MILESTONE_MAP["$title"]="$number"
  echo "$number"
}

create_issue() {
  local title="$1" body="$2" milestone_title="$3" labels_json="$4" assignees_json="$5"

  local ms_title
  if [[ -n "${MILESTONE_OVERRIDE}" ]]; then
    ms_title="${MILESTONE_OVERRIDE}"
  else
    ms_title="${milestone_title}"
  fi
  [[ -z "${ms_title}" ]] && ms_title="Backlog"

  # Ensure milestone exists (and cache number)
  ensure_milestone "${ms_title}" >/dev/null

  # Build label args
  local -a label_args=()
  if [[ -n "${labels_json}" && "${labels_json}" != "null" ]]; then
    mapfile -t labels < <(jq -r '.[]' <<< "${labels_json}")
    for l in "${labels[@]}"; do
      [[ -z "$l" ]] && continue
      ensure_label "$l"
      label_args+=(--label "$l")
    done
  fi

  # Build assignee args
  local -a assign_args=()
  if [[ -n "${assignees_json}" && "${assignees_json}" != "null" ]]; then
    mapfile -t assignees < <(jq -r '.[]' <<< "${assignees_json}")
    for a in "${assignees[@]}"; do
      [[ -z "$a" ]] && continue
      assign_args+=(--assignee "$a")
    done
  fi

  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "[DRY RUN] gh issue create --repo ${REPO} --title \"$title\" --milestone \"${ms_title}\" ${label_args[*]} ${assign_args[*]}"
    return 0
  fi

  gh issue create \
    --repo "${REPO}" \
    --title "$title" \
    --body "$body" \
    --milestone "${ms_title}" \
    "${label_args[@]}" \
    "${assign_args[@]}"
}

# --- main ---
issues_len="$(jq 'length' "${JSON_FILE}")"
echo "Creating ${issues_len} issue(s) from ${JSON_FILE} ..."

for i in $(seq 0 $((issues_len-1))); do
  item="$(jq -c ".[$i]" "${JSON_FILE}")"
  title="$(jq -r '.title' <<< "${item}")"
  body="$(jq -r '.body' <<< "${item}")"
  labels_json="$(jq -c '.labels' <<< "${item}")"
  assignees_json="$(jq -c '.assignees // empty' <<< "${item}")"
  milestone_title="$(jq -r '.milestone // empty' <<< "${item}")"

  echo "→ ${title}"
  create_issue "${title}" "${body}" "${milestone_title}" "${labels_json}" "${assignees_json}"
done

echo "Done ✅"
