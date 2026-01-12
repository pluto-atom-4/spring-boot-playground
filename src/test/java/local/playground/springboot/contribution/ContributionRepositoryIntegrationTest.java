package local.playground.springboot.contribution;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ContributionRepository using H2 in-memory database.
 * Tests the native SQL aggregation query that calculates max contribution per category.
 *
 * @SpringBootTest loads application.properties from src/test/resources
 * which configures H2 in-memory database and enables DDL auto-creation.
 */
@SpringBootTest
@Transactional
class ContributionRepositoryIntegrationTest {

    @Autowired
    private ContributionRepository repository;

    @Test
    void testFindMaxContributionPerCategoryNative() {
        // given: persist sample contributions with different categories and values
        Contribution c1 = new Contribution(null, "Team A", "Engineering", 10);
        Contribution c2 = new Contribution(null, "Team B", "Engineering", 42);
        Contribution c3 = new Contribution(null, "Team C", "Marketing", 30);
        Contribution c4 = new Contribution(null, "Team D", "Marketing", 5);

        repository.saveAll(List.of(c1, c2, c3, c4));

        // when: execute the native aggregation query
        List<Object[]> rows = repository.findMaxContributionPerCategoryNative();

        // then: verify the results contain max per category
        Map<String, Integer> result = rows.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> ((Number) r[1]).intValue()
                ));

        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).isEqualTo(42);
        assertThat(result.get("Marketing")).isEqualTo(30);
    }
}
