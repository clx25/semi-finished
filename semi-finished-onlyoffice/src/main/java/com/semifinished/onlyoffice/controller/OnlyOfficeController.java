package com.semifinished.onlyoffice.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.semifinished.onlyoffice.config.OnlyOfficeProperties;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/onlyoffice")
@AllArgsConstructor
public class OnlyOfficeController {

    private final OnlyOfficeProperties onlyOfficeProperties;

    @PostMapping("/token")
    public String getToken(@RequestBody Map<String, Object> params) {
        Algorithm algorithm = Algorithm.HMAC256(onlyOfficeProperties.getSecretKey());

        return JWT.create()
                .withIssuedAt(new Date())
                .withPayload(params)
                .sign(algorithm);
    }

    @PostMapping("/save")
    public String saveDocument(@RequestParam String documentId, @RequestBody String content) {
        return "Document " + documentId + " saved successfully";
    }
}
