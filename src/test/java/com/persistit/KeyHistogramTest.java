package com.persistit;

import org.junit.Test;

import com.persistit.Exchange;
import com.persistit.Key;
import com.persistit.KeyHistogram;
import com.persistit.Persistit;
import com.persistit.KeyHistogram.KeyCount;
import com.persistit.unit.PersistitUnitTestCase;

public class KeyHistogramTest extends PersistitUnitTestCase {

	private String _volumeName = "persistit";

	@Test
	public void testCull() throws Exception {
		final int total = 1000;
		final int samples = 10;
		final KeyHistogram histogram = new KeyHistogram(null, null, null, samples, 1, 1);
		final Key key = new Key((Persistit)null);
		for (int i = 0; i < total; i++) {
			key.to(i);
			histogram.addKeyCopy(key);
		}
		histogram.cull();
		assertEquals(total, histogram.getKeyCount());
		assertEquals(samples, histogram.getSampleSize());
		for (final KeyCount sample : histogram.getSamples()) {
			System.out.println(sample);
		}
	}
	
	@Test
	public void testGather() throws Exception {
		final Exchange exchange = _persistit.getExchange(_volumeName,
				"HistogramTestTestGather", true);
		final int total = 100000;
		final int samples = 10;
		exchange.removeAll();
		for (int i = 0; i < total; i++) {
			exchange.getValue().put(String.format("Record %08d", i));
			exchange.to(i).store();
		}
		final KeyHistogram histogram = exchange.computeHistogram(null, null, samples, 1, 1);
		assertEquals(samples, histogram.getSampleSize());
		for (final KeyCount sample : histogram.getSamples()) {
			System.out.println(sample);
		}
	}
	
	@Test
	public void testUnequalDistribution() throws Exception {
		final Exchange exchange = _persistit.getExchange(_volumeName,
				"HistogramTestTestGather", true);
		final int total = 100000;
		final int samples = 10;
		exchange.removeAll();
		for (int i = 0; i < total; i++) {
			exchange.getValue().put(String.format("Record %08d", i));
			final String which = i % 100 < 10 ? "A": i % 100 < 90 ? "B" : "C";
			exchange.clear().append(which).append(i).store();
		}
		final KeyHistogram histogram = exchange.computeHistogram(null, null, samples, 1, 0);
		for (final KeyCount sample : histogram.getSamples()) {
			System.out.println(sample);
		}
		assertEquals(3, histogram.getSampleSize());
	}

	@Override
	public void runAllTests() throws Exception {
		testCull();
		testGather();
		testUnequalDistribution();
	}

}
