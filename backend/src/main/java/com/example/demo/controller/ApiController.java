package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody DeleteRequest req) {
        List<Map<String, String>> results = new ArrayList<>();
        Path root;
        try {
            root = Paths.get(allowedRoot).toRealPath();
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "allowed_root not accessible: " + allowedRoot));
        }
        if (req == null || req.paths == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paths required"));
        }
        for (String p : req.paths) {
            Map<String, String> r = new LinkedHashMap<>();
            r.put("path", p);
            try {
                if (p == null || p.isBlank()) {
                    r.put("status", "rejected");
                    r.put("reason", "empty path");
                    results.add(r);
                    continue;
                }
                Path target = root.resolve(p).normalize();
                if (!target.startsWith(root)) {
                    r.put("status", "rejected");
                    r.put("reason", "outside allowed_root");
                    results.add(r);
                    continue;
                }
                if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    r.put("status", "rejected");
                    r.put("reason", "not found");
                    results.add(r);
                    continue;
                }
                if (Files.isSymbolicLink(target)) {
                    r.put("status", "rejected");
                    r.put("reason", "symlink refused");
                    results.add(r);
                    continue;
                }
                Path real = target.toRealPath();
                if (!real.startsWith(root)) {
                    r.put("status", "rejected");
                    r.put("reason", "resolves outside allowed_root");
                    results.add(r);
                    continue;
                }
                if (!Files.isRegularFile(real, LinkOption.NOFOLLOW_LINKS)) {
                    r.put("status", "rejected");
                    r.put("reason", "not a regular file");
                    results.add(r);
                    continue;
                }
                Files.delete(real);
                r.put("status", "deleted");
            } catch (Exception e) {
                r.put("status", "error");
                r.put("reason", e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            results.add(r);
        }
        return ResponseEntity.ok(Map.of("allowedRoot", root.toString(), "results", results));
    }

    public static class DeleteRequest {
        public List<String> paths;
    }
}
