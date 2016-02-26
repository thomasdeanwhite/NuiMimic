package com.sheffield.instrumenter.analysis;

import java.util.ArrayList;

public class DependencyTree {
	
	private static DependencyTree depTree;
	
	public static DependencyTree getDependancyTree(){
		if (depTree == null){
			depTree = new DependencyTree();
		}
		return depTree;
	}
	
	protected class ClassNode {
		private String className;
		private ArrayList<ClassNode> children;
		
		public ClassNode(String className){
			children = new ArrayList<ClassNode>();
			this.className = className;
		}
		
		public void addChild(ClassNode child){
			if (!children.contains(child)){
				children.add(child);
			}
		}
		
		public String getClassName(){
			return className;
		}
		
		public ClassNode findClassNode(String className){
			ArrayList<String> seen = new ArrayList<String>(children.size());
			return findClassNode(className, seen);
		}
		
		private ClassNode findClassNode(String className, ArrayList<String> seen){
			for (ClassNode cn : children){
				if (seen.contains(cn.getClassName())){
					continue;
				}
				seen.add(cn.getClassName());
				if (cn.getClassName().equals(className)){
					return cn;
				} else {
					ClassNode result = cn.findClassNode(className, seen);
					if (result != null && !seen.contains(result.getClassName())){
						seen.add(result.getClassName());
						return result;
					}
				}
			}
			return null;

		}

		public void clear() {
			ArrayList<ClassNode> childrenBackup = new ArrayList<ClassNode>(children);
			children.clear();
			for (ClassNode cn : childrenBackup){
				cn.clear();
			}
		}
	}
	
	private ClassNode root;
	
	private DependencyTree(){
		root = new ClassNode("root");
	}
	
	public ClassNode getClassNode(String className){
		return root.findClassNode(className);
	}
	
	public void addDependancy(String className, String childName){
		ClassNode cn = root.findClassNode(className);
		
		if (cn == null){
			cn = new ClassNode(className);
			root.addChild(cn);
		}
		
		ClassNode child = root.findClassNode(childName);
		if (child == null){
			child = new ClassNode(childName);
		}
		
		cn.addChild(child);
		
		
	}

	public void clear(){
		root.clear();
	}
	
	
}
