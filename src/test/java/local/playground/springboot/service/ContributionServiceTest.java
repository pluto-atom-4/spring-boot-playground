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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {

    @Mock
    private ContributionRepository repository;

    @InjectMocks
    private ContributionServiceImpl service;

    @Test
    void testGetMaxContributionPerCategory() {
        // Arrange: mock SQL-like results
        Map<String, Object> row1 = new HashMap<>();
        row1.put("category", "Engineering");
        row1.put("maxValue", 42);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("category", "Marketing");
        row2.put("maxValue", 30);

        List<Map<String, Object>> mockResults = List.of(row1, row2);

        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(mockResults);

        // Act
        Map<String, Integer> result = service.getMaxContributionPerCategory();

        // Assert
        assertEquals(2, result.size());
        assertEquals(42, result.get("Engineering"));
        assertEquals(30, result.get("Marketing"));
    }
}

