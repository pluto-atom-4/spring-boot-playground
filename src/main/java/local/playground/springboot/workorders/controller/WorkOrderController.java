package local.playground.springboot.workorders.controller;

import local.playground.springboot.workorders.service.WorkOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/work-orders")
public class WorkOrderController {

    private final WorkOrderService service;

    public WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Long>> getStatus() {
        return ResponseEntity.ok(service.getStatusCounts());
    }
}
