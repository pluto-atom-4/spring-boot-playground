package local.playground.springboot.service;

import local.playground.springboot.contribution.ContributionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ContributionServiceAggregateMetricsTest {

    @Mock
    private ContributionRepository repository;

    @InjectMocks
    private ContributionServiceImpl service;

    @Test
    void testAverageEmptyResult() {
        Mockito.when(repository.findAvgContributionPerCategory()).thenReturn(List.of());

        Map<String, Double> result = service.getAverageContributionPerCategory();

        assertThat(result).isEmpty();
    }

    @Test
    void testAverageSkipNullsAndNonNumeric() {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("category", "Eng"); r1.put("avgValue", 12.5);
        Map<String, Object> r2 = new HashMap<>();
        r2.put("category", "Mkt"); r2.put("avgValue", null);
        Map<String, Object> r3 = new HashMap<>();
        r3.put("category", "Ops"); r3.put("avgValue", "bad");

        Mockito.when(repository.findAvgContributionPerCategory()).thenReturn(List.of(r1, r2, r3));

        Map<String, Double> result = service.getAverageContributionPerCategory();

        assertThat(result).hasSize(1);
        assertThat(result.get("Eng")).isEqualTo(12.5);
    }

    @Test
    void testAverageThrowOnNull() {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("category", "Eng"); r1.put("avgValue", 12.5);
        Map<String, Object> r2 = new HashMap<>();
        r2.put("category", "Mkt"); r2.put("avgValue", null);

        Mockito.when(repository.findAvgContributionPerCategory()).thenReturn(List.of(r1, r2));

        assertThrows(InvalidRepositoryDataException.class, () ->
                service.getAverageContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
    }

    @Test
    void testTotalEmptyResult() {
        Mockito.when(repository.findTotalContributionPerCategory()).thenReturn(List.of());

        Map<String, Long> result = service.getTotalContributionPerCategory();

        assertThat(result).isEmpty();
    }

    @Test
    void testTotalSkipNullsAndNonNumeric() {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("category", "Eng"); r1.put("totalValue", 100L);
        Map<String, Object> r2 = new HashMap<>();
        r2.put("category", "Mkt"); r2.put("totalValue", null);
        Map<String, Object> r3 = new HashMap<>();
        r3.put("category", "Ops"); r3.put("totalValue", "bad");

        Mockito.when(repository.findTotalContributionPerCategory()).thenReturn(List.of(r1, r2, r3));

        Map<String, Long> result = service.getTotalContributionPerCategory();

        assertThat(result).hasSize(1);
        assertThat(result.get("Eng")).isEqualTo(100L);
    }

    @Test
    void testTotalThrowOnNull() {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("category", "Eng"); r1.put("totalValue", 100L);
        Map<String, Object> r2 = new HashMap<>();
        r2.put("category", "Mkt"); r2.put("totalValue", null);

        Mockito.when(repository.findTotalContributionPerCategory()).thenReturn(List.of(r1, r2));

        assertThrows(InvalidRepositoryDataException.class, () ->
                service.getTotalContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
    }

    @Test
    void testCountEmptyResult() {
        Mockito.when(repository.findCountPerCategory()).thenReturn(List.of());

        Map<String, Long> result = service.getCountPerCategory();

        assertThat(result).isEmpty();
    }

    @Test
    void testCountSkipNullsAndNonNumeric() {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("category", "Eng"); r1.put("countValue", 5L);
        Map<String, Object> r2 = new HashMap<>();
        r2.put("category", "Mkt"); r2.put("countValue", null);
        Map<String, Object> r3 = new HashMap<>();
        r3.put("category", "Ops"); r3.put("countValue", "bad");

        Mockito.when(repository.findCountPerCategory()).thenReturn(List.of(r1, r2, r3));

        Map<String, Long> result = service.getCountPerCategory();

        assertThat(result).hasSize(1);
        assertThat(result.get("Eng")).isEqualTo(5L);
    }

    @Test
    void testCountThrowOnNull() {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("category", "Eng"); r1.put("countValue", 5L);
        Map<String, Object> r2 = new HashMap<>();
        r2.put("category", "Mkt"); r2.put("countValue", null);

        Mockito.when(repository.findCountPerCategory()).thenReturn(List.of(r1, r2));

        assertThrows(InvalidRepositoryDataException.class, () ->
                service.getCountPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
    }
}

