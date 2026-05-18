package com.behsa.medportal.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class VersionController {
    private static final String version = "2.0.0-SNAPSHOT-mediation-";



    @GetMapping("/version/info")
    public Map<String, String> buildInfo() {
        return Map.of(
            "version", version,
            "fullVersion", version + "_" + LocalDateTime.now()
        );
    }


    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok().body(version+"_"+LocalDateTime.now());
    }
}
