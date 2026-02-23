package org.mthambipillai.checkerapp.exercises;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import lombok.Data;
import org.mthambipillai.checkerapp.entity.GroupTest;

import java.util.List;
@Data
public abstract class Exercise {
    protected List<String> tasks;
    protected BrowserContext context;
    public abstract GroupTest getTests(Page page);
}
