# How to gather contribution metrics (quick how-to)

This document explains the flow for collecting and testing SQL-based contribution metrics in this project and gives concrete steps to run the unit and integration tests. It is a condensed, actionable companion to `contribution-metrics.md` with a simple visual process flow you can copy into docs or PR descriptions.

Checklist (what I'll show):
- A compact process-flow diagram for metric collection and testing
- Where logic lives (Repository vs Service) and what to test where
- Null-safety strategies and the custom exception to expect
- Concrete commands to run unit and integration tests
- Tips for H2 integration testing and quoting identifiers

Process flow (visual)
```
Repository (JPA / SQL)
    |
    |— Executes JPQL / native SQL aggregation query
    |— Returns rows as typed projection (e.g. `ContributionCategoryMax`) or raw maps
    v
Service (business logic)
    |
    |— Converts repository rows into domain types
    |— Applies null-handling policy (SKIP_NULLS or THROW_ON_NULL)
    |— Aggregates into final Map<String,Integer> (or other metric DTOs)
    v
API / Consumer (controller, CLI, or tests)
```

This can be represented as a simple flow:

```
[Repository query] -> [Raw rows / DTOs] -> [Service conversion + policy] -> [Aggregated metrics]
```

What to test where

- Repository tests (integration)
  - Purpose: validate the JPQL / native SQL actually returns the columns and values your service expects.
  - Tools: H2 in-memory database, `@SpringBootTest` (or `@DataJpaTest` when classpath supports it).
  - Focus: schema creation, quoted identifiers (if `globally_quoted_identifiers=true`), native query correctness.

- Service tests (unit)
  - Purpose: validate conversion rules, null-safety policy, aggregation, numeric conversions.
  - Tools: JUnit 5 + Mockito (no Spring context). Mock the `ContributionRepository`.
  - Focus: SKIP_NULLS returns only valid entries; THROW_ON_NULL throws `InvalidRepositoryDataException` on invalid rows; empty repository => empty map; numeric conversions use `((Number) obj).intValue()` safely.

Null-safety strategies (design summary)

- SKIP_NULLS (default)
  - The service ignores rows where the `category` is null, the aggregated value is null, or the value is non-numeric.
  - Use when data may be dirty and you want tolerant behavior in production.

- THROW_ON_NULL (strict)
  - The service throws `InvalidRepositoryDataException` when it encounters a null/invalid category or numeric value.
  - Use for CI or data-validation where failing fast is desired.

Custom exception

- InvalidRepositoryDataException extends RuntimeException — service throws this when strict null handling is enabled and an invalid row is found.

Concrete unit-test patterns (examples)

1) Happy path (SKIP_NULLS default)
- Mock repository to return: [ { category: "A", maxValue: 10 }, { category: "B", maxValue: 5 } ]
- Call `contributionService.getMaxContributionPerCategory()`
- Assert map contains { "A" -> 10, "B" -> 5 }

2) Skip nulls (default tolerant)
- Mock repository to return: [ { category: "A", maxValue: 10 }, { category: null, maxValue: 99 }, { category: "C", maxValue: null } ]
- Call default service method
- Assert only "A" is present in the result

3) Throw on null (strict)
- Mock repository to return: [ { category: "A", maxValue: 10 }, { category: null, maxValue: 99 } ]
- Call `contributionService.getMaxContributionPerCategory(NullHandling.THROW_ON_NULL)`
- Expect `InvalidRepositoryDataException`

4) Non-numeric value handling
- Mock repository to return a non-Number object for the aggregated value
- SKIP_NULLS: row ignored
- THROW_ON_NULL: `InvalidRepositoryDataException`

Tips for writing service unit tests
- Don't start Spring context: use Mockito to mock the repository bean.
- Use typed DTO projection (projected record `ContributionCategoryMax`) where possible — it's clearer than raw maps.
- Cover: empty list, null category, null value, non-numeric value, duplicate categories.

H2 integration testing notes (quick)

- Use `src/test/resources/application.properties` configured for H2 (recommended properties in `contribution-metrics.md`). Key ones:
  - `spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false`
  - `spring.jpa.hibernate.ddl-auto=create-drop`
  - `spring.jpa.properties.hibernate.globally_quoted_identifiers=true` (important when column names like `value` are used)

- If you enable quoted identifiers, native SQL needs exact quoted names (e.g. `SELECT "category", MAX("value") FROM "team_contributions" GROUP BY "category"`).

- Test annotation: prefer `@SpringBootTest` + `@Transactional` in this project (see `contribution-metrics.md` for classpath notes). If `@DataJpaTest` works in your environment, it is OK to use that slice instead.

Commands — run tests locally

- Run full test suite (Unix/Git Bash):

```bash
./mvnw test
```

- Run full test suite (Windows cmd):

```bash
mvnw.cmd test
```

- Run only service unit tests (example):

```bash
./mvnw -Dtest=ContributionServiceNullHandlingTest,ContributionServiceTest test
```

Requirements coverage (mapping to `contribution-metrics.md`)
- Visual process: Done
- Repository vs Service responsibilities: Done
- Unit test guidance (Mockito + JUnit5): Done
- Integration H2 notes and quoted identifiers: Done
- Commands to run tests: Done

Next steps and optional improvements
- If you want, I can:
  - Create a small diagram image (SVG) and add it to the repo for richer visuals.
  - Generate skeleton unit test examples (Java files) under `src/test/java` using Mockito + JUnit 5.
  - Add or verify `src/test/resources/application.properties` contains the recommended H2 config.

If you'd like any of those next steps implemented, tell me which one and I'll apply it directly.

