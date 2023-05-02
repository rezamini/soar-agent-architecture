package com.soar.agent.architecture.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.soar.agent.architecture.api.entity.SemanticMemoryEntity;
import com.soar.agent.architecture.api.service.SemanticMemoryService;

@RestController
@CrossOrigin
public class SemanticMemoryController {
    @Autowired
    SemanticMemoryService semanticMemoryService;

    @GetMapping(value = "/smemAttributes")
    public ResponseEntity<Map<String, List<String>>> getSemanticMemoryAttributes(){
        return ResponseEntity.ok(semanticMemoryService.getSemanticMemoryAttributes());
    }

    @PostMapping(value = "smemAttributes")
    public ResponseEntity<SemanticMemoryEntity> saveSemanticMemoryAttributes(@RequestBody SemanticMemoryEntity semanticMemoryEntity){
        return ResponseEntity.ok(semanticMemoryEntity);
    }

}
