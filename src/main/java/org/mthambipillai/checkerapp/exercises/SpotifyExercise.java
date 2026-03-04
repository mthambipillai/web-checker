package org.mthambipillai.checkerapp.exercises;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.mthambipillai.checkerapp.entity.GroupTest;
import org.mthambipillai.checkerapp.entity.SingleTest;
import org.mthambipillai.checkerapp.utils.PlaywrightUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mthambipillai.checkerapp.utils.PlaywrightUtils.*;

public class SpotifyExercise extends Exercise {

    public SpotifyExercise(BrowserContext context) {
        this.context = context;
        this.tasks = List.of(
                "Le serveur est en ligne avec une page principale et un titre h1 correct (\"Artistes Spotify\").",
                "La page principale contient les noms des artistes.",
                "La page principale contient un lien par artiste vers la liste de ses chansons.",
                "Chaque page de chaque artiste contient le nom de l'artiste dans un titre h1.",
                "Chaque page de chaque artiste contient le titre et le genre des chansons de l'artiste.",
                "Chaque page de chaque artiste contient un lien par chanson vers les paroles.",
                "Chaque page de chaque artiste contient un lien qui retourne vers la page principale.",
                "Chaque page de chaque chanson contient les paroles.",
                "Chaque page de chaque chanson contient un lien qui retourne vers la page de l'artiste.",
                "Le serveur retourne 404 et pas 500 si un identifiant inexistant est passé en argument.");
    }
    @Override
    public GroupTest getTests(Page page) {
        return new GroupTest("all", List.of(
                testMainPage(page),
                testArtistPages(page),
                testSongPage(page),
                new SingleTest("Fake artist ID returns 404", () -> fakeArtistLinkReturns404(page))
        ));
    }

    private boolean hasMainTitle(Page page, String title) {
        Locator h1s = page.locator("h1");
        if (h1s.count() != 1) {
            return false;
        }
        String text = h1s.first().innerText();
        return text.contains(title);
    }

    private boolean fakeArtistLinkReturns404(Page mainPage) {
        try {
            String firstLink = mainPage.locator(String.format("a[href*='%s']",  "5wD0owYApRtYmjPWavWKvb")).first().getAttribute("href");
            if (firstLink == null) {
                return false;
            }
            String newLink = firstLink.replace("5wD0owYApRtYmjPWavWKvb", "UNARTISTEQUINEXISTEPAS");
            return hasExpectedStatus(context, joinUrl(mainPage.url(), newLink), 404);
        } catch (Exception e) {
            return false;
        }
    }

    private List<Page> getArtistPages(Page mainPage) {
        return Stream.of(
                gotoPageIfLinkContains(context, mainPage, "3oDbviiivRWhXwIE8hxkVV"),
                gotoPageIfLinkContains(context, mainPage, "5wD0owYApRtYmjPWavWKvb"),
                gotoPageIfLinkContains(context, mainPage, "2xrB9HuIJ8XYHFpgNPg1wy")
        ).filter(Optional::isPresent).map(Optional::get).toList();
    }

    private boolean hasAllLinks(Page page, int nbExpectedLinks) {
        return page.locator("a").count() == nbExpectedLinks;
    }

    private boolean hasSomeKnownArtists(Page page) {
        String text = page.locator("body").innerText();
        return text.contains("Loyle Carner") && text.contains("DJ Shah") && text.contains("The Beach Boys");
        /**return page.evaluate("""
        () => {
          const text = document.body.textContent;
          return text.includes("string1") &&
                 text.includes("string2") &&
                 text.includes("string3");
        }
        """); 
    }

    private GroupTest testMainPage(Page page) {
        return new GroupTest("Main Page", List.of(
                new SingleTest("main page has correct title", () -> hasMainTitle(page, "Artistes Spotify")),
                new SingleTest("main page has 3 expected artist names", () -> hasSomeKnownArtists(page)),
                new SingleTest("main page has 36080 links", () -> hasAllLinks(page, 36080))
        ));
    }

    private GroupTest testArtistPages(Page homePage) {
        List<Page> artistPages = getArtistPages(homePage);
        return new GroupTest("Artist Pages", List.of(
                new SingleTest("artist name is in the page title", () -> artistPages.size() == 3 && testArtistTitles(artistPages)),
                new SingleTest("each artist page contains song names", () -> artistPages.size() == 3 && testArtistSongList(artistPages)),
                new SingleTest("each artist page contains song links", () -> artistPages.size() == 3 && testArtistSongLinks(artistPages)),
                new SingleTest("each artist page contains link back to home page", () -> artistPages.size() == 3 && artistPages
                        .stream()
                        .allMatch(p -> hasLinkBackToHomePage(p)))
        ), () -> artistPages.forEach(Page::close));
    }

    private boolean testArtistTitles(List<Page> artistPages) {
        return hasMainTitle(artistPages.get(0), "The Beach Boys") &&
                hasMainTitle(artistPages.get(1), "Giant Rooks") &&
                hasMainTitle(artistPages.get(2), "Sanction");
    }

    private boolean testArtistSongList(List<Page> artistPages) {
        List<String> pageTexts = artistPages.stream()
                .map(page -> page.locator("body").innerText())
                .toList();
        return pageTexts.get(0).contains("Barbara Ann") && pageTexts.get(0).contains("Forever") &&
                pageTexts.get(1).contains("New Estate") && pageTexts.get(1).contains("Rock") &&
                pageTexts.get(2).contains("The Prophet Who Saw Fire") && pageTexts.get(2).contains("Rock");
    }

    private boolean testArtistSongLinks(List<Page> artistPages) {
        return hasAllLinks(artistPages.get(0), 508 + 1) &&
                hasAllLinks(artistPages.get(1), 3 + 1) &&
                hasAllLinks(artistPages.get(2), 3 + 1);
    }

    private boolean hasLinkBackToHomePage(Page page) {
        return page.locator("a[href='/']").count() == 1;
    }

    private boolean hasLinkBackToArtistPage(Page page, String artistId) {
        return page.locator(String.format("a[href*='%s']", artistId)).count() == 1;
    }

    private GroupTest testSongPage(Page homePage) {
        Optional<Page> artistPage = gotoPageIfLinkContains(context, homePage, "3oDbviiivRWhXwIE8hxkVV");
        Optional<Page> songPage = artistPage.flatMap(p -> gotoPageIfLinkContains(context, p, "1wEHSOXRBj1wa5O7WXU3B8"));

        return new GroupTest("Song Page", List.of(
                new SingleTest("song page contains lyrics", () -> songPage.isPresent() &&
                        songPage.get().locator("body").innerText().contains("I heard voices in my head")),
                new SingleTest("song page contains link back to artist page", () -> songPage.isPresent() &&
                        hasLinkBackToArtistPage(songPage.get(), "3oDbviiivRWhXwIE8hxkVV"))
        ), () -> {
            artistPage.ifPresent(Page::close);
            songPage.ifPresent(Page::close);
        });
    }
}
