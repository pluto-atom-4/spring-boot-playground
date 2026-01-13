package local.playground.springboot.contribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository interface for {@link Contribution} entities.
 *
 * <p>
 * Provides data access operations and specialized aggregation queries for computing
 * team contribution metrics (max, avg, total, count) per category. This repository
 * follows the separation-of-concerns pattern: raw SQL execution is delegated to JPA
 * queries, while business logic (null-handling, aggregation, validation) is handled
 * in the service layer ({@link local.playground.springboot.service.ContributionService}).
 * </p>
 *
 * <p>
 * <b>Architecture Pattern:</b>
 * </p>
 * <ul>
 *   <li><b>Repository:</b> Executes JPQL/native SQL and returns raw rows (List&lt;Map&lt;String, Object&gt;&gt; or DTOs)</li>
 *   <li><b>Service:</b> Converts repository results into domain-friendly types and implements business rules</li>
 *   <li><b>Controller:</b> Exposes APIs (not required for unit testing the service layer)</li>
 * </ul>
 *
 * <p>
 * <b>Query Return Types:</b>
 * </p>
 * <ul>
 *   <li>{@code List<Map<String, Object>>} – Generic, flexible but less type-safe. Used by default aggregation methods.</li>
 *   <li>{@code List<ContributionCategoryMax>} – Typed DTO record, type-safe and immutable. Preferred for new code.</li>
 *   <li>{@code List<Object[]>} – Native query results as tuples, useful for complex native SQL aggregations.</li>
 * </ul>
 *
 * <p>
 * <b>H2 Integration Testing Notes:</b>
 * When running integration tests with H2 in-memory database, the following configuration is required:
 * </p>
 * <ul>
 *   <li>Enable globally_quoted_identifiers in Hibernate config to handle reserved keywords (e.g., {@code value})</li>
 *   <li>Quote identifiers in native queries: {@code SELECT "category", MAX("value") FROM "team_contributions"}</li>
 *   <li>Verify DDL is created and dialect is set to {@code H2Dialect}</li>
 *   <li>Use {@code @SpringBootTest} or {@code @DataJpaTest} with {@code src/test/resources/application.properties}</li>
 * </ul>
 *
 * @see ContributionCategoryMax
 * @see local.playground.springboot.service.ContributionService
 * @since 1.0
 */
@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    /**
     * Finds the maximum contribution value per category (generic map-based projection).
     *
     * <p>
     * <b>Purpose:</b> Aggregates contributions by category and computes the maximum value
     * for each category. Returns raw map objects (List&lt;Map&lt;String, Object&gt;&gt;)
     * which are then processed by the service layer for null-handling and validation.
     * </p>
     *
     * <p>
     * <b>Query Logic:</b>
     * Executes: {@code SELECT c.category AS category, MAX(c.value) AS maxValue FROM Contribution c GROUP BY c.category}
     * </p>
     *
     * <p>
     * <b>Null-Handling:</b> If no contributions exist for a category, the repository may return
     * null values. The service layer ({@link local.playground.springboot.service.ContributionService#getMaxContributionPerCategory(local.playground.springboot.service.ContributionService.NullHandling)})
     * handles nulls according to the configured strategy (SKIP_NULLS or THROW_ON_NULL).
     * </p>
     *
     * <p>
     * <b>Return Format:</b> Each map contains:
     * <ul>
     *   <li>{@code "category"} → String (team contribution category)</li>
     *   <li>{@code "maxValue"} → Integer (maximum value, or null if no data)</li>
     * </ul>
     * </p>
     *
     * @return list of maps with "category" and "maxValue" keys; empty list if no contributions exist
     * @see #findMaxContributionPerCategoryTyped() for type-safe alternative
     */
    @Query("SELECT c.category AS category, MAX(c.value) AS maxValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findMaxContributionPerCategory();

    /**
     * Finds the maximum contribution value per category (typed DTO projection).
     *
     * <p>
     * <b>Purpose:</b> Same as {@link #findMaxContributionPerCategory()} but uses a typed
     * DTO record ({@link ContributionCategoryMax}) instead of generic maps. Provides
     * better type safety and immutability.
     * </p>
     *
     * <p>
     * <b>Query Logic:</b>
     * Executes: {@code SELECT new local.playground.springboot.contribution.ContributionCategoryMax(c.category, MAX(c.value)) FROM Contribution c GROUP BY c.category}
     * </p>
     *
     * <p>
     * <b>Advantages over Map-based projection:</b>
     * <ul>
     *   <li>Compile-time type checking for category and maxValue fields</li>
     *   <li>Immutable record design prevents accidental modifications</li>
     *   <li>Direct field access without string-based key lookups</li>
     *   <li>Built-in {@code equals()}, {@code hashCode()}, and {@code toString()}</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Null-Handling:</b> If no contributions exist for a category, maxValue will be null.
     * The service layer processes nulls according to its configured strategy.
     * </p>
     *
     * @return list of {@link ContributionCategoryMax} records; empty list if no contributions exist
     * @see ContributionCategoryMax
     * @see local.playground.springboot.service.ContributionService#getMaxContributionPerCategoryFromTyped()
     */
    @Query("SELECT new local.playground.springboot.contribution.ContributionCategoryMax(c.category, MAX(c.value)) " +
           "FROM Contribution c GROUP BY c.category")
    List<ContributionCategoryMax> findMaxContributionPerCategoryTyped();

    /**
     * Finds the maximum contribution value per category using a native SQL query.
     *
     * <p>
     * <b>Purpose:</b> Provides a native SQL alternative for database-specific optimizations
     * or edge cases where JPQL cannot express the required aggregation. Returns tuples as
     * {@code Object[]} arrays: [category, maxValue].
     * </p>
     *
     * <p>
     * <b>Query Logic:</b>
     * Executes: {@code SELECT "category", MAX("value") as max_value FROM "team_contributions" GROUP BY "category"}
     * </p>
     *
     * <p>
     * <b>H2-Specific Notes:</b> Identifiers are quoted ({@code "category"}, {@code "value"})
     * to avoid issues with reserved keywords and maintain compatibility with Hibernate's
     * {@code globally_quoted_identifiers} setting. For other databases (PostgreSQL, MySQL, etc.),
     * adjust quoting style accordingly.
     * </p>
     *
     * <p>
     * <b>Return Format:</b> Each Object[] array contains:
     * <ul>
     *   <li>Index 0: category (String)</li>
     *   <li>Index 1: max_value (Integer or Long, database-dependent)</li>
     * </ul>
     * </p>
     *
     * @return list of Object[] tuples; empty list if no contributions exist
     */
    @Query(value = "SELECT \"category\", MAX(\"value\") as max_value FROM \"team_contributions\" GROUP BY \"category\"", nativeQuery = true)
    List<Object[]> findMaxContributionPerCategoryNative();

    /**
     * Finds the average contribution value per category.
     *
     * <p>
     * <b>Purpose:</b> Aggregates contributions by category and computes the average value
     * for each category using generic map-based projection.
     * </p>
     *
     * <p>
     * <b>Query Logic:</b>
     * Executes: {@code SELECT c.category AS category, AVG(c.value) AS avgValue FROM Contribution c GROUP BY c.category}
     * </p>
     *
     * <p>
     * <b>Null-Handling:</b> If a category has no contributions, avgValue may be null.
     * The service layer applies its configured null-handling strategy.
     * </p>
     *
     * <p>
     * <b>Return Format:</b> Each map contains:
     * <ul>
     *   <li>{@code "category"} → String</li>
     *   <li>{@code "avgValue"} → Double (database average may vary in precision)</li>
     * </ul>
     * </p>
     *
     * @return list of maps with "category" and "avgValue" keys; empty list if no contributions exist
     * @see local.playground.springboot.service.ContributionService#getAverageContributionPerCategory()
     */
    @Query("SELECT c.category AS category, AVG(c.value) AS avgValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findAvgContributionPerCategory();

    /**
     * Finds the total (sum) contribution value per category.
     *
     * <p>
     * <b>Purpose:</b> Aggregates contributions by category and computes the total (sum) value
     * for each category using generic map-based projection.
     * </p>
     *
     * <p>
     * <b>Query Logic:</b>
     * Executes: {@code SELECT c.category AS category, SUM(c.value) AS totalValue FROM Contribution c GROUP BY c.category}
     * </p>
     *
     * <p>
     * <b>Null-Handling:</b> If a category has no contributions, totalValue may be null.
     * The service layer applies its configured null-handling strategy.
     * </p>
     *
     * <p>
     * <b>Return Format:</b> Each map contains:
     * <ul>
     *   <li>{@code "category"} → String</li>
     *   <li>{@code "totalValue"} → Long (database sum result)</li>
     * </ul>
     * </p>
     *
     * @return list of maps with "category" and "totalValue" keys; empty list if no contributions exist
     * @see local.playground.springboot.service.ContributionService#getTotalContributionPerCategory()
     */
    @Query("SELECT c.category AS category, SUM(c.value) AS totalValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findTotalContributionPerCategory();

    /**
     * Finds the count of contributions per category.
     *
     * <p>
     * <b>Purpose:</b> Aggregates contributions by category and computes the count of contributions
     * for each category using generic map-based projection.
     * </p>
     *
     * <p>
     * <b>Query Logic:</b>
     * Executes: {@code SELECT c.category AS category, COUNT(c) AS countValue FROM Contribution c GROUP BY c.category}
     * </p>
     *
     * <p>
     * <b>Null-Handling:</b> COUNT always returns a value (0 or greater); nulls are unlikely
     * but the service layer still applies its null-handling strategy for consistency.
     * </p>
     *
     * <p>
     * <b>Return Format:</b> Each map contains:
     * <ul>
     *   <li>{@code "category"} → String</li>
     *   <li>{@code "countValue"} → Long (count of contributions)</li>
     * </ul>
     * </p>
     *
     * @return list of maps with "category" and "countValue" keys; empty list if no contributions exist
     * @see local.playground.springboot.service.ContributionService#getCountPerCategory()
     */
    @Query("SELECT c.category AS category, COUNT(c) AS countValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findCountPerCategory();
}
