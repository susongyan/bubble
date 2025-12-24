package com.zuomagai.bubble.web;

import com.zuomagai.bubble.service.SegmentIdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ids")
public class IdController {

    private final SegmentIdService segmentIdService;

    public IdController(SegmentIdService segmentIdService) {
        this.segmentIdService = segmentIdService;
    }

    @GetMapping("/{bizTag}")
    public ResponseEntity<Map<String, Object>> nextId(@PathVariable String bizTag) {
        long id = segmentIdService.nextId(bizTag);
        Map<String, Object> body = new HashMap<>();
        body.put("bizTag", bizTag);
        body.put("id", id);
        return ResponseEntity.ok(body);
    }
}
