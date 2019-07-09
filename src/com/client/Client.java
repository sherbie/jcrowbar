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

/**
 * @author sherbie
 * The Client is a handler for the jcrowbar cli. It will simultaneously build and traverse the Node tree in a depth-first-search manner.
 */
public class Client {
	Node baseNode;
	int depthLimit;
	
	/**
	 * @param url  A resource handle (e.g. web page URL) conforming to RFC 3986
	 * @param maxDepth  A zero-based index which limits how deep the client will crawl. 0 means only URLs on the initial page load will be checked.
	 */
	public Client(String url, int maxDepth) {
		baseNode = new Node(url);
		depthLimit = maxDepth;
	}
	
	ArrayList<String> checkedUrls = new ArrayList<String>();
	ArrayList<String> brokenUrls = new ArrayList<String>();

	public boolean isUrlChecked(String url) {
		return checkedUrls.contains(url);
	}

	/**
	 * Get the response code of a basic HTTP GET request.
	 * @param url  A resource handle (e.g. web page URL) conforming to RFC 3986
	 * @return  HTTP GET response code
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	int getNonHtmlMimeTypeResourceResponseCode(String url) throws MalformedURLException, IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setRequestMethod("GET");
		c.connect();
		c.disconnect();

		return c.getResponseCode();
	}

	/**
	 * Attempts to create a jsoup document object from an HTTP GET request. If a document was unable to be created due to an UnsupportedMimeTypeException, it will make a simple HTTP GET request to see if the resource exists.
	 * @param node
	 * @return  The document object of the response.
	 * @throws UnsupportedMimeTypeException
	 * @throws IOException
	 */
	Document transformNode(Node node) throws UnsupportedMimeTypeException, IOException {
		var pageUrl = node.getParent() != null ? node.getParent().getUrl() : baseNode.getUrl();
		var info = "page=" + pageUrl + " link=" + node.getUrl();

		try {
			Document d = Jsoup.connect(node.getUrl()).get();
			System.out.println("OK : " + info);
			return d;
		} catch( UnsupportedMimeTypeException e ) {
			var responseCode = getNonHtmlMimeTypeResourceResponseCode(node.getUrl());
			boolean isGoodResponseCode = responseCode < 400;

			if( isGoodResponseCode ) {
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

	/**
	 * Create child nodes for the relevant node based on each href and src attribute found in the node's URL response body.
	 * @param node
	 * @param document
	 */
	void generateChildNodes(Node node, Document document) {
		for( Element e : document.body().getElementsByAttribute("href") ) {
			var link = e.absUrl("href");
			if( link != "" )
				node.addChildWithUrl(link);
		}
		for( Element e : document.body().getElementsByAttribute("src") ) {
			var link = e.absUrl("src");
			if( link != "" )
				node.addChildWithUrl(link);
		}
	}

	/**
	 * Do a depth-first recursive search of all located URLs to a maximum depth.
	 * @param node
	 * @param depth
	 */
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

	/**
	 * Traverse all possible nodes and return true if all links found are valid.
	 * @return
	 */
	public boolean crawl() {
		crawl(baseNode, 0);
		return brokenUrls.size() == 0;
	}
}

