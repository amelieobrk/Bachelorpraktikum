package de.kreuzenonline.kreuzen.ping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "Ping")
public class PingController {

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/ping")
    @ApiOperation(value = "Ping endpoint", notes = "Ping endpoint to check the availability of the service")
    public String pingEndpoint() {
        return "pong";
    }
}
