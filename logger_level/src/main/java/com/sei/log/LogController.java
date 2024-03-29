package com.sei.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LogController {
    @GetMapping("/log")
    public ResponseEntity<Void> log() {
        log.debug("debug");
        return ResponseEntity.ok().build();
    }
}
