package com.myapp.lexicon;

import java.util.BitSet;
import java.util.Random;

/**
 * Created by Ренат on 08.08.2016.
 */
public class z_RandomNumberGenerator
{
    private final BitSet input;
    private final Random rnd;
    private final int Count;
    private int genCount=0;
    public z_RandomNumberGenerator(int in, int seed)
    {
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
        return next;
    }
}
