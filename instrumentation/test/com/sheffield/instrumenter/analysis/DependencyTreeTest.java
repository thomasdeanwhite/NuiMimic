package com.sheffield.instrumenter.analysis;

import com.sheffield.instrumenter.analysis.DependencyTree.ClassNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class DependencyTreeTest {

	@Test
	public void testChild (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo.Bar", "bar.Foo");
		
		ClassNode cn = dt.getClassNode("bar.Foo");
		
		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
		
	}

	@Test
	public void testGrandChild (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo.Bar", "step");

		dt.addDependency("step", "bar.Foo");

		ClassNode cn = dt.getClassNode("bar.Foo");

		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
	}

	@Test
	public void testRecursion (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo.Bar", "step");

		dt.addDependency("step", "foo.bar");

		ClassNode cn = dt.getClassNode("invalid");

		assertNull(cn);
	}

	@Test
	public void testSecondChild (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo.Bar", "step");


		dt.addDependency("foo.Bar", "bar.Foo");

		ClassNode cn = dt.getClassNode("bar.Foo");

		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
	}

	@Test
	public void testDoubleRecursion (){
		DependencyTree dt = DependencyTree.getDependencyTree();
		dt.clear();
		dt.addDependency("foo.Bar", "step");
		dt.addDependency("step", "step2");
		dt.addDependency("step2", "foo.Bar");
		dt.addDependency("foo.Bar", "step2");
		dt.addDependency("foo.bar", "bar.Foo");
		ClassNode cn = dt.getClassNode("bar.Foo");

		assertNotNull(cn);
		assertEquals("bar.Foo", cn.getClassName());
	}
	
}
