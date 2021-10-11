package net.uint32.example.validation.controller;

import net.uint32.example.validation.model.Test1Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-10-11 15:38
 */
@RestController
public class ExampleController {

    @PostMapping("/test1")
    public String test1(Test1Param param) {
        return "OK: " + param.toString();
    }

    @PostMapping("/test2")
    public String test2(Test1Param param) {
        return "OK: " + param.toString();
    }

}
