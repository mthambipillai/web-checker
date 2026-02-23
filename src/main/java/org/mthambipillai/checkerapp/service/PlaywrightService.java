package org.mthambipillai.checkerapp.service;

import org.mthambipillai.checkerapp.config.ApplicationProperties;
import org.mthambipillai.checkerapp.entity.GroupTest;
import org.mthambipillai.checkerapp.exercises.ClassListsExercise;
import org.mthambipillai.checkerapp.exercises.Exercise;
import org.mthambipillai.checkerapp.exercises.SpotifyExercise;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.*;

import java.util.*;

import static org.mthambipillai.checkerapp.utils.PlaywrightUtils.gotoPage;

@Service
public class PlaywrightService {
    private final ApplicationProperties applicationProperties;
    private final BrowserContext context;

    private final Map<String, Exercise> exercises = new HashMap<>();

    public PlaywrightService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        context = browser.newContext();
        exercises.put("listes-de-classes", new ClassListsExercise(context));
        exercises.put("spotify", new SpotifyExercise(context));
    }

    public List<String> getCurrentTasks() {
        return exercises.get(applicationProperties.getExerciseName()).getTasks();
    }

    public int runTests(String ip, int port) {
        String url = "http://" + ip + ":" + port;
        System.out.println("Navigating to " + url + "...");
        int total = 0;
        Page page = gotoPage(context, url);
        if (page != null) {
            System.out.println("Starting tests on " + url + "...");
            Exercise exercise = exercises.get(applicationProperties.getExerciseName());
            GroupTest allTests = exercise.getTests(page);
            total = allTests.isSuccessful().getScore();
            page.close();
            System.out.println("Finished tests on " + url + ".");
        }
        return total;
    }
}
