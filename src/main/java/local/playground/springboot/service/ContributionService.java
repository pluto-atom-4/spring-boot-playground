package local.playground.springboot.service;

import local.playground.springboot.contribution.Contribution;
import local.playground.springboot.contribution.ContributionCategoryMax;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ContributionService {
    Contribution save(Contribution contribution);
    Optional<Contribution> findById(Long id);
    List<Contribution> findAll();

    // the aggregation method under test - default behavior
    Map<String, Integer> getMaxContributionPerCategory();

    // Overload with explicit null-handling strategy
    Map<String, Integer> getMaxContributionPerCategory(NullHandling handling);

    // Typed helper using the JPQL record projection
    Map<String, Integer> getMaxContributionPerCategoryFromTyped();

    // Additional metrics
    Map<String, Double> getAverageContributionPerCategory();
    Map<String, Double> getAverageContributionPerCategory(NullHandling handling);

    Map<String, Long> getTotalContributionPerCategory();
    Map<String, Long> getTotalContributionPerCategory(NullHandling handling);

    Map<String, Long> getCountPerCategory();
    Map<String, Long> getCountPerCategory(NullHandling handling);

    // Null handling strategies for aggregation results
    enum NullHandling {
        // Skip rows with null category or null numeric value
        SKIP_NULLS,
        // Throw an exception when encountering null category or null numeric value
        THROW_ON_NULL
    }
}
