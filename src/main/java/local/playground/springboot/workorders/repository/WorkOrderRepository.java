package local.playground.springboot.workorders.repository;

import local.playground.springboot.workorders.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    long countByStatus(String status);
}
