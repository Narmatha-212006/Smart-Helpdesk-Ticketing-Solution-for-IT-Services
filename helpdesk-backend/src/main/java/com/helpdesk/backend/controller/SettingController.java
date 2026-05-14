package com.helpdesk.backend.controller;

import com.helpdesk.backend.entity.Setting;
import com.helpdesk.backend.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    public ResponseEntity<Setting> getSettings() {
        return ResponseEntity.ok(settingService.getSettings());
    }

    @PutMapping
    public ResponseEntity<Setting> updateSettings(@RequestBody Setting settings) {
        return ResponseEntity.ok(settingService.updateSettings(settings));
    }
}
