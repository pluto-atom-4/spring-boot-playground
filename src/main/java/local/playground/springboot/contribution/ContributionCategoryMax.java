package local.playground.springboot.contribution;

/**
 * Typed projection (DTO) record for aggregated max contribution per category.
 *
 * <p>
 * This record is used as a strongly-typed result for JPQL queries that aggregate
 * contributions by category and compute the maximum value. Using a record instead
 * of a generic {@code Map<String, Object>} improves type safety and readability.
 * </p>
 *
 * <p>
 * <b>Benefits of using a record:</b>
 * <ul>
 *   <li>Type-safe: guarantees {@code category} is a String and {@code maxValue} is an Integer</li>
 *   <li>Immutable: records are final and provide no setters</li>
 *   <li>Concise: no need for getters; fields are directly accessible as methods</li>
 *   <li>Built-in {@code equals()}, {@code hashCode()}, and {@code toString()}</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage in repository queries:</b>
 * </p>
 * <pre>
 * {@code
 * @Query("SELECT new local.playground.springboot.contribution.ContributionCategoryMax(c.category, MAX(c.value)) " +
 *        "FROM Contribution c GROUP BY c.category")
 * List<ContributionCategoryMax> findMaxContributionPerCategoryTyped();
 * }
 * </pre>
 *
 * <p>
 * <b>Fields:</b>
 * </p>
 * <ul>
 *   <li>{@code category} – the team contribution category (e.g., "Engineering", "Marketing")</li>
 *   <li>{@code maxValue} – the maximum contribution value in that category (may be null)</li>
 * </ul>
 *
 * @see ContributionRepository#findMaxContributionPerCategoryTyped()
 * @since 1.0
 */
public record ContributionCategoryMax(String category, Integer maxValue) {
}

