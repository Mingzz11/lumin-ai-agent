package cn.xhm.luminaiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查
     *
     * @return "ok"
     */
    @GetMapping
    public String healthCheck() {
        return "ok";
    }

}
