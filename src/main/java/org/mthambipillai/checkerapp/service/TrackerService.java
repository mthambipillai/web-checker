package org.mthambipillai.checkerapp.service;

import lombok.RequiredArgsConstructor;
import org.mthambipillai.checkerapp.config.ApplicationProperties;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TrackerService {
    private final ApplicationProperties applicationProperties;
    private final NetworkScanService networkScanService;
    private final PlaywrightService playwrightService;

    private static final int EXPECTED_PORT = 8081;

    public List<String> getTasks() {
        return playwrightService.getCurrentTasks();
    }

    private final Map<String, Integer> progressions = new HashMap<>();

    public Map<String, Integer> getProgressions() {
        List<String> onlineIps = List.of("0.0.0.0"); // networkScanService.getParticipantsFromSubnet(applicationProperties.getSubnet(), EXPECTED_PORT);
        for  (String ip : onlineIps) {
            int score = playwrightService.runTests(ip, EXPECTED_PORT);
            if (!progressions.containsKey(ip)) {
                progressions.put(ip, score);
            } else {
                progressions.put(ip, Math.max(progressions.get(ip), score));
            }
        }
        return progressions;
    }
}
