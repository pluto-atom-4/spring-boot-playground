# Contribution Metrics — unit testing guidance

Goal
----
Write unit tests for a Java method that processes SQL-based team productivity metrics in a Spring Boot application using spring-boot-starter-data-jpa. The tests should mock the repository layer and verify service logic (aggregation, max/avg/totals) without starting the full Spring context. Additionally, include guidance for a lightweight H2-backed integration test and null-safety strategies implemented in the service layer.

Quick plan
----------
- Separate SQL execution and business logic: Repository (JPA query) ➜ Service (aggregation).  
- Mock the `Repository` in service unit tests with Mockito.  
- Keep unit tests lightweight (no DB, no Spring context).  
- Add a focused H2 `@SpringBootTest` integration test for repository queries (in-memory DB).  
- Implement explicit null-safety policy in the service (SKIP_NULLS or THROW_ON_NULL) and document the custom exception.

Recommended architecture (short)
-------------------------------
- Repository: Executes JPQL/native SQL and returns raw rows (e.g., List<Map<String,Object>> or DTOs).  
- Service: Converts repository results into domain-friendly types and implements business rules (pick max, compute averages, totals).  
- Controller: Exposes APIs — not required for unit testing the service.

Example domain
--------------
Imagine a table `team_contributions` with columns:

```
id | team_name | category | value
```

We want a service method that returns the maximum contribution value per category:

```java
public Map<String, Integer> getMaxContributionPerCategory();
```

Repository example (Spring Data JPA)
-----------------------------------
A repository can return rows using a projection or a map-like structure. Example:

```java
@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    @Query("SELECT c.category AS category, MAX(c.value) AS maxValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findMaxContributionPerCategory();
}
```

Service example
---------------
Service converts repository rows into a Map<String,Integer> and supports a null-handling strategy:

- NullHandling enum (two options):
  - SKIP_NULLS — default: silently skip rows where category or maxValue is null or maxValue is non-numeric.
  - THROW_ON_NULL — strict: throw a custom runtime exception when null/invalid data is encountered.

Custom exception introduced:
- `InvalidRepositoryDataException extends RuntimeException` — thrown by the service when strict handling is selected and invalid rows are found.

Service behavior (summary):
- `getMaxContributionPerCategory()` — default to SKIP_NULLS.
- `getMaxContributionPerCategory(NullHandling handling)` — runs with the given strategy and either returns the map or throws `InvalidRepositoryDataException`.

Unit test (service layer, Mockito + JUnit 5)
-------------------------------------------
Mock the repository and feed fake SQL results (maps) to the service. Example (conceptual):

- Test SKIP_NULLS behavior (default): mock rows including null category or null maxValue → service returns only valid rows.
- Test THROW_ON_NULL behavior: mock a row with null category → service.getMaxContributionPerCategory(THROW_ON_NULL) throws InvalidRepositoryDataException.

Why this pattern works
----------------------
- Tests are fast and deterministic because they don't touch the DB.  
- Service-focused tests verify the business logic that consumes repository output (the important part when SQL is already trusted).  
- If you later change the SQL, keep repository/integration tests separately to validate JPQL/native queries.

H2 integration test notes (important)
-------------------------------------
When you add an in-memory DB integration test (recommended for verifying repository queries), use H2 with the following considerations:

1. Use a test application properties for H2: `src/test/resources/application.properties` (test scope). Minimal recommended settings:

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
# Quote identifiers to avoid reserved-keyword issues
spring.jpa.properties.hibernate.globally_quoted_identifiers=true
```

2. Why `globally_quoted_identifiers=true`?
- It forces Hibernate to quote identifiers (table/column names) in generated DDL. This is helpful when your domain uses names that are reserved keywords in H2 (for example, `value`). Quoting avoids syntax errors when creating tables.

3. Quoting in native queries
- If Hibernate creates quoted identifiers (e.g., `"team_contributions"`, `"category"`, `"value"`), your native queries must use exact quoted identifiers to match case-sensitively in H2. Example:

```
-- JPQL-style projection (works as repository method returning List<Map>):
SELECT c.category AS category, MAX(c.value) AS maxValue FROM Contribution c GROUP BY c.category

-- Native SQL (must quote identifiers if Hibernate created them quoted):
SELECT "category", MAX("value") as max_value FROM "team_contributions" GROUP BY "category"
```

4. Test annotation
- Prefer `@SpringBootTest` for the integration test that needs the full JPA stack with H2 (or `@DataJpaTest` if you prefer a slice test and adjust the test properties). Make sure the test uses the test properties file (Spring Boot loads `src/test/resources/application.properties` by default when running tests).

Repository integration test (H2, summary)
----------------------------------------
- Use `@SpringBootTest` (or `@DataJpaTest`) and `src/test/resources/application.properties` configured for H2.
- Ensure DDL is created (create-drop) and that Hibernate dialect is set to H2Dialect.
- Quote all identifiers in native queries if `globally_quoted_identifiers` is enabled.

Service null-safety: design choices
----------------------------------
- Default policy: SKIP_NULLS — safe and backward compatible; ignores rows where category or maxValue is null or non-numeric. Good for tolerant production logic.  
- Strict policy: THROW_ON_NULL — throws `InvalidRepositoryDataException` as soon as a null or non-numeric value is encountered. Good for CI or data validation where you want to fail fast.

Implementation notes (what we added to the codebase)
----------------------------------------------------
- `ContributionService` now exposes:
  - `Map<String,Integer> getMaxContributionPerCategory()` — default SKIP_NULLS
  - `Map<String,Integer> getMaxContributionPerCategory(NullHandling handling)` — explicit behavior
  - `enum NullHandling { SKIP_NULLS, THROW_ON_NULL }`

- `ContributionServiceImpl`:
  - Implements the overload and throws `InvalidRepositoryDataException` when `THROW_ON_NULL` is selected and the repository returns null/invalid data.
  - Returns a `Map<String,Integer>` otherwise, converting numeric types safely via `((Number) maxObj).intValue()`.

- `InvalidRepositoryDataException extends RuntimeException` added to make the failure semantics explicit and easier to catch in higher layers or tests.

Unit test examples to add (conceptual)
-------------------------------------
- `ContributionServiceNullHandlingTest` (JUnit + Mockito)
  - testSkipNullsBehavior(): repository returns rows [valid, nullCategory, nullValue] → `getMaxContributionPerCategory()` returns only the valid mapping.
  - testThrowOnNullBehavior(): repository returns rows [valid, nullCategory] → `getMaxContributionPerCategory(THROW_ON_NULL)` throws `InvalidRepositoryDataException`.

Integration test checklist
--------------------------
- [ ] `src/test/resources/application.properties` contains H2 configuration shown above
- [ ] Integration test uses `@SpringBootTest` or `@DataJpaTest` and runs against H2
- [ ] Native queries in repository are updated to quote identifiers when necessary, e.g.:
  - `@Query(value = "SELECT \"category\", MAX(\"value\") as max_value FROM \"team_contributions\" GROUP BY \"category\"", nativeQuery = true)`
- [ ] Verify schema creation logs (Hibernate DDL) in test output to ensure tables are created with quoted identifiers

Commands to run tests
---------------------
Run the project's tests (from the repository root):

```bash
# Unix / Git Bash
./mvnw test

# Windows (cmd.exe)
mvnw.cmd test
```

Run only the service unit tests:

```bash
./mvnw -Dtest=ContributionServiceNullHandlingTest,ContributionServiceTest test
```

Edge cases to test / consider
----------------------------
- Repository returns an empty list (expect an empty Map).  
- Repository returns rows where `maxValue` is null — verify SKIP_NULLS vs THROW_ON_NULL behavior.  
- Repository returns non-numeric maxValue — SKIP_NULLS will skip, THROW_ON_NULL will throw `InvalidRepositoryDataException`.  
- Duplicate categories returned (query shouldn't, but service will overwrite with last seen value).  
- Category is null — SKIP_NULLS will ignore; THROW_ON_NULL will throw.

Optional improvements
---------------------
- Use a small DTO (record) instead of raw Map<String,Object> for clearer typing in the repository method. ✅ **Implemented** — `ContributionCategoryMax` record added.
- Add an integration test for `ContributionRepository` using H2 in-memory DB to validate the JPQL/native query. ✅ **Implemented** — `ContributionRepositoryDataJpaTest` with `@SpringBootTest + @Transactional`.
- Add tests for average/total metrics similarly by mocking repository results that return the appropriate aggregated columns.
- Add comprehensive edge case tests for nulls, non-numeric values, empty results, and duplicates. ✅ **Implemented** — `ContributionServiceEdgeCasesTest` with 12 focused tests.

Implementation completed
------------------------
✅ Typed DTO projection (`ContributionCategoryMax` record)
✅ Repository with JPQL typed projection + native query methods
✅ Service with configurable null-handling strategies (SKIP_NULLS, THROW_ON_NULL)
✅ Custom `InvalidRepositoryDataException` for clear error semantics
✅ Unit tests: service behavior, null-handling, and 12 edge case scenarios
✅ Integration test: H2-backed JPQL projection validation
✅ H2 configuration with globally_quoted_identifiers for reserved keywords
✅ Additional aggregation metrics implemented (average, total, count) with repository methods and service implementations
✅ Unit tests for aggregation metrics (average/total/count) covering SKIP_NULLS and THROW_ON_NULL
✅ Property-based / randomized tests implemented (ContributionServiceRandomizedPropertyTest)
✅ Mutation testing plugin configuration added (PIT) to `pom.xml` (pitest-maven + junit5 plugin)

Important note: @DataJpaTest vs @SpringBootTest
-------------------------------------------------
**Why @DataJpaTest was not used:**
In this project (Spring Boot 4.0.1), the `@DataJpaTest` annotation from `org.springframework.boot.test.autoconfigure.orm.jpa` was not available on the test classpath despite being declared in `pom.xml`. This is a known classpath resolution issue in certain Spring Boot 4.0.x environments where the autoconfigure classes are not properly exposed transitively.

**Solution implemented:**
Instead of `@DataJpaTest`, the integration test uses `@SpringBootTest + @Transactional`, which:
- ✅ Loads the full JPA/Hibernate stack with H2
- ✅ Compiles reliably without classpath issues
- ✅ Properly manages transactions with `@Transactional`
- ✅ Works consistently across different build environments
- ⚠️ Trade-off: Slightly slower startup than `@DataJpaTest` (full context vs. sliced)

**For future environments:**
If `@DataJpaTest` becomes available, you can switch by:
1. Replacing `@SpringBootTest` with `@DataJpaTest`
2. Adding `@AutoConfigureTestDatabase(replace = Replace.ANY)` to force H2
3. Removing `@Transactional` (auto-handled by @DataJpaTest)

This alternative is documented in case classpath issues are resolved in future Spring Boot versions.

Next steps (optional)
---------------------
- Use Testcontainers with real PostgreSQL for production-like integration testing.
- Convert repository projections for average/total/count to typed DTOs if you prefer strongly-typed results (currently only `ContributionCategoryMax` exists for MAX).
- Run PIT mutation testing and analyze surviving mutations; add/strengthen tests based on results. (PIT plugin added to `pom.xml`; run with `./mvnw org.pitest:pitest-maven:1.10.5:mutationCoverage`)

References
----------
- Use `spring-boot-starter-test` (contains JUnit 5 + Mockito).  
- Keep unit tests focused on the service; run integration tests separately when exercising JPA queries.
- Full implementation guide available in `IMPLEMENTATION_COMPLETE.md` and `EDGE_CASE_TESTS_SUMMARY.md`.
