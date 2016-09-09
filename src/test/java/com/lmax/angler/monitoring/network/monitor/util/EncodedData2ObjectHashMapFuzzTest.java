package com.lmax.angler.monitoring.network.monitor.util;

import org.agrona.collections.Long2ObjectHashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class EncodedData2ObjectHashMapFuzzTest
{
    private final EncodedData2ObjectHashMap<EncodableKey, String> map;
    private final Long2ObjectHashMap<String> comparison;
    private final long seed = System.nanoTime();
    private final Random random = new Random(seed);
    private final int numberOfElements;

    public EncodedData2ObjectHashMapFuzzTest(final int initialCapacity, final float loadFactor, final int numberOfElements)
    {
        this.numberOfElements = numberOfElements;
        map = new EncodedData2ObjectHashMap<>(initialCapacity, loadFactor, 8,
                this::encodeKey, encoded -> Long.hashCode(encoded.getLong(0)),
                new EncodableKey(Long.MIN_VALUE));
        comparison = new Long2ObjectHashMap<>(initialCapacity, loadFactor);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
                {
                { 2, 0.5f, 37 }, { 4, 0.6f, 23497 }, { 16, 0.1f, 16 }, { 64, 0.2222f, 883 }, { 4096, 0.3f, 65555 }
        });
    }

    @Test
    public void shouldWorkWithLongMap() throws Exception
    {
        try
        {
            final List<EncodableKey> keyList = new ArrayList<>();
            for (int i = 0; i < numberOfElements; i++)
            {
                final EncodableKey e = new EncodableKey(Math.abs(random.nextLong()));
                keyList.add(e);
                comparison.put(e.value, Long.toString(e.value));
            }

            keyList.forEach(e ->
            {
                assertThat(failureMessage("Expected map to contain key " + e), comparison.containsKey(e.value), is(true));
            });
        }
        catch(RuntimeException e)
        {
            e.printStackTrace();
            fail(failureMessage(e.getMessage()));
        }

    }

    @Test
    public void shouldContainValuesAfterInsert() throws Exception
    {
        try
        {
            final List<EncodableKey> keyList = new ArrayList<>();
            for (int i = 0; i < numberOfElements; i++)
            {
                final EncodableKey e = new EncodableKey(Math.abs(random.nextLong()));
                keyList.add(e);
                map.put(e, Long.toString(e.value));
            }

            keyList.forEach(e ->
            {
                assertThat(failureMessage("Expected map to contain key " + e), map.containsKey(e), is(true));
            });
        }
        catch(RuntimeException e)
        {
            e.printStackTrace();
            fail(failureMessage(e.getMessage()));
        }
    }

    private String failureMessage(final String input)
    {
        return "SEED: " + seed + "; " + input;
    }

    private static final class EncodableKey
    {
        private long value;

        private EncodableKey(final long value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return "EncodableKey{" +
                    "value=" + value +
                    '}';
        }
    }

    private void encodeKey(final EncodableKey key, final ByteBuffer keyBuffer)
    {
        keyBuffer.putLong(key.value);
    }
}