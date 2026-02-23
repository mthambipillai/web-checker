package org.mthambipillai.checkerapp.controller;

import lombok.RequiredArgsConstructor;
import org.mthambipillai.checkerapp.service.NetworkScanService;
import org.mthambipillai.checkerapp.service.PlaywrightService;
import org.mthambipillai.checkerapp.service.TrackerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class TrackerController {
    private final TrackerService trackerService;

    @GetMapping("/tracking")
    public String trackingDashboard(Model model) {
        Map<String, Integer> progressions = trackerService.getProgressions();
        model.addAttribute("tasks", trackerService.getTasks());
        model.addAttribute("progressions", progressions);
        return "tracking";
    }
}
