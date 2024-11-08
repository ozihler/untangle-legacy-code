package com.codeartify.tablebooking.test;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AServiceBetterTest {

    @Test
    void test() {
        var aService = new AService(new BService());

        String result = aService.invoke();

        assertEquals("Hello World", result);
    }

    @Test
    void test2() {
        var aService = new AService(new BService());

        aService.invoke();
        String result = aService.invoke();

        assertEquals("World Hello", result);
    }
}
