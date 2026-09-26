package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.BatchGenealogyDtoFactory;
import com.generated.qualityTrace.exceptions.GenealogyRejectionException;
import com.generated.qualityTrace.routes.BatchGenealogyRoutes;
import com.generated.qualityTrace.services.BatchGenealogyService;
import com.generated.qualityTrace.types.BatchGenealogyPayload;
import com.generated.qualityTrace.types.BatchGenealogyResult;
import com.generated.qualityTrace.utils.Formatters;

@RestController
@RequestMapping(BatchGenealogyRoutes.PATH)
public class BatchGenealogyController {
  private static final Logger log = LoggerFactory.getLogger(BatchGenealogyController.class);

  private final BatchGenealogyService service;

  public BatchGenealogyController(BatchGenealogyService service) { this.service = service; }

  @PostMapping("/split")
  public ResponseEntity<Map<String, Object>> split(
      @RequestBody(required = false) BatchGenealogyPayload.SplitRequest req) {
    try {
      BatchGenealogyResult r = service.registerSplit(req);
      return ResponseEntity.status(r.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(r.body());
    } catch (GenealogyRejectionException e) {
      return reject(req == null ? null : req.docNo(), e);
    }
  }

  @PostMapping("/merge")
  public ResponseEntity<Map<String, Object>> merge(
      @RequestBody(required = false) BatchGenealogyPayload.MergeRequest req) {
    try {
      BatchGenealogyResult r = service.registerMerge(req);
      return ResponseEntity.status(r.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(r.body());
    } catch (GenealogyRejectionException e) {
      return reject(req == null ? null : req.docNo(), e);
    }
  }

  @GetMapping("/events")
  public List<Map<String, Object>> events() { return service.listEvents(); }

  @GetMapping("/links")
  public List<Map<String, Object>> links() { return service.listLinks(); }

  @GetMapping("/trace/{batchNo}")
  public Map<String, Object> trace(@PathVariable String batchNo) { return service.trace(batchNo); }

  private ResponseEntity<Map<String, Object>> reject(String docNo, GenealogyRejectionException e) {
    log.warn(String.format(LogTemplates.GENEALOGY_REJECT,
        Formatters.docRef(docNo), e.getErrorCode(), e.getMessage()));
    return ResponseEntity.unprocessableEntity()
        .body(BatchGenealogyDtoFactory.error(e.getErrorCode(), e.getMessage()));
  }
}
