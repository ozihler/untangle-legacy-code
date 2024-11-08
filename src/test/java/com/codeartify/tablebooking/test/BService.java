package com.codeartify.tablebooking.test;

public class BService {
    private int callCounter = 0;

    public String getHelloWorld() {
        if (callCounter++ == 0) {
            return "Hello World";
        }
        return "World Hello";
    }
}
