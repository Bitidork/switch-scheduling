package util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WeightedMultiHashMapTests {
	WeightedMultiHashMap<String, String> map;

	@Before
	public void setUp() throws Exception {
		map = new WeightedMultiHashMap<String, String>( );
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet() {
		map.put( "i pity", "the fool" );
		assertTrue(map.get("i pity").contains("the fool"));
	}

	@Test
	public void testPutKVFloat() {
		map.put( "i pity", "the fool", 2.0f );
		assertTrue(map.get("i pity").contains("the fool"));
	}

	@Test
	public void testGetWeightKV() {
		map.put( "i pity", "the fool" );
		assertEquals( (Float)1.0f, (Float)map.getWeight("i pity", "the fool") );
		map.put( "i pity", "the fool", 2.0f );
		assertEquals( (Float)2.0f, (Float)map.getWeight("i pity", "the fool") );
		
		assertEquals( "the fool", map.remove("i pity", "the fool") );
		assertEquals( (Float)0.0f, (Float)map.getWeight("i pity", "the fool") );
	}

	@Test
	public void testGetWeightK() {
		final String k = "i pity";
		assertEquals( (Float)0.0f, (Float)map.getWeight(k) );
		
		map.put( k, "the fool" );
		assertEquals( (Float)1.0f, (Float)map.getWeight(k) );
		
		map.put( k, "the fool", 2.0f );
		assertEquals( (Float)2.0f, (Float)map.getWeight(k) );
		
		map.remove( k, "doofoo" );
		assertEquals( (Float)2.0f, (Float)map.getWeight(k) );
		
		map.put( k, "doofoo" );
		map.put( k, "lol" );
		assertEquals( (Float)4.0f, (Float)map.getWeight(k) );
		
		map.get( k ).removeAll( Arrays.asList("doofoo", "lol", "not in there") );
		assertEquals( (Float)2.0f, (Float)map.getWeight(k) );
		
		map.get( k ).add("its in there now");
		assertEquals( (Float)3.0f, (Float)map.getWeight(k) );
		
		map.get( k ).remove("the fool");
		assertEquals( (Float)1.0f, (Float)map.getWeight(k) );
		
		map.get( k ).addAll( Arrays.asList("test1", "test1", "test2" ) );
		assertEquals( (Float)3.0f, (Float)map.getWeight(k) );
	}

	@Test
	public void testRemoveAll() {
		String k = "abc";
		Set<String> entries = new HashSet<String>(Arrays.asList("yes", "no", "maybe" ));
		map.putAll( k, entries );
		assertEquals( entries, map.removeAll( k ) );
		assertEquals( null, map.get( k ) );
		assertEquals( (Float)0.f, (Float)map.getWeight(k) );
	}

}
