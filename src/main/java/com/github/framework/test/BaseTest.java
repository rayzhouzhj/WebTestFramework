package com.github.framework.test;

import java.util.Random;

public class BaseTest {
    protected TestLogger logger = new TestLogger();

    public String getRandomNumberString(int length)
    {
        String output = "";
        Random random = new Random();

        for(int i = 0; i < length; i++)
        {
            output = output + random.nextInt(10);
        }

        return output;
    }

    public void sleep(long millis)
    {
        try
        {
            System.out.println("[BaseTest] Wait for " + millis + " milliseconds");
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
