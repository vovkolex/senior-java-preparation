# Spring Security — Senior Interview Notes

## Security filter chain
Spring Security is primarily filter-based for web requests.

Typical flow:
```text
HTTP request
  -> SecurityFilterChain
  -> authentication filter
  -> SecurityContext
  -> authorization
  -> controller
```

Authentication answers **who are you?** Authorization answers **what are you allowed to do?**

## JWT basics
A JWT commonly contains header, payload claims and signature.

Important claims:
- `sub`: subject/user,
- `iss`: issuer,
- `aud`: intended audience,
- `exp`: expiration,
- `iat`: issued-at,
- roles/scopes as application-specific claims.

Always validate signature, expiration, issuer and audience according to your trust model.

## Symmetric vs asymmetric signing
- HS256: shared secret signs/verifies. Every verifier holding the secret can also sign tokens.
- RS256/ES256: private key signs, public key verifies. Better separation for distributed systems.

## OAuth2 and OIDC
OAuth2 is an authorization framework. OpenID Connect adds identity/authentication on top of OAuth2.

Common actors:
- Resource Owner,
- Client,
- Authorization Server / Identity Provider,
- Resource Server.

Authorization Code flow with PKCE is common for user-facing applications.

## Access vs refresh token
- Access token: short-lived credential sent to APIs.
- Refresh token: longer-lived credential used to obtain a new access token; protect more carefully and rotate/revoke when appropriate.

Do not send refresh tokens to every resource API.

## CSRF
CSRF exploits browsers automatically attaching credentials such as cookies.

- Important for cookie/session-based authentication.
- A stateless API using Authorization bearer tokens not automatically attached by browser is typically less exposed to classic CSRF.
- Do not disable CSRF blindly; understand credential transport.

## CORS
CORS is a browser policy controlling whether frontend JavaScript from one origin may call another origin.

It is **not authentication or authorization**. The server still must authenticate and authorize requests.

## Roles and authorities
Spring Security commonly models permissions as `GrantedAuthority`.

Method security example:
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(...) {}
```

Prefer permission design that reflects real business capabilities rather than accumulating arbitrary role checks everywhere.

## Passwords
Store passwords with adaptive one-way hashing (e.g. bcrypt/Argon2/PBKDF2 depending policy), never reversible encryption or plain SHA-256 alone. Use framework-supported password encoders.

## Common traps
- JWT payload is encoded, not encrypted by default.
- Signature validates integrity/authenticity, not confidentiality.
- CORS does not secure backend endpoints from non-browser clients.
- OAuth2 and OIDC are not synonyms.
- A valid JWT can still be unacceptable if issuer/audience is wrong.

## Interview-ready summary
> Spring Security protects web applications through a filter chain. I separate authentication from authorization, validate JWT issuer/audience/expiration as well as signature, understand OAuth2 versus OIDC, and treat CSRF and CORS as different browser-security concerns rather than authorization mechanisms.

## Review questions
1. Authentication vs authorization?
2. OAuth2 vs OIDC?
3. HS256 vs RS256?
4. Why validate JWT audience?
5. CSRF vs CORS?
6. Access token vs refresh token?
