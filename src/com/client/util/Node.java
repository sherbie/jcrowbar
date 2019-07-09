package com.client.util;

import java.util.ArrayList;

public class Node {
	String url;
	
	public Node(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	Node parent;

	public Node getParent() {
		return parent;
	}

	void setParent(Node node) {
		parent = node;
	}

	ArrayList<Node> children = new ArrayList<Node>();

	public ArrayList<Node> getChildren() {
		return children;
	}


	public void addChildWithUrl(String url) {
		Node n = new Node(url);
		n.setParent(this);
		children.add(n);
	}
}

