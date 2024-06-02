package com.example.sarithmetics;

import java.util.Random;

public class RandomHelper {
    public RandomHelper() {}

    public String generateRandom5CharString() {
        int length = 5;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) (random.nextInt(26) + 'A');
            sb.append(c);
        }
        return sb.toString();
    }

    public String generateRandom5NumberCharString() {
        int length = 5;
        String numbers = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(numbers.length());
            sb.append(numbers.charAt(randomIndex));
        }
        return sb.toString();
    }
}
