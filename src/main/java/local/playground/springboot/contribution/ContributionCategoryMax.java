package local.playground.springboot.contribution;

/**
 * Typed projection (DTO) for max contribution per category.
 */
public record ContributionCategoryMax(String category, Integer maxValue) {
}

