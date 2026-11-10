#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

start_local_m1() {
  echo "El entorno local usa Docker. Si Docker esta apagado, ejecuta: sudo systemctl start docker"
  command -v docker >/dev/null || { echo "Falta Docker para iniciar M1/Keycloak local." >&2; exit 1; }
  docker info >/dev/null 2>&1 || { echo "Docker no esta iniciado. Ejecuta: sudo systemctl start docker" >&2; exit 1; }
  docker compose up -d keycloak

  for attempt in {1..60}; do
    if curl --fail --silent --output /dev/null http://localhost:8080/realms/ds-2026-realm/.well-known/openid-configuration; then
      return
    fi
    sleep 1
  done
  echo "M1/Keycloak no quedo disponible en http://localhost:8080. Revisa: docker compose logs keycloak" >&2
  exit 1
}

start_local_m1

# An explicit datasource uses the developer's configured PostgreSQL server.
# Otherwise provision a separate cluster inside this checkout, never the system database.
if [[ -z "${SPRING_DATASOURCE_URL:-}" ]]; then
  for command_name in initdb pg_ctl psql createdb; do
    command -v "$command_name" >/dev/null || { echo "Falta $command_name. Instalá PostgreSQL o configurá SPRING_DATASOURCE_URL." >&2; exit 1; }
  done
  db_dir="$PWD/.local/postgres"
  mkdir -p .local
  if [[ ! -f "$db_dir/PG_VERSION" ]]; then
    initdb -D "$db_dir" -A trust -U m3_user > .local/postgres-init.log
  fi
  if ! pg_ctl -D "$db_dir" status >/dev/null 2>&1; then
    pg_ctl -D "$db_dir" -l "$PWD/.local/postgres.log" -o "-p 55434 -h 127.0.0.1 -k /tmp" start
  fi
  if [[ "$(psql -h 127.0.0.1 -p 55434 -U m3_user -d postgres -Atc "SELECT 1 FROM pg_database WHERE datname='m3_db'")" != "1" ]]; then
    createdb -h 127.0.0.1 -p 55434 -U m3_user m3_db
  fi
  export SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:55434/m3_db
  export SPRING_DATASOURCE_USERNAME=m3_user
  export SPRING_DATASOURCE_PASSWORD=m3_password
  psql -h 127.0.0.1 -p 55434 -U m3_user -d m3_db -v ON_ERROR_STOP=1 -c "INSERT INTO drivers (id, identity_subject, display_name, qualification_status, qualification_reason, first_name, last_name, dni, birth_date, address, phone, email, version, created_at, updated_at) VALUES ('f0000000-0000-0000-0000-000000000002', 'c7c497ec-71cf-48ba-a387-23df38eda8d6', 'María Demo · Keycloak', 'APPROVED', 'Perfil local vinculado a M1 para la demostración', 'María', 'Demo', '44900123', DATE '1992-06-14', 'Av. Costanera 1234, Corrientes', '+54 379 455-0101', 'm3-demo@example.com', 1, now(), now()) ON CONFLICT (identity_subject) DO NOTHING;" >/dev/null
fi
export OIDC_ISSUER="${OIDC_ISSUER:-http://localhost:8080/realms/ds-2026-realm}"
export OIDC_JWKS_URI="${OIDC_JWKS_URI:-http://localhost:8080/realms/ds-2026-realm/protocol/openid-connect/certs}"
export OIDC_AUTH_URI="${OIDC_AUTH_URI:-http://localhost:8080/realms/ds-2026-realm/protocol/openid-connect/auth}"
export OIDC_TOKEN_URI="${OIDC_TOKEN_URI:-http://localhost:8080/realms/ds-2026-realm/protocol/openid-connect/token}"
export OIDC_USERINFO_URI="${OIDC_USERINFO_URI:-http://localhost:8080/realms/ds-2026-realm/protocol/openid-connect/userinfo}"
export OIDC_WEB_CLIENT_ID="${OIDC_WEB_CLIENT_ID:-m3-web-client}"

exec bash ./mvnw -Dspring-boot.run.profiles=production -Dspring-boot.run.arguments=--server.address=127.0.0.1 "$@" spring-boot:run
