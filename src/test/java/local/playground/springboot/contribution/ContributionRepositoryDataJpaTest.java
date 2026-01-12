package local.playground.springboot.contribution;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ContributionRepository typed JPQL projection.
 * Uses @SpringBootTest with H2 in-memory database (configured in src/test/resources/application.properties).
 * Validates the typed DTO projection (ContributionCategoryMax) works correctly with aggregation queries.
 */
@SpringBootTest
@Transactional
class ContributionRepositoryDataJpaTest {

    @Autowired
    private ContributionRepository repository;

    @Test
    @Rollback
    void testFindMaxContributionPerCategoryTyped() {
        // given - save contributions with different categories and values
        Contribution c1 = new Contribution(null, "Team A", "Engineering", 10);
        Contribution c2 = new Contribution(null, "Team B", "Engineering", 42);
        Contribution c3 = new Contribution(null, "Team C", "Marketing", 30);
        Contribution c4 = new Contribution(null, "Team D", "Marketing", 5);

        repository.saveAll(List.of(c1, c2, c3, c4));

        // when - call typed projection to get max contribution per category
        List<ContributionCategoryMax> rows = repository.findMaxContributionPerCategoryTyped();

        // then - verify results: convert to map and assert aggregation is correct
        Map<String, Integer> result = rows.stream()
                .collect(Collectors.toMap(ContributionCategoryMax::category, ContributionCategoryMax::maxValue));

        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).isEqualTo(42);
        assertThat(result.get("Marketing")).isEqualTo(30);
    }
}
