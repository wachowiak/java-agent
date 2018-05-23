package org.wachowiak.agentapp.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
class HelloResource {

    @GetMapping("/first")
    String getFirst(){
        return "first";
    }
}
