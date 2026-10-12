# Spring Core / Boot / AOP — Senior Interview Notes

## IoC and DI
Spring creates and manages beans inside the ApplicationContext. Constructor injection is preferred because dependencies are explicit, testable and can be final.

```java
@Service
@RequiredArgsConstructor
class OrderService {
    private final PaymentClient paymentClient;
}
```

Use `@Qualifier` when multiple beans of the same type exist.

## Bean scopes
- `singleton` (default): one bean instance per ApplicationContext.
- `prototype`: new instance each retrieval/injection request.
- web scopes: `request`, `session`, etc.

A singleton bean must be designed for concurrent access. Avoid mutable request-specific fields.

## Lifecycle
Typical lifecycle concepts:
1. instantiate bean,
2. inject dependencies,
3. BeanPostProcessors / proxying,
4. initialization (`@PostConstruct`),
5. bean in use,
6. destruction (`@PreDestroy`) on context shutdown for applicable scopes.

## `@Component` vs `@Bean`
- `@Component` / `@Service` / `@Repository`: component scanning.
- `@Bean`: explicit factory method, useful for third-party classes or custom construction.
- `@Configuration`: configuration class whose `@Bean` methods are managed by Spring.

## Auto-configuration
Spring Boot auto-configuration creates beans based on classpath, properties and existing beans. Common conditional annotations include `@ConditionalOnClass`, `@ConditionalOnProperty`, `@ConditionalOnMissingBean`.

`@ConditionalOnMissingBean` is useful for defaults that users can override with their own bean.

## Circular dependencies
Constructor-based circular dependencies fail because neither bean can be fully created first. Better fix the design by splitting responsibilities. `@Lazy` can break a cycle technically but often hides a design smell.

## AOP and proxies
Spring commonly applies cross-cutting concerns through proxies, e.g. transactions, security, caching.

- JDK dynamic proxy: interface-based.
- CGLIB subclass proxy: class-based.

A proxy intercepts an external method call, runs advice, then delegates to the target.

### Self-invocation
```java
public void outer() {
    inner(); // this.inner() — bypasses proxy
}

@Transactional
public void inner() {}
```

The annotation advice is not applied for the internal call in standard proxy mode. Move the method to another bean or make the externally invoked method transactional.

## Spring MVC request flow
Typical flow:

```text
HTTP request
  -> Filter chain
  -> DispatcherServlet
  -> HandlerInterceptor preHandle
  -> Controller
  -> Service
  -> HandlerInterceptor afterCompletion
  -> HTTP response
```

- Filter: servlet-level concerns, before DispatcherServlet; useful for security/logging/correlation.
- HandlerInterceptor: Spring MVC handler lifecycle.
- AOP: method-level cross-cutting logic inside Spring beans.

## REST controller essentials
- `@RestController` = `@Controller` + response body serialization.
- `@PathVariable`: identifies resource path component.
- `@RequestParam`: query parameter/filter/paging input.
- `ResponseEntity`: explicit status/headers/body.
- `@RestControllerAdvice` + `@ExceptionHandler`: centralized API error mapping.
- `HttpMessageConverter`: converts request/response payloads (e.g. JSON via Jackson).

## HTTP idempotency
GET/PUT/DELETE are intended to be idempotent semantically; POST is not automatically idempotent. For retryable create/payment operations, use a client-provided idempotency key and persist the result atomically.

## Common traps
- Singleton does not mean thread-safe.
- `@Service` is not transactional by itself.
- Calling an annotated method via `this` bypasses proxy advice.
- Filters, interceptors and AOP solve different layers of problems.
- Spring Boot auto-configuration does not mean 'magic'; it is conditional bean registration.

## Interview-ready summary
> Spring manages application components through IoC and dependency injection. I prefer constructor injection, keep singleton beans stateless, understand bean lifecycle and conditional auto-configuration, and use the right interception layer—Filter, MVC interceptor, or AOP—based on where the concern belongs.

## Review questions
1. Why prefer constructor injection?
2. Why can singleton beans have concurrency bugs?
3. `@Bean` vs `@Component`?
4. How does self-invocation affect Spring AOP?
5. Filter vs Interceptor vs AOP?
