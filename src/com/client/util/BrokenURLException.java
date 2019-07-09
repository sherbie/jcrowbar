package com.client.util;

public class BrokenURLException extends Exception {

	public BrokenURLException(String string) {
		super(string);
	}

	public BrokenURLException(Exception e) {
		super(e);
	}

}
