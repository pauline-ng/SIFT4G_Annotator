package org.siftdna.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.siftdna.siftobjects.SIFTQuery;

public class TEST {

	public TEST() {
		
	}
	
	public static void main(String[] args) {
		System.out.println("Here");
		TEST test = new TEST();
		test.test();
	}
	
	public void test() {
		SIFTQuery q1 = new SIFTQuery("18,12345,1,C/C");
		SIFTQuery q2 = new SIFTQuery("18,12345,1,C/T");
		SIFTQuery q3 = new SIFTQuery("18,12345,1,C/A");
		SIFTQuery q4 = new SIFTQuery("18,12345,1,C/G");
		List<SIFTQuery> q = new ArrayList<SIFTQuery>();
		q.add(q1);
		q.add(q2);
		q.add(q3);
		q.add(q4);
		System.out.println("Hello");
		for (SIFTQuery query : q) {
			System.out.println("Before: " + query.getQuery());
		}
		
		Collections.sort(q);
		
		for (SIFTQuery query : q) {
			System.out.println("After: " + query.getQuery());
		}
		System.out.println("Goodbye");
		
	}
	
	
	
}
