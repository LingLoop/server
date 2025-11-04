package song.lingloop.server.mvp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import song.lingloop.server.mvp.service.MvpService;

@RestController
@RequiredArgsConstructor
public class MvpController {

    private final MvpService mvpService;


    @GetMapping
    public void get(){

    }

}
