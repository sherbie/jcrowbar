package jcrowbar;

import com.client.Client;

public class jcrowbar {
	public static void main(String[] args) {
		Client c = new Client(args[0], Integer.parseInt(args[1]) );
		if( c.crawl() )
			System.exit(0);
		else
			System.exit(1);
	}
}

