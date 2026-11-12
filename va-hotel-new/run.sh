#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

if ! command -v java >/dev/null 2>&1; then
  echo "Java 21 is required. Install a JDK, then run this script again." >&2
  exit 1
fi
if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven is required. Install Maven 3.9+, then run this script again." >&2
  exit 1
fi

export PORT="${PORT:-8080}"
echo "Starting VA Hotel. Open http://localhost:${PORT} when startup completes. Stop with Ctrl+C."
exec mvn spring-boot:run
