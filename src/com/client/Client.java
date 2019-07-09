package com.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
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
	ArrayList<String> brokenUrls = new ArrayList<String>();

	public boolean isUrlChecked(String url) {
		return checkedUrls.contains(url);
	}

	int getNonHtmlMimeTypeResourceResponseCode(String url) throws MalformedURLException, IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setRequestMethod("GET");
		c.connect();
		c.disconnect();

		return c.getResponseCode();
	}

	Document transformNode(Node node) throws UnsupportedMimeTypeException, IOException {
		var pageUrl = node.getParent() != null ? node.getParent().getUrl() : baseNode.getUrl();
		var info = "page=" + pageUrl + " link=" + node.getUrl();

		try {
			Document d = Jsoup.connect(node.getUrl()).get();
			System.out.println("OK : " + info);
			return d;
		} catch( UnsupportedMimeTypeException e ) {
			var responseCode = getNonHtmlMimeTypeResourceResponseCode(node.getUrl());
			if( responseCode < 400 ) {
				System.out.println("OK : " + info);
				throw e;
			} else {
				System.out.println("ERR: " + info);
				var ex = new IOException("HTTP GET " + node.getUrl() + " returned " + responseCode);
				brokenUrls.add(node.getUrl());
				ex.printStackTrace();
				throw ex;
			}
		} catch( IOException e ) {
			System.out.println("ERR: " + info);
			brokenUrls.add(node.getUrl());
			e.printStackTrace();
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

		checkedUrls.add(node.getUrl());

		try {
			Document nodeDocument = transformNode(node);
			generateChildNodes(node, nodeDocument);

			if( depth > depthLimit )
				return;
			for( Node n : node.getChildren() ) {
				crawl(n, ++depth);
			}
		} catch( IOException e) {}
	}

	public boolean crawl() {
		crawl(baseNode, 0);
		return brokenUrls.size() == 0;
	}
}

