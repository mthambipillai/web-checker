package org.mthambipillai.checkerapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestResult {
    private final boolean success;
    private final int score;
}
