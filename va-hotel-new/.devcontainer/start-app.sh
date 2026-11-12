#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
APP_PORT="${PORT:-8080}"

if curl --silent --fail --max-time 2 "http://localhost:${APP_PORT}/" >/dev/null; then
  exit 0
fi

nohup mvn spring-boot:run >/tmp/va-hotel-spring.log 2>&1 </dev/null &
echo $! >/tmp/va-hotel-maven.pid
