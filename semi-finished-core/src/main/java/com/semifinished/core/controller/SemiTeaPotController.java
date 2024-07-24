package com.semifinished.core.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SemiTeaPotController {
    @GetMapping(value = "/teapot", name = "SEMI-TEAPOT")
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public String teapot() {
        return "<h1 style='text-align:center'>418. I’m a teapot.</h1>";
    }


}
