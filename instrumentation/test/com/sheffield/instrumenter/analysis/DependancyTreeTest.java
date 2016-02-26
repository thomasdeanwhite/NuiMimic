package com.sheffield.instrumenter.analysis;

import com.sheffield.instrumenter.analysis.DependancyTree.ClassNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class DependancyTreeTest {

	@Test
	public void testChild (){
		DependancyTree dt = DependancyTree.getDependancyTree();
		dt.clear();
		dt.addDependancy("foo.Bar", "bar.Foo");
		
		ClassNode cn = dt.getClassNode("bar.Foo");
		
		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
		
	}

	@Test
	public void testGrandChild (){
		DependancyTree dt = DependancyTree.getDependancyTree();
		dt.clear();
		dt.addDependancy("foo.Bar", "step");

		dt.addDependancy("step", "bar.Foo");

		ClassNode cn = dt.getClassNode("bar.Foo");

		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
	}

	@Test
	public void testRecursion (){
		DependancyTree dt = DependancyTree.getDependancyTree();
		dt.clear();
		dt.addDependancy("foo.Bar", "step");

		dt.addDependancy("step", "foo.bar");

		ClassNode cn = dt.getClassNode("invalid");

		assertNull(cn);
	}

	@Test
	public void testSecondChild (){
		DependancyTree dt = DependancyTree.getDependancyTree();
		dt.clear();
		dt.addDependancy("foo.Bar", "step");


		dt.addDependancy("foo.Bar", "bar.Foo");

		ClassNode cn = dt.getClassNode("bar.Foo");

		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
	}

	@Test
	public void testDoubleRecursion (){
		DependancyTree dt = DependancyTree.getDependancyTree();
		dt.clear();
		dt.addDependancy("foo.Bar", "step");
		dt.addDependancy("step", "step2");
		dt.addDependancy("step2", "foo.Bar");
		dt.addDependancy("foo.Bar", "step2");
		dt.addDependancy("foo.bar", "bar.Foo");
		ClassNode cn = dt.getClassNode("bar.Foo");

		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
	}
	
}
