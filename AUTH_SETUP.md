# Auth Service Setup

This backend now acts as a resource microservice. It supports two bearer-token paths:

- JWTs issued by a separate auth server such as Keycloak
- First-party JWTs issued by this service after Google sign-in

## Local auth server

Start the infrastructure:

```powershell
docker compose up -d
```

Services:

- PostgreSQL: `localhost:5433`
- Keycloak: `http://localhost:8180`
- Ride service issuer expectation: `http://localhost:8180/realms/ride-hailing`

Keycloak bootstrap credentials:

- Username: `admin`
- Password: `admin`

Demo realm users:

- `rider.demo` / `password`
- `driver.demo` / `password`
- `dispatcher.demo` / `password`
- `admin.demo` / `password`

## Configuration

Set these environment variables for local Google login:

```powershell
$env:GOOGLE_CLIENT_IDS="your-google-web-client-id.apps.googleusercontent.com"
$env:APP_JWT_SECRET="ride-hailing-local-jwt-secret-2026"
```

`APP_JWT_SECRET` should be a strong secret with at least 32 bytes.

## Resource service behavior

Protected endpoints require a JWT bearer token with Keycloak realm roles. Mapped roles become Spring authorities:

- `RIDER` -> `ROLE_RIDER`
- `DRIVER` -> `ROLE_DRIVER`
- `DISPATCHER` -> `ROLE_DISPATCHER`
- `ADMIN` -> `ROLE_ADMIN`

Public endpoints:

- `/actuator/health`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/api/v1/auth/me`
- `/api/v1/auth/google/login`

`/api/v1/auth/me` returns anonymous details without a token and resolved principal details with a token. Use it to verify the auth handshake before calling protected APIs.

## Google login

Exchange a Google ID token for an API JWT:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/auth/google/login" `
  -ContentType "application/json" `
  -Body '{
    "idToken": "GOOGLE_ID_TOKEN",
    "phoneNumber": "+263771234567"
  }'
```

Behavior:

- Verifies the Google ID token against the configured Google client ID
- Looks for an existing linked auth account
- Falls back to rider lookup by email address
- Creates a rider automatically if no rider exists
- Returns a local JWT for subsequent API calls

`phoneNumber` is optional for Google-created riders. Existing manual rider creation still requires it.

## Get a token

Example using the seeded public client:

```powershell
$body = @{
  client_id = "ride-hailing-api"
  username = "rider.demo"
  password = "password"
  grant_type = "password"
}

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8180/realms/ride-hailing/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body $body
```

Then call the backend:

```powershell
$token = "<access_token>"

Invoke-RestMethod `
  -Headers @{ Authorization = "Bearer $token" } `
  -Uri "http://localhost:8080/api/v1/auth/me"
```

## Google sign-in via Keycloak

If you want Google auth, do it in Keycloak instead of this backend:

1. Create Google OAuth credentials in Google Cloud.
2. In Keycloak admin, open `Identity providers`.
3. Add `Google`.
4. Configure the Google client ID and secret.
5. Map Google users to realm roles such as `RIDER` or `DRIVER`.

That keeps this service as a pure ride-domain microservice and lets the auth server own login, federation, token issuance, password policies, and account linking.
