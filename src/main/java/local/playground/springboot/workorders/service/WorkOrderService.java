package local.playground.springboot.workorders.service;

import local.playground.springboot.workorders.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WorkOrderService {

    private final WorkOrderRepository repository;

    public WorkOrderService(WorkOrderRepository repository) {
        this.repository = repository;
    }

    public Map<String, Long> getStatusCounts() {
        long completed = repository.countByStatus("COMPLETED");
        long pending = repository.countByStatus("PENDING");

        Map<String, Long> result = new HashMap<>();
        result.put("completed", completed);
        result.put("pending", pending);

        return result;
    }
}
