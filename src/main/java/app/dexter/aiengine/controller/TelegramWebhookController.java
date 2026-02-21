package app.dexter.aiengine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class TelegramWebhookController {

    @PostMapping
    public ResponseEntity<String> onUpdateReceived(@RequestBody String update) {
        System.out.println("Received update: " + update);
        return ResponseEntity.ok("OK");
    }
}