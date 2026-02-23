package org.mthambipillai.checkerapp.exercises;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.mthambipillai.checkerapp.entity.GroupTest;
import org.mthambipillai.checkerapp.entity.SingleTest;
import org.mthambipillai.checkerapp.utils.PlaywrightUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mthambipillai.checkerapp.utils.PlaywrightUtils.*;

public class ClassListsExercise extends Exercise {
    public  ClassListsExercise(BrowserContext context) {
        this.context = context;
        this.tasks = List.of(
                "Le serveur est en ligne avec une page principale et un titre h1 correct (\"Groupes d'OC informatique\").",
                "La page principale contient les trois liens corrects.",
                "Chaque page de chaque groupe contient les mots \"Prénom\", \"Nom\" et \"Email\".",
                "Chaque page contient les adresses emails des élèves du groupe.",
                "Chaque page contient un lien qui retourne vers la page principale.",
                "Le serveur retourne 404 et pas 500 si un groupe inexistant est passé en argument.");
    }
    @Override
    public GroupTest getTests(Page page) {
        return new GroupTest("all", List.of(
                testMainPage(page),
                testGroupPages(getSubPages(context, page)),
                new SingleTest("Fake group link returns 404", () -> fakeGroupLinkReturns404(page))
        ));
    }

    private boolean hasMainTitle(Page page) {
        Locator h1s = page.locator("h1");
        if (h1s.count() != 1) {
            return false;
        }
        String text = h1s.first().innerText();
        return text.contains("OC informatique");
    }

    private boolean fakeGroupLinkReturns404(Page mainPage) {
        try {
            String firstLink = mainPage.locator("a").first().getAttribute("href");
            if (firstLink == null) {
                return false;
            }
            String newLink = firstLink.replace("3OCIN1", "UNECLASSEQUINEXISTEPAS");
            Page page = context.newPage();
            Response response = page.navigate(joinUrl(mainPage.url(), newLink));
            int status = response.status();
            page.close();
            return status == 404;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasAllLinks(Page page) {
        List<String> links = new ArrayList<>();
        for (Locator loc : page.locator("a").all()) {
            String href = loc.getAttribute("href");
            if (href != null)
                links.add(href);
        }
        if (links.size() != 3) {
            return false;
        }

        Pattern pattern = Pattern.compile("3OCIN\\d");
        Set<String> groups = new HashSet<>();
        for (String link : links) {
            Matcher m = pattern.matcher(link);
            if (m.find()) {
                groups.add(m.group());
            }
        }
        return groups.contains("3OCIN1") && groups.contains("3OCIN2") && groups.contains("3OCIN3");
    }

    private GroupTest testMainPage(Page page) {
        return new GroupTest("Main Page", List.of(
                new SingleTest("main page has title", () -> hasMainTitle(page)),
                new SingleTest("main page has 3 links", () -> hasAllLinks(page))
        ));
    }

    private GroupTest testGroupPages(List<Page> pages) {
        List<String> pageTexts = pages.stream()
                .map(PlaywrightUtils::getPageFullText)
                .toList();
        return new GroupTest("Group Pages", List.of(
                new SingleTest("all group pages have correct text headers", () -> pageTexts.size() == 3 && pageTexts.stream().allMatch(this::testGroupHeaders)),
                new SingleTest("all group pages contain @eduvaud email addresses", () -> pageTexts.size() == 3 && pageTexts.stream().allMatch(this::testEmails)),
                new SingleTest("all group pages have a link back to the home page", () -> pages.size() == 3 && pages.stream().allMatch(this::testLinkBackToHomePage))
        ), () -> pages.forEach(Page::close));
    }

    private boolean testGroupHeaders(String pageFullText) {
        return pageFullText.contains("Prénom") &&
                pageFullText.contains("Nom") &&
                pageFullText.contains("Email");
    }

    private boolean testEmails(String pageFullText) {
        return pageFullText.contains("@eduvaud");
    }

    private boolean testLinkBackToHomePage(Page page) {
        List<String> links = new ArrayList<>();
        for (Locator loc : page.locator("a").all()) {
            String href = loc.getAttribute("href");
            if (href != null) {
                links.add(href);
            }
        }
        if (links.size() != 1) {
            return false;
        }
        Page homepage = context.newPage();
        homepage.navigate(joinUrl(page.url(), links.get(0)));
        boolean result = hasMainTitle(homepage);
        homepage.close();
        return result;
    }
}
