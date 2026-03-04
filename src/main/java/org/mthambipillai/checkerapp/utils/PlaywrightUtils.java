package org.mthambipillai.checkerapp.utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaywrightUtils {

    private PlaywrightUtils() {
        // hide non instantiable utils class
    }

    public static Optional<Page> gotoPageIfLinkContains(BrowserContext context, Page fromPage, String linkSubstring) {
        try {
            Locator loc = fromPage.locator(String.format("a[href*='%s']",  linkSubstring));
            if (loc != null && loc.count() == 1) {
                String link = loc.getAttribute("href");
                if (link != null && link.contains(linkSubstring)) {
                    return gotoPage(context, joinUrl(fromPage.url(), link));
                }
            }
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    public static boolean hasExpectedStatus(BrowserContext context, String url, int statusCode) {
        try {
            Page page = context.newPage();
            Response response = page.navigate(url);
            int status = response.status();
            page.close();
            return status == statusCode;
        } catch (Exception e) {
            return false;
        }
    }

    public static Optional<Page> gotoPage(BrowserContext context, String url) {
        Page page = context.newPage();
        try {
            System.out.println("Navigating to URL: " + url);
            int responseStatus = page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.LOAD)
                    .setTimeout(10000)).status();
            System.out.println("got response status: " + responseStatus);
            if (responseStatus != 200) {
                System.out.println("Page " + url + " returned status " + responseStatus);
                return Optional.empty();
            }
            return Optional.of(page);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            page.close();
            return Optional.empty();
        }
    }

    public static List<String> getPageTexts(Page page) {
        List<String> texts = new ArrayList<>();
        Locator elements = page.locator("*");
        int count = elements.count();
        for (int i = 0; i < count; i++) {
            texts.add(elements.nth(i).innerText());
        }
        return texts;
    }

    public static String getPageFullText(Page page) {
        return String.join("", getPageTexts(page));
    }

    public static List<Page> getSubPages(BrowserContext context, Page page) {
        List<Page> pages = new ArrayList<>();
        for (Locator loc : page.locator("a").all()) {
            String link = loc.getAttribute("href");
            if (link != null) {
                Optional<Page> newPage = gotoPage(context, joinUrl(page.url(), link));
                newPage.ifPresent(pages::add);
            }
        }
        return pages;
    }

    public static String joinUrl(String base, String path) {
        try {
            return new URI(base).resolve(path).toString();
        } catch (Exception e) {
            return null;
        }
    }
}
