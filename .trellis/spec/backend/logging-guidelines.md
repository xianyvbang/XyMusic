# Logging Guidelines

> How logging is done in this project.

## Scenario: HTTP and Login Log Sanitization

### 1. Scope / Trigger
- Trigger: any API request/response logging, especially login flows and URL-based token propagation.
- Applies to: `Ktor` logs, login state logs, and helper utilities that print request URLs.

### 2. Signatures
- `HttpLogSanitizer.sanitize(message: String): String`
- `HttpLogSanitizer.sanitizeUrl(url: String): String`

### 3. Contracts
- Redact `Authorization`, `X-Emby-Authorization`, `X-Plex-Token`, `token`, and `api_key`.
- Release builds must not emit raw HTTP logging.
- Login logs may keep non-sensitive context only: connection id, service type, HTTP status, and success/failure.

### 4. Validation & Error Matrix
- Sensitive header/query value present -> replace with `***`.
- Query string contains multiple params -> redact one key without swallowing the next key.
- Unknown log format -> keep non-sensitive text intact.

### 5. Good/Base/Bad Cases
- Good: `Authorization: ***`
- Base: `GET /music?id=1&token=***`
- Bad: `token=secret&api_key=secret` left visible or partially consumed.

### 6. Tests Required
- Unit test for header redaction.
- Unit test for query redaction.
- Regression test that `token=...&api_key=...` keeps both keys and redacts both values.

### 7. Wrong vs Correct
#### Wrong
```text
token=query-secret&api_key=api-secret
```
#### Correct
```text
token=***&api_key=***
```
