package com.helpdesk.backend.service;

import com.helpdesk.backend.entity.Setting;
import com.helpdesk.backend.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;

    public Setting getSettings() {
        return settingRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Setting s = Setting.builder()
                            .themeColor("light")
                            .notifications(true)
                            .autoAssign(true)
                            .build();
                    return settingRepository.save(s);
                });
    }

    public Setting updateSettings(Setting newSettings) {
        Setting current = getSettings();
        current.setThemeColor(newSettings.getThemeColor());
        current.setNotifications(newSettings.getNotifications());
        current.setAutoAssign(newSettings.getAutoAssign());
        return settingRepository.save(current);
    }
}
