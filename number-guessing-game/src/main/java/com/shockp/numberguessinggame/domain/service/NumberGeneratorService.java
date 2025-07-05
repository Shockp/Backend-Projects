package com.shockp.numberguessinggame.domain.service;

import java.util.Random;

public class NumberGeneratorService {
    private final Random random;

    public NumberGeneratorService() {
        this.random = new Random();
    }

    public int generateNumber() {
        return random.nextInt(100) + 1;
    }
}