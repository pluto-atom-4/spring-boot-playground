package local.playground.springboot.service;

import local.playground.springboot.contribution.ContributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ContributionServiceRandomizedPropertyTest {

    @Mock
    private ContributionRepository repository;

    @InjectMocks
    private ContributionServiceImpl service;

    private final Random rnd = new Random(12345);

    // Utility to create a random row with possible nulls and non-numeric values
    private Map<String, Object> randomRow() {
        Map<String, Object> row = new HashMap<>();
        // category: 80% chance non-null string, 20% null
        if (rnd.nextDouble() < 0.8) {
            row.put("category", randomCategory());
        } else {
            row.put("category", null);
        }
        // value: choose among Integer, Long, Double, null, or a non-numeric String
        double p = rnd.nextDouble();
        if (p < 0.6) {
            // numeric
            int choice = rnd.nextInt(3);
            if (choice == 0) row.put("maxValue", rnd.nextInt(200));
            else if (choice == 1) row.put("maxValue", (long) rnd.nextInt(200));
            else row.put("maxValue", rnd.nextDouble() * 200);
        } else if (p < 0.75) {
            row.put("maxValue", null);
        } else {
            row.put("maxValue", "bad" + rnd.nextInt(1000));
        }
        return row;
    }

    private String randomCategory() {
        int len = 1 + rnd.nextInt(10);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = (char) ('A' + rnd.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
    }

    private Map<String, Integer> expectedMaxMap(List<Map<String, Object>> rows) {
        Map<String, Integer> expected = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object catObj = row.get("category");
            Object maxObj = row.get("maxValue");
            if (catObj == null || maxObj == null) continue;
            if (!(maxObj instanceof Number)) continue;
            String cat = (String) catObj;
            int val = ((Number) maxObj).intValue();
            expected.merge(cat, val, Math::max);
        }
        return expected;
    }

    @Test
    void quickSanity() {
        // small deterministic sanity check
        Map<String, Object> r1 = new HashMap<>(); r1.put("category", "ENG"); r1.put("maxValue", 10);
        Map<String, Object> r2 = new HashMap<>(); r2.put("category", "ENG"); r2.put("maxValue", 20L);
        Map<String, Object> r3 = new HashMap<>(); r3.put("category", null); r3.put("maxValue", 100);
        List<Map<String,Object>> rows = List.of(r1,r2,r3);
        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(rows);
        Map<String,Integer> out = service.getMaxContributionPerCategory();
        assertThat(out).containsEntry("ENG", 20);
    }

    @RepeatedTest(50)
    void randomizedPropertyTestMaxBehavior() {
        // generate random list
        int size = rnd.nextInt(20);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < size; i++) rows.add(randomRow());

        Mockito.when(repository.findMaxContributionPerCategory()).thenReturn(rows);

        // SKIP_NULLS expected
        Map<String, Integer> expected = expectedMaxMap(rows);
        Map<String, Integer> actual = service.getMaxContributionPerCategory();
        assertThat(actual).isEqualTo(expected);

        // Strict behavior: if any row has null category or null/invalid maxValue, expect exception
        boolean hasInvalid = rows.stream().anyMatch(r -> r.get("category") == null || !(r.get("maxValue") instanceof Number) || r.get("maxValue") == null);
        if (hasInvalid) {
            assertThrows(InvalidRepositoryDataException.class, () -> service.getMaxContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
        } else {
            Map<String,Integer> actualStrict = service.getMaxContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL);
            assertThat(actualStrict).isEqualTo(expected);
        }
    }

    @RepeatedTest(30)
    void randomizedPropertyTestAvgTotalCountBehavior() {
        int size = rnd.nextInt(20);
        List<Map<String,Object>> rowsAvg = new ArrayList<>();
        List<Map<String,Object>> rowsTot = new ArrayList<>();
        List<Map<String,Object>> rowsCnt = new ArrayList<>();
        // create rows with keys avgValue/totalValue/countValue for different repository methods
        for (int i=0;i<size;i++){
            Map<String,Object> a = new HashMap<>();
            a.put("category", rnd.nextDouble()<0.85?randomCategory():null);
            double p = rnd.nextDouble();
            if (p<0.6) a.put("avgValue", rnd.nextDouble()*100);
            else if (p<0.75) a.put("avgValue", null);
            else a.put("avgValue", "bad");
            rowsAvg.add(a);

            Map<String,Object> t = new HashMap<>();
            t.put("category", rnd.nextDouble()<0.85?randomCategory():null);
            double q = rnd.nextDouble();
            if (q<0.6) t.put("totalValue", (long)rnd.nextInt(200));
            else if (q<0.75) t.put("totalValue", null);
            else t.put("totalValue", "bad");
            rowsTot.add(t);

            Map<String,Object> c = new HashMap<>();
            c.put("category", rnd.nextDouble()<0.85?randomCategory():null);
            double r = rnd.nextDouble();
            if (r<0.6) c.put("countValue", (long)rnd.nextInt(50));
            else if (r<0.75) c.put("countValue", null);
            else c.put("countValue", "bad");
            rowsCnt.add(c);
        }

        // avg
        Mockito.when(repository.findAvgContributionPerCategory()).thenReturn(rowsAvg);
        Map<String,Double> expectedAvg = new HashMap<>();
        for (Map<String,Object> row: rowsAvg){
            Object cat=row.get("category"); Object v=row.get("avgValue");
            if (cat==null||v==null||!(v instanceof Number)) continue;
            String s=(String)cat; double val=((Number)v).doubleValue();
            expectedAvg.merge(s, val, Math::max);
        }
        Map<String,Double> actualAvg = service.getAverageContributionPerCategory();
        assertThat(actualAvg).isEqualTo(expectedAvg);

        boolean hasInvalidAvg = rowsAvg.stream().anyMatch(r -> r.get("category")==null || r.get("avgValue")==null || !(r.get("avgValue") instanceof Number));
        if (hasInvalidAvg) assertThrows(InvalidRepositoryDataException.class, () -> service.getAverageContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
        else assertThat(service.getAverageContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL)).isEqualTo(expectedAvg);

        // total
        Mockito.when(repository.findTotalContributionPerCategory()).thenReturn(rowsTot);
        Map<String,Long> expectedTot = new HashMap<>();
        for (Map<String,Object> row: rowsTot){
            Object cat=row.get("category"); Object v=row.get("totalValue");
            if (cat==null||v==null||!(v instanceof Number)) continue;
            String s=(String)cat; long val=((Number)v).longValue();
            expectedTot.merge(s, val, Long::sum);
        }
        Map<String,Long> actualTot = service.getTotalContributionPerCategory();
        assertThat(actualTot).isEqualTo(expectedTot);

        boolean hasInvalidTot = rowsTot.stream().anyMatch(r -> r.get("category")==null || r.get("totalValue")==null || !(r.get("totalValue") instanceof Number));
        if (hasInvalidTot) assertThrows(InvalidRepositoryDataException.class, () -> service.getTotalContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
        else assertThat(service.getTotalContributionPerCategory(ContributionService.NullHandling.THROW_ON_NULL)).isEqualTo(expectedTot);

        // count
        Mockito.when(repository.findCountPerCategory()).thenReturn(rowsCnt);
        Map<String,Long> expectedCnt = new HashMap<>();
        for (Map<String,Object> row: rowsCnt){
            Object cat=row.get("category"); Object v=row.get("countValue");
            if (cat==null||v==null||!(v instanceof Number)) continue;
            String s=(String)cat; long val=((Number)v).longValue();
            expectedCnt.merge(s, val, Long::sum);
        }
        Map<String,Long> actualCnt = service.getCountPerCategory();
        assertThat(actualCnt).isEqualTo(expectedCnt);

        boolean hasInvalidCnt = rowsCnt.stream().anyMatch(r -> r.get("category")==null || r.get("countValue")==null || !(r.get("countValue") instanceof Number));
        if (hasInvalidCnt) assertThrows(InvalidRepositoryDataException.class, () -> service.getCountPerCategory(ContributionService.NullHandling.THROW_ON_NULL));
        else assertThat(service.getCountPerCategory(ContributionService.NullHandling.THROW_ON_NULL)).isEqualTo(expectedCnt);
    }
}
