package com.myapp.lexicon.helpers;

import java.util.BitSet;
import java.util.Random;

/**
 * Created by Ренат on 08.08.2016.
 */
public class RandomNumberGenerator
{
    private final BitSet input;
    private final Random rnd;
    private final int Count;
    private int genCount=0;
    private int start = 0;
    public RandomNumberGenerator(int in, int seed)
    {
        Count=in;
        rnd=new Random(in);
        rnd.setSeed(seed);
        input = new BitSet(in);
    }

    public RandomNumberGenerator(int start, int in, int seed)
    {
        this.start = start;
        Count=in;
        rnd=new Random(in);
        rnd.setSeed(seed);
        input = new BitSet(in);
    }

    public int generate()
    {
        if (genCount>=Count)
            return -1;

        int next;
        do
        {
            next = rnd.nextInt(Count);
        }
        while (input.get(next));
        input.set(next);
        genCount++;
        return next + start;
    }
}
