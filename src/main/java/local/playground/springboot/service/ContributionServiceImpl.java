package local.playground.springboot.service;

import local.playground.springboot.contribution.Contribution;
import local.playground.springboot.contribution.ContributionCategoryMax;
import local.playground.springboot.contribution.ContributionRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository repository;

    public ContributionServiceImpl(ContributionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Contribution save(Contribution contribution) {
        return repository.save(contribution);
    }

    @Override
    public Optional<Contribution> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Contribution> findAll() {
        return repository.findAll();
    }

    @Override
    public Map<String, Integer> getMaxContributionPerCategory() {
        return getMaxContributionPerCategory(NullHandling.SKIP_NULLS);
    }

    @Override
    public Map<String, Integer> getMaxContributionPerCategory(NullHandling handling) {
        List<Map<String, Object>> rows = repository.findMaxContributionPerCategory();

        Map<String, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object catObj = row.get("category");
            Object maxObj = row.get("maxValue");

            if (catObj == null || maxObj == null) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("Null category or maxValue encountered in repository result: " + row);
                }
                // SKIP_NULLS: continue
                continue;
            }

            String category = (String) catObj;
            if (!(maxObj instanceof Number)) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("maxValue is not numeric: " + maxObj);
                }
                continue;
            }

            Integer maxValue = ((Number) maxObj).intValue();
            result.put(category, maxValue);
        }
        return result;
    }

    @Override
    public Map<String, Integer> getMaxContributionPerCategoryFromTyped() {
        List<ContributionCategoryMax> rows = repository.findMaxContributionPerCategoryTyped();

        Map<String, Integer> result = new HashMap<>();
        for (ContributionCategoryMax r : rows) {
            if (r == null || r.category() == null || r.maxValue() == null) {
                // default behavior: skip nulls
                continue;
            }
            result.put(r.category(), r.maxValue());
        }
        return result;
    }

    @Override
    public Map<String, Double> getAverageContributionPerCategory() {
        return getAverageContributionPerCategory(NullHandling.SKIP_NULLS);
    }

    @Override
    public Map<String, Double> getAverageContributionPerCategory(NullHandling handling) {
        List<Map<String, Object>> rows = repository.findAvgContributionPerCategory();

        Map<String, Double> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object catObj = row.get("category");
            Object avgObj = row.get("avgValue");

            if (catObj == null || avgObj == null) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("Null category or avgValue encountered in repository result: " + row);
                }
                continue;
            }

            String category = (String) catObj;
            if (!(avgObj instanceof Number)) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("avgValue is not numeric: " + avgObj);
                }
                continue;
            }

            Double avgValue = ((Number) avgObj).doubleValue();
            result.put(category, avgValue);
        }
        return result;
    }

    @Override
    public Map<String, Long> getTotalContributionPerCategory() {
        return getTotalContributionPerCategory(NullHandling.SKIP_NULLS);
    }

    @Override
    public Map<String, Long> getTotalContributionPerCategory(NullHandling handling) {
        List<Map<String, Object>> rows = repository.findTotalContributionPerCategory();

        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object catObj = row.get("category");
            Object totalObj = row.get("totalValue");

            if (catObj == null || totalObj == null) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("Null category or totalValue encountered in repository result: " + row);
                }
                continue;
            }

            String category = (String) catObj;
            if (!(totalObj instanceof Number)) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("totalValue is not numeric: " + totalObj);
                }
                continue;
            }

            Long totalValue = ((Number) totalObj).longValue();
            result.put(category, totalValue);
        }
        return result;
    }

    @Override
    public Map<String, Long> getCountPerCategory() {
        return getCountPerCategory(NullHandling.SKIP_NULLS);
    }

    @Override
    public Map<String, Long> getCountPerCategory(NullHandling handling) {
        List<Map<String, Object>> rows = repository.findCountPerCategory();

        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object catObj = row.get("category");
            Object countObj = row.get("countValue");

            if (catObj == null || countObj == null) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("Null category or countValue encountered in repository result: " + row);
                }
                continue;
            }

            String category = (String) catObj;
            if (!(countObj instanceof Number)) {
                if (handling == NullHandling.THROW_ON_NULL) {
                    throw new InvalidRepositoryDataException("countValue is not numeric: " + countObj);
                }
                continue;
            }

            Long countValue = ((Number) countObj).longValue();
            result.put(category, countValue);
        }
        return result;
    }
}
