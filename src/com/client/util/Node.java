package com.client.util;

import java.util.ArrayList;

/**
 * @author sherbie
 * A Node describes a place where the jcrowbar tool can visit. Nodes are organized in a tree-like hierarchy.
 */
public class Node {
	String url;
	
	/**
	 * @param url  A resource handle (e.g. web page URL) conforming to RFC 3986
	 */
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

