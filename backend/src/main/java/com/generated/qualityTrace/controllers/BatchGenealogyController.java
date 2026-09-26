package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.routes.BatchGenealogyRoutes;
import com.generated.qualityTrace.services.BatchGenealogyService;
import com.generated.qualityTrace.types.BatchGenealogyPayload;

@RestController
@RequestMapping(BatchGenealogyRoutes.PATH)
public class BatchGenealogyController {
  private final BatchGenealogyService service;

  public BatchGenealogyController(BatchGenealogyService service) {
    this.service = service;
  }

  @PostMapping(BatchGenealogyRoutes.SPLIT)
  public Map<String, Object> split(@RequestBody(required = false) BatchGenealogyPayload payload) {
    return service.split(payload);
  }

  @PostMapping(BatchGenealogyRoutes.MERGE)
  public Map<String, Object> merge(@RequestBody(required = false) BatchGenealogyPayload payload) {
    return service.merge(payload);
  }

  @GetMapping(BatchGenealogyRoutes.TRACE)
  public Map<String, Object> trace(@PathVariable String batchNo) {
    return service.trace(batchNo);
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }
}
