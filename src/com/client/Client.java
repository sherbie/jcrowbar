package com.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.client.util.BrokenURLException;
import com.client.util.Node;

/**
 * @author sherbie
 * The Client is a handler for the jcrowbar cli. It will simultaneously build and traverse the Node tree in a breadth-first-search manner.
 */
public class Client {
	Node baseNode;
	int depthLimit;

	/**
	 * @param url  A resource handle (e.g. web page URL) conforming to RFC 3986
	 * @param maxDepth  A zero-based index which limits how deep the client will crawl. In order to check links contained in a response from a url of depth D, maxDepth must be D+1.
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
	int getNonHtmlMimeTypeResourceResponseCode(String url) throws IOException, ConnectException {
		URL urlObj = new URL(url);

		HttpURLConnection c = (HttpURLConnection) urlObj.openConnection();
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
	 * @throws BrokenURLException
	 */
	void transformNode(Node node, int depth) throws IOException, BrokenURLException {
		var pageUrl = node.getParent() != null ? node.getParent().getUrl() : baseNode.getUrl();
		var info = "depth=" + depth + " page=" + pageUrl + " link=" + node.getUrl();

		try {
			Document d = Jsoup.connect(node.getUrl()).get();
			System.out.println("OK : " + info);
			node.setDocument(d);
		} catch (MalformedURLException e) {
			Pattern pattern = Pattern.compile(".+:.*");
	        Matcher matcher = pattern.matcher(node.getUrl());

	        if( matcher.matches() ) {
        		String linkType = matcher.group(0).split(":")[0];
        		System.out.println("WRN: " + info + " '" + linkType + "' links not supported");
	        } else {
	        	throw e;
	        }
		} catch( IOException e ) {
			// At this point, the resource is a non-html document (unsupported mime type or poorly-formed html)

			try {
				var responseCode = getNonHtmlMimeTypeResourceResponseCode(node.getUrl());
				boolean isGoodResponseCode = responseCode < 400;

				if( isGoodResponseCode ) {
					System.out.println("OK : " + info);
				} else {
					System.out.println("ERR: " + info);
					brokenUrls.add(node.getUrl());
					throw new BrokenURLException("HTTP GET " + node.getUrl() + " returned " + responseCode);
				}
			} catch( ConnectException ex ) {
				System.out.println("ERR: " + info);
				brokenUrls.add(node.getUrl());
				throw new BrokenURLException("HTTP GET " + node.getUrl() + " connection timed out");
			}
		}
	}

	/**
	 * Create child nodes for the relevant node based on each href and src attribute found in the node's URL response body.
	 * @param node
	 * @param document
	 */
	void generateChildNodes(Node node) {
		var document = node.getDocument();

		Element[] topLevelElements = {document.body(), document.head()};

		for( Element topLevel : topLevelElements ) {
			for( Element e : topLevel.getElementsByAttribute("href") ) {
				var link = e.absUrl("href");
				if( link != "" )
					node.addChildWithUrl(link);
			}
			for( Element e : topLevel.getElementsByAttribute("src") ) {
				var link = e.absUrl("src");
				if( link != "" )
					node.addChildWithUrl(link);
			}
		}
	}

	/**
     * Do a breadth-first recursive search of all located URLs to a maximum depth.
	 * @param node
	 * @param depth
	 * @throws IOException
	 */
	void crawl(Node node, int depth) throws IOException {

		if( node.getDocument() != null && depth <= depthLimit ) {
			generateChildNodes(node);
		}

		for( Node n : node.getChildren() ) {
			try {
				if( !isUrlChecked(n.getUrl()) )
					transformNode(n, depth + 1);
			} catch (BrokenURLException e) {}
		}
		for( Node n : node.getChildren() ) {
			crawl(n, depth + 1);
		}
	}

	/**
	 * Traverse all possible nodes and return true if all links found are valid.
	 * @return
	 * @throws IOException
	 */
	public boolean crawl() throws IOException {
		try {
			transformNode(baseNode, 0);
			checkedUrls.add(baseNode.getUrl());
		} catch (BrokenURLException e) {}

		crawl(baseNode, 0);
		return brokenUrls.size() == 0;
	}
}

