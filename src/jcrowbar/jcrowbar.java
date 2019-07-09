package jcrowbar;

import java.io.IOException;
import java.util.ArrayList;

import com.client.Client;

/**
 * @author sherbie
 * jcrowbar website crawler cli
 *
 * This tool will crawl a webpage from a given URL to maximum depth for broken links. An exit code 1 is returned if any broken links were found.
 *
 * Features implemented:
 *
 * - Depth-first traversal of target resource's href and src attribute links
 * - Output '[status]: page:link' message for each link checked where 'page' is the url where 'link' was located
 * - Configurable maximum depth
 * - Return non-zero status if any broken links were found
 * - Link traversal works with almost any mime type
 */
public class jcrowbar {

	static final String help =
			"Usage: java -jar jcrowbar.jar [-h|--h|-help|--help] [URL] [depth]\n\n" +
			"-h\tshow this help page\n" +
			"[URL]\tThe target URL of a resource to link crawl. See RFC 3986 for URL syntax specifications.\n" +
			"[depth]\tA zero-based index of the maximum depth to traverse through the target URL.";

	static final ArrayList<String> helpFlags = new ArrayList<String>(){{
	    add("-h");
	    add("--h");
	    add("-help");
	    add("--help");
	}};

	public static void main(String[] args) throws IOException {
		if( args.length < 1 || helpFlags.stream().filter(x -> x.equalsIgnoreCase(args[0])).count() > 0 ) {
			System.out.println(help);
			System.exit(0);
		}

		Client c = new Client(args[0], Integer.parseInt(args[1]) );
		if( c.crawl() )
			System.exit(0);
		else
			System.exit(1);
	}
}

