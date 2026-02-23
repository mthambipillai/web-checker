package org.mthambipillai.checkerapp.entity;

import lombok.Data;

@Data
public abstract class TestCase {
    protected String name;
    public abstract TestResult isSuccessful();
    public abstract int getTotalExpectedScore();
}
