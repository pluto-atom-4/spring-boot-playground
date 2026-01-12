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

/**
 * Edge case unit tests for ContributionService null-handling and data validation.
 * Tests cover: empty results, null category, null maxValue, non-numeric values, duplicates.
 */
@ExtendWith(MockitoExtension.class)
class ContributionServiceEdgeCasesTest {

    @Mock
    private ContributionRepository repository;

    @InjectMocks
    private ContributionServiceImpl service;

    @Test
    void testEmptyRepositoryResult() {
        // given: repository returns empty list
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(List.of());

        // when: call default (SKIP_NULLS)
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: result is empty map
        assertThat(result).isEmpty();
        assertThat(result).isNotNull();
    }

    @Test
    void testNullCategorySkipNulls() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", null);
        row2.put("maxValue", 30);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("category", "Marketing");
        row3.put("maxValue", 25);

        List<Map<String, Object>> mockResults = List.of(row1, row2, row3);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when: call with default SKIP_NULLS
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: skip the null category row, keep valid ones
        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).isEqualTo(50);
        assertThat(result.get("Marketing")).isEqualTo(25);
        assertThat(result.get(null)).isNull();
    }

    @Test
    void testNullCategoryThrowOnNull() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", null);
        row2.put("maxValue", 30);

        List<Map<String, Object>> mockResults = List.of(row1, row2);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when/then: throw exception on null category
        InvalidRepositoryDataException exception = assertThrows(
                InvalidRepositoryDataException.class,
                () -> service.getMaxContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL)
        );
        assertThat(exception.getMessage()).contains("Null category or maxValue encountered");
    }

    @Test
    void testNullMaxValueSkipNulls() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "Marketing");
        row2.put("maxValue", null);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("category", "Sales");
        row3.put("maxValue", 40);

        List<Map<String, Object>> mockResults = List.of(row1, row2, row3);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when: call with default SKIP_NULLS
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: skip rows with null maxValue
        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).isEqualTo(50);
        assertThat(result.get("Sales")).isEqualTo(40);
        assertThat(result).doesNotContainKey("Marketing");
    }

    @Test
    void testNullMaxValueThrowOnNull() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "Marketing");
        row2.put("maxValue", null);

        List<Map<String, Object>> mockResults = List.of(row1, row2);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when/then: throw exception on null maxValue
        InvalidRepositoryDataException exception = assertThrows(
                InvalidRepositoryDataException.class,
                () -> service.getMaxContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL)
        );
        assertThat(exception.getMessage()).contains("Null category or maxValue encountered");
    }

    @Test
    void testNonNumericMaxValueSkipNulls() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "Marketing");
        row2.put("maxValue", "not_a_number");

        Map<String, Object> row3 = new HashMap<>();
        row3.put("category", "Sales");
        row3.put("maxValue", 40);

        List<Map<String, Object>> mockResults = List.of(row1, row2, row3);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when: call with default SKIP_NULLS
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: skip rows with non-numeric maxValue
        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).isEqualTo(50);
        assertThat(result.get("Sales")).isEqualTo(40);
        assertThat(result).doesNotContainKey("Marketing");
    }

    @Test
    void testNonNumericMaxValueThrowOnNull() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "Marketing");
        row2.put("maxValue", "not_a_number");

        List<Map<String, Object>> mockResults = List.of(row1, row2);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when/then: throw exception on non-numeric maxValue
        InvalidRepositoryDataException exception = assertThrows(
                InvalidRepositoryDataException.class,
                () -> service.getMaxContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL)
        );
        assertThat(exception.getMessage()).contains("not numeric");
    }

    @Test
    void testDuplicateCategoriesLastWins() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "Engineering");  // duplicate category
        row2.put("maxValue", 75);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("category", "Marketing");
        row3.put("maxValue", 30);

        List<Map<String, Object>> mockResults = List.of(row1, row2, row3);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when: call with default SKIP_NULLS
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: last value for duplicate category wins
        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).isEqualTo(75);  // second value overwrites first
        assertThat(result.get("Marketing")).isEqualTo(30);
    }

    @Test
    void testMixedValidAndInvalidRowsSkipNulls() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 100);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", null);
        row2.put("maxValue", 50);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("category", "Marketing");
        row3.put("maxValue", null);

        Map<String, Object> row4 = new HashMap<>();
        row4.put("category", "Sales");
        row4.put("maxValue", "invalid");

        Map<String, Object> row5 = new HashMap<>();
        row5.put("category", "Operations");
        row5.put("maxValue", 60);

        List<Map<String, Object>> mockResults = List.of(row1, row2, row3, row4, row5);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when: call with default SKIP_NULLS
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: only valid rows are included
        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).isEqualTo(100);
        assertThat(result.get("Operations")).isEqualTo(60);
    }

    @Test
    void testNumericTypesConversion() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);  // Integer

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "Marketing");
        row2.put("maxValue", 30L);  // Long

        Map<String, Object> row3 = new HashMap<>();
        row3.put("category", "Sales");
        row3.put("maxValue", 40.5);  // Double

        List<Map<String, Object>> mockResults = List.of(row1, row2, row3);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when: call with default SKIP_NULLS
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: all numeric types are converted to Integer
        assertThat(result).hasSize(3);
        assertThat(result.get("Engineering")).isEqualTo(50);
        assertThat(result.get("Marketing")).isEqualTo(30);
        assertThat(result.get("Sales")).isEqualTo(40);  // 40.5 truncated to 40
    }

    @Test
    void testBothCategoryAndMaxValueNull() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", null);
        row2.put("maxValue", null);

        List<Map<String, Object>> mockResults = List.of(row1, row2);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when: call with default SKIP_NULLS
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // then: skip row where both are null
        assertThat(result).hasSize(1);
        assertThat(result.get("Engineering")).isEqualTo(50);
    }

    @Test
    void testBothCategoryAndMaxValueNullThrowOnNull() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 50);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", null);
        row2.put("maxValue", null);

        List<Map<String, Object>> mockResults = List.of(row1, row2);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // when/then: throw exception
        InvalidRepositoryDataException exception = assertThrows(
                InvalidRepositoryDataException.class,
                () -> service.getMaxContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL)
        );
        assertThat(exception.getMessage()).contains("Null category or maxValue encountered");
    }
}

