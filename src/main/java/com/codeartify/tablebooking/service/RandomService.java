package com.codeartify.tablebooking.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomService {
    private final Random random = new Random();

    public int nextRand(int size) {
        return random.nextInt(size);
    }
}
