# jcrowbar
A simple website crawler implemented in java

## Setup Requirements

This project was built using
* Maven (3.6.1): https://maven.apache.org/install.html
* Java JDK (SE 12): https://www.oracle.com/technetwork/java/javase/downloads/jdk12-downloads-5295953.html

## Installation

Run the installation script to compile all necessary code for execution:
```
./install.sh
```

## Execution

See execution details by running the application with the help flag
```
java -jar jcrowbar.jar -h
```

### Output

The following snippet was generated from running `java -jar jcrowbar.jar -h http://www.google.com 1`
```
OK : depth=0 page=http://www.google.com link=http://www.google.com
OK : depth=1 page=http://www.google.com link=https://mail.google.com/mail/?tab=wm&ogbl
......
OK : depth=1 page=http://www.google.com link=https://www.google.com/
OK : depth=2 page=https://mail.google.com/mail/?tab=wm&ogbl link=https://accounts.google.com/
```

Notes
* The initial depth is always 0 which indicates the entrypoint of the crawler.
* The depth D for any given page:link pair is derived from a response of a request to a URL with depth D-1 for all URLs with depth D > 0.
* The depth D provided as input to the cli will result in output not to exceed depth D+1.
* The page value indicates which URL was used at the time a request was sent
* The link value indicates which URL was located in the body of the response
* The crawler does not assume equivalence of similar URLs e.g. http://www.google.com, https://www.google.com, https://www.google.com/

## TODO

* Client-side DOM mutation support (e.g. client-side js-rendered links)
* More robust logging features
* Asynchronous page loading

:sparkles:
