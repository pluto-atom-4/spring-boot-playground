package local.playground.springboot.contribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    @Query("SELECT c.category AS category, MAX(c.value) AS maxValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findMaxContributionPerCategory();

    // Typed JPQL projection using a record constructor expression
    @Query("SELECT new local.playground.springboot.contribution.ContributionCategoryMax(c.category, MAX(c.value)) " +
           "FROM Contribution c GROUP BY c.category")
    List<ContributionCategoryMax> findMaxContributionPerCategoryTyped();

    // Native query returning tuples: [category, maxValue]
    @Query(value = "SELECT \"category\", MAX(\"value\") as max_value FROM \"team_contributions\" GROUP BY \"category\"", nativeQuery = true)
    List<Object[]> findMaxContributionPerCategoryNative();

    // Additional aggregation queries
    @Query("SELECT c.category AS category, AVG(c.value) AS avgValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findAvgContributionPerCategory();

    @Query("SELECT c.category AS category, SUM(c.value) AS totalValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findTotalContributionPerCategory();

    @Query("SELECT c.category AS category, COUNT(c) AS countValue FROM Contribution c GROUP BY c.category")
    List<Map<String, Object>> findCountPerCategory();
}
