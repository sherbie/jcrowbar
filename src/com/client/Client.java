package com.client;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.client.util.Node;

public class Client {
	Node baseNode;
	int depthLimit;
	
	public Client(String url, int maxDepth) {
		baseNode = new Node(url);
		depthLimit = maxDepth;
	}
	
	ArrayList<String> checkedUrls = new ArrayList<String>();

	public boolean isUrlChecked(String url) {
		return checkedUrls.contains(url);
	}

	Document transformUrl(String url) throws IOException {
		try {
			Document d = Jsoup.connect(url).get();
			System.out.println("OK : " + url);
			return d;
		} catch( IOException e) {
			e.printStackTrace();
			System.out.println("ERR: " + url);
			throw e;
		}
	}

	void generateChildNodes(Node node, Document d) {
		for( Element e : d.body().getElementsByAttribute("href") ) {
			var link = e.absUrl("href");
			if( link != "" )
				node.addChildWithUrl(link);
		}
		for( Element e : d.body().getElementsByAttribute("src") ) {
			var link = e.absUrl("src");
			if( link != "" )
				node.addChildWithUrl(link);
		}
	}

	void crawl(Node node, int depth) {
		if( isUrlChecked(node.getUrl()) )
			return;

		try {
			checkedUrls.add(node.getUrl());
			Document nodeDocument = transformUrl(node.getUrl());
			generateChildNodes(node, nodeDocument);

			if( depth > depthLimit )
				return;
			for( Node n : node.getChildren() ) {
				crawl(n, ++depth);
			}
		} catch( IOException e) {}
	}

	public void crawl() {
		crawl(baseNode, 0);
	}
}

