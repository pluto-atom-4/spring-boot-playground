package local.playground.springboot.service;

import local.playground.springboot.contribution.ContributionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ContributionServiceNullHandlingTest {

    @Mock
    private ContributionRepository repository;

    @InjectMocks
    private ContributionServiceImpl service;

    @Test
    void testSkipNullsBehavior() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 42);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", null);
        row2.put("maxValue", 30);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("category", "Marketing");
        row3.put("maxValue", null);

        List<Map<String, Object>> mockResults = List.of(row1, row2, row3);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        Map<String, Integer> result = service.getMaxContributionPerCategory(); // default SKIP_NULLS

        assertEquals(1, result.size());
        assertEquals(42, result.get("Engineering"));
    }

    @Test
    void testThrowOnNullBehavior() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 42);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", null);
        row2.put("maxValue", 30);

        List<Map<String, Object>> mockResults = List.of(row1, row2);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        Assertions.assertThrows(InvalidRepositoryDataException.class, () ->
                service.getMaxContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
    }
}
