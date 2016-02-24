package com.sheffield.instrumenter.analysis;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sheffield.instrumenter.analysis.DependancyTree.ClassNode;

public class DependancyTreeTest {

	@Test
	public void testChild (){
		DependancyTree dt = DependancyTree.getDependancyTree();
		dt.addDependancy("foo.Bar", "bar.Foo");
		
		ClassNode cn = dt.getClassNode("bar.Foo");
		
		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
		
	}
	
}
