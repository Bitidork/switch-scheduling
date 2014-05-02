package util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiHashMapTest {
	MultiHashMap<String, String> map;

	@Before
	public void setUp() throws Exception {
		map = new MultiHashMap<String, String>( );
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPut() {
		ArrayList<String> S = new ArrayList<String>();
		
		for ( int i = 0; i < 10; i++ ) {
			S.add(Integer.toString( i ));
			map.put( S.get(i), S.get(i) );
			map.put( S.get(i), S.get(i) );
			if ( i >= 1 )
				map.put( S.get(i), S.get(i - 1));
			else
				map.put( S.get(i),  Integer.toString(-1));
		}
		
		for ( int i = 0; i < 10; i++ ) {
			Set<String> set = map.get( S.get(i) );
			
			assertEquals(2, set.size() );
			assertEquals(true, set.contains(S.get(i)));
			assertEquals(true, set.contains(i == 0 ? Integer.toString(i - 1) : S.get(i-1)));
		}
	}

	@Test
	public void testPutAllKSetOfV() {
		ArrayList<String> S = new ArrayList<String>();
		
		for ( int i = 0; i < 10; i++ ) {
			S.add(Integer.toString( i ));
			map.putAll( S.get(i), new HashSet<String>(S) );
		}
		
		for ( int i = 0; i < 10; i++ ) {
			Set<String> set = map.get( S.get(i) );
			assertEquals( i + 1, set.size() );
		}
	}

	@Test
	public void testRemoveAll() {
		ArrayList<String> S = new ArrayList<String>();
		
		for ( int i = 0; i < 10; i++ ) {
			S.add(Integer.toString( i ));
			map.putAll( S.get(i), new HashSet<String>(S) );
		}
		
		assertEquals(new HashSet<String>(S), map.removeAll( S.get(S.size() - 1) ));
	}

	@Test
	public void testKeySet() {
		ArrayList<String> S = new ArrayList<String>();
		
		for ( int i = 0; i < 10; i++ ) {
			S.add(Integer.toString( i ));
			map.put( S.get(i), S.get(i) );
			map.put( S.get(i), S.get(i) );
			if ( i >= 1 )
				map.put( S.get(i), S.get(i - 1));
			else
				map.put( S.get(i),  Integer.toString(-1));
		}
		
		assertEquals( new HashSet<String>( S ), map.keySet() );
	}

}
