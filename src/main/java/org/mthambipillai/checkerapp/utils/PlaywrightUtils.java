package org.mthambipillai.checkerapp.utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PlaywrightUtils {

    private PlaywrightUtils() {
        // hide non instantiable utils class
    }

    public static Page gotoPage(BrowserContext context, String url) {
        Page page = context.newPage();
        try {
            System.out.println("Navigating to URL: " + url);
            int responseStatus = page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.LOAD)
                    .setTimeout(10000)).status();
            System.out.println("got response status: " + responseStatus);
            if (responseStatus != 200) {
                System.out.println("Page " + url + " returned status " + responseStatus);
            }
            return page;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            page.close();
            return null;
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
                Page newPage = gotoPage(context, joinUrl(page.url(), link));
                if (newPage != null) {
                    pages.add(newPage);
                }
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
