package me.fru1t.web;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.util.Consumer;
import me.fru1t.util.LRUMap;
import me.fru1t.util.Logger;
import me.fru1t.util.ThreadUtils;

/**
 * Concurrent crawler that utilizes multiple IP Addresses
 */
public class MultiIPCrawler {
	/**
	 * Represents an IP address.
	 */
	private static class  IP{
		public byte[] ip;
		public boolean inUse;
		public long lastUsed;
		public String name;
		public IP(byte[] ip, String name) {
			this.ip = ip;
			this.name = name;
			this.lastUsed = 0;
			this.inUse = false;
		}
	}
	private static final int MAX_THREAD_AGE_MS = 30000;
	private static final int LOCAL_CACHE_SIZE = 10;
	private static final String[] USER_AGENTS = {
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1",
			"Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10; rv:33.0) Gecko/20100101 Firefox/33.0",
			"Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox/31.0",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20130401 Firefox/31.0",
			"Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0",
			"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36",
			"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2225.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko",
			"Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/1.0.154.53 Safari/525.19",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/1.0.154.36 Safari/525.19",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/7.0.540.0 Safari/534.10",
			"Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/534.4 (KHTML, like Gecko) Chrome/6.0.481.0 Safari/534.4",
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.86 Safari/533.4",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.2 (KHTML, like Gecko) Chrome/4.0.223.3 Safari/532.2",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/4.0.201.1 Safari/532.0",
			"Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.27 Safari/532.0",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/530.5 (KHTML, like Gecko) Chrome/2.0.173.1 Safari/530.5",
			"Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.558.0 Safari/534.10",
			"Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/540.0 (KHTML,like Gecko) Chrome/9.1.0.0 Safari/540.0",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.14 (KHTML, like Gecko) Chrome/9.0.600.0 Safari/534.14",
			"Mozilla/5.0 (X11; U; Windows NT 6; en-US) AppleWebKit/534.12 (KHTML, like Gecko) Chrome/9.0.587.0 Safari/534.12",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.0 Safari/534.13",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.11 Safari/534.16",
			"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/534.20 (KHTML, like Gecko) Chrome/11.0.672.2 Safari/534.20",
			"Mozilla/5.0 (Windows NT 6.0) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.792.0 Safari/535.1",
			"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.872.0 Safari/535.2",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.36 Safari/535.7",
			"Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.45 Safari/535.19",
			"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24",
			"Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1090.0 Safari/536.6",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1",
			"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.15 (KHTML, like Gecko) Chrome/24.0.1295.0 Safari/537.15",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.93 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1467.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1623.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.103 Safari/537.36",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.38 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.4; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36 Edge/12.0",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.9600",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10547",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64; Xbox; Xbox One) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.1b3) Gecko/20090305 Firefox/3.1b3 GTB5",
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; ko; rv:1.9.1b2) Gecko/20081201 Firefox/3.1b2",
			"Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:1.9b5) Gecko/2008032620 Firefox/3.0b5",
			"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.12) Gecko/20080214 Firefox/2.0.0.12",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; cs; rv:1.9.0.8) Gecko/2009032609 Firefox/3.0.8",
			"Mozilla/5.0 (X11; U; OpenBSD i386; en-US; rv:1.8.0.5) Gecko/20060819 Firefox/1.5.0.5",
			"Mozilla/5.0 (Windows; U; Windows NT 5.0; es-ES; rv:1.8.0.3) Gecko/20060426 Firefox/1.5.0.3",
			"Mozilla/5.0 (Windows; U; WinNT4.0; en-US; rv:1.7.9) Gecko/20050711 Firefox/1.0.5",
			"Mozilla/5.0 (Windows; Windows NT 6.1; rv:2.0b2) Gecko/20100720 Firefox/4.0b2",
			"Mozilla/5.0 (X11; Linux x86_64; rv:2.0b4) Gecko/20100818 Firefox/4.0b4",
			"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2) Gecko/20100308 Ubuntu/10.04 (lucid) Firefox/3.6 GTB7.1",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0b7) Gecko/20101111 Firefox/4.0b7",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0b8pre) Gecko/20101114 Firefox/4.0b8pre",
			"Mozilla/5.0 (X11; Linux x86_64; rv:2.0b9pre) Gecko/20110111 Firefox/4.0b9pre",
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.0b9pre) Gecko/20101228 Firefox/4.0b9pre",
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.2a1pre) Gecko/20110324 Firefox/4.2a1pre",
			"Mozilla/5.0 (X11; U; Linux amd64; rv:5.0) Gecko/20100101 Firefox/5.0 (Debian)",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0a2) Gecko/20110613 Firefox/6.0a2",
			"Mozilla/5.0 (X11; Linux i686 on x86_64; rv:12.0) Gecko/20100101 Firefox/12.0",
			"Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15.0a2",
			"Mozilla/5.0 (X11; Ubuntu; Linux armv7l; rv:17.0) Gecko/20100101 Firefox/17.0",
			"Mozilla/5.0 (Windows NT 6.1; rv:21.0) Gecko/20130328 Firefox/21.0",
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:22.0) Gecko/20130328 Firefox/22.0",
			"Mozilla/5.0 (Windows NT 5.1; rv:25.0) Gecko/20100101 Firefox/25.0",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:25.0) Gecko/20100101 Firefox/25.0",
			"Mozilla/5.0 (Windows NT 6.1; rv:28.0) Gecko/20100101 Firefox/28.0",
			"Mozilla/5.0 (X11; Linux i686; rv:30.0) Gecko/20100101 Firefox/30.0",
			"Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0",
			"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0",
			"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)",
			"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8; Zune 4.7)",
			"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)",
			"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
			"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Win64; x64; Trident/6.0)",
			"Mozilla/5.0 (IE 11.0; Windows NT 6.3; Trident/7.0; .NET4.0E; .NET4.0C; rv:11.0) like Gecko",
			"Mozilla/5.0 (IE 11.0; Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; fi-fi) AppleWebKit/420+ (KHTML, like Gecko) Safari/419.3",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; de-de) AppleWebKit/125.2 (KHTML, like Gecko) Safari/125.7",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/312.8 (KHTML, like Gecko) Safari/312.6",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; cs-CZ) AppleWebKit/523.15 (KHTML, like Gecko) Version/3.0 Safari/523.15",
			"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/528.16 (KHTML, like Gecko) Version/4.0 Safari/528.16",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_6; it-it) AppleWebKit/528.16 (KHTML, like Gecko) Version/4.0 Safari/528.16",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-HK) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE) AppleWebKit/533.19.4 (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.55.3 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/6.1.3 Safari/537.75.14",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/600.3.10 (KHTML, like Gecko) Version/8.0.3 Safari/600.3.10",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11) AppleWebKit/601.1.39 (KHTML, like Gecko) Version/9.0 Safari/601.1.39"
	};

	private IP[] ips;
	private Logger logger;
	private int ipRestPeriodInMs;
	private int minContentLength;
	private Map<String, String> localCache;

	public MultiIPCrawler(Logger logger, int ipRestPeriodInMs, byte[][] ips) {
		if (ips.length < 1) {
			throw new RuntimeException("Must have 1 or more IPs specified");
		}

		this.ips = new IP[ips.length];
		for (int i = 0; i < ips.length; i++){
			this.ips[i] = new IP(ips[i], Integer.toString(i));
		}
		this.ipRestPeriodInMs = ipRestPeriodInMs;
		this.logger = logger;
		this.minContentLength = -1;
		this.localCache = Collections.synchronizedMap(new LRUMap<String, String>(LOCAL_CACHE_SIZE));
	}

	/**
	 * Sets the minimum content length threshold. If set, the crawler will continuously crawl
	 * a page until it returns with a content length greater than this setting. This is useful for
	 * unanticipated partial content returns.
	 *
	 * @param minContentLength The minimum content length to accept.
	 * @return This.
	 */
	public MultiIPCrawler setMinContentLength(int minContentLength) {
		this.minContentLength = minContentLength;
		return this;
	}

	public synchronized boolean insertDBitem(Scrape scrape, Consumer<Scrape> converter) {
		// We like logging.
		StringBuilder status = new StringBuilder();
		status.append("Processing scrape row with id: " + scrape.id + " and url \\" + scrape.url);
		
		// Find the best free IP to use, or return if there are none.
		IP freeIp = null;
		for (IP ip : ips) {
			if (!ip.inUse && (freeIp == null || freeIp.lastUsed > ip.lastUsed)) {
				freeIp = ip;
			}
		}
		if (freeIp == null) {
			status.append("; No free IPs found. Cancelling call.");
			logger.log(status.toString(), true);
			return false;
		}
		
		freeIp.inUse = true;
		dispatchDBQueryThread(freeIp, scrape, converter, status);
		
		return true;
	}
	
	private void dispatchDBQueryThread(IP ip, Scrape scrape, Consumer<Scrape> converter, StringBuilder status) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				long startTime = (new Date()).getTime();
				status.append("; IP: " + ip.name);

				try {
					// Do until it reaches all the way through the return or an error exits the
					// while loop.
					while (!Thread.currentThread().isInterrupted()) {
						// See if we need to wait between crawls (rest time - rested time)
						long waitTime = (ipRestPeriodInMs - (startTime - ip.lastUsed));
						if (waitTime > 0) {
							ThreadUtils.waitGauss((int) waitTime);
						} else {
							waitTime = 0;
						}
						status.append("; Waiting " + waitTime + "ms");

						// Execute DB query
						long requestStartTime = (new Date()).getTime();
						converter.eat(scrape);
						long requestEndTime = (new Date()).getTime();
						status.append("; Response: " + (requestEndTime - requestStartTime) + "ms");
						
						// Stats for nerds
						long endTime = (new Date()).getTime();
						status.append("; Total: " + (endTime - startTime) + "ms");

						// IP Cleanup
						ip.lastUsed = endTime;
						ip.inUse = false;

						logger.log(status.toString(), false);
						return;
					}
				} catch (Exception e) {
				}
			}
		})).start();
	}
	
	
	
	/**
	 * Starts a new AJAX request. Returns true if a free IP was found. Otherwise, returns false and
	 * doesn't start the request if no IPs were free.
	 *
	 * @param request
	 */
	public synchronized boolean sendRequest(Request request) {
		// We like logging.
		StringBuilder status = new StringBuilder();
		status.append("Crawling " + request.getUrl());

		// Check if the request is in the cache.
		synchronized (localCache) {
			// This synchronize block makes certain that between the time we check if the cache
			// contains the key, and the time we retrieve it, the object we're looking for doesn't
			// disappear.
			if (localCache.containsKey(request.getUrl())) {
				status.append("; hit cache. Success!");
				//logger.log(status.toString(), false);
				String localCacheResult = localCache.get(request.getUrl());

				// Onsuccess MUST be called asynchronously.
				(new Thread(new Runnable() {
					@Override
					public void run() {
						request.onSuccess(localCacheResult);
					}
				})).start();
				return true;
			}
		}

		// Find the best free IP to use, or return if there are none.
		IP freeIp = null;
		for (IP ip : ips) {
			if (!ip.inUse && (freeIp == null || freeIp.lastUsed > ip.lastUsed)) {
				freeIp = ip;
			}
		}
		if (freeIp == null) {
			status.append("; No free IPs found. Cancelling call.");
			logger.log(status.toString(), true);
			return false;
		}

		freeIp.inUse = true;
		dispatchCrawlThread(freeIp, request, status);
		return true;
	}

	private void dispatchCrawlThread(IP ip, Request request, StringBuilder status) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				long startTime = (new Date()).getTime();
				status.append("; IP: " + ip.name);

				CloseableHttpClient client = null;
				CloseableHttpResponse response = null;
				String responseString = null;
				try {
					// Do until it reaches all the way through the return or an error exits the
					// while loop.
					while (!Thread.currentThread().isInterrupted()) {
						// See if we need to wait between crawls (rest time - rested time)
						long waitTime = (ipRestPeriodInMs - (startTime - ip.lastUsed));
						if (waitTime > 0) {
							ThreadUtils.waitGauss((int) waitTime);
						} else {
							waitTime = 0;
						}
						status.append("; Waiting " + waitTime + "ms");

						// Set up GET request with IP and headers
						RequestConfig config = RequestConfig.custom()
								.setLocalAddress(InetAddress.getByAddress(ip.ip))
								.setConnectTimeout(MAX_THREAD_AGE_MS)
								.setConnectionRequestTimeout(MAX_THREAD_AGE_MS)
								.setSocketTimeout(MAX_THREAD_AGE_MS)
								.build();
						HttpGet get = new HttpGet(request.getUrl());
						get.setConfig(config);
						get.setHeader("user-agent", getRandomUserAgent());

						// Execute crawler
						long requestStartTime = (new Date()).getTime();
						client = HttpClients.createDefault();
						response = client.execute(get);

						// Fetch response as it comes in as a stream
						responseString = EntityUtils.toString(response.getEntity());
						long requestEndTime = (new Date()).getTime();
						status.append("; Response: " + responseString.length() + "b in "
								+ (requestEndTime - requestStartTime) + "ms");

						// Min content length check. This check helps combat HTTP errors where
						// the server will respond 200-OK, but will return garbage content
						if (minContentLength > 0
								&& responseString.length() < minContentLength) {
							status.append("; Failed minimum content length check of "
									+ minContentLength + ", retrying");
							continue; // retry
						}

						// Stats for nerds
						long endTime = (new Date()).getTime();
						status.append("; Total: " + (endTime - startTime) + "ms");

						// IP Cleanup
						ip.lastUsed = endTime;
						ip.inUse = false;

						// Update cache
						synchronized (localCache) {
							// A synchro is needed as it prevents knocking off cached items
							// while they're being used.
							localCache.put(request.getUrl(), responseString);
						}

						// Final words
						long cacheTime = (new Date()).getTime();
						status.append("; Cache time: " + (cacheTime - endTime)
								+ "ms; Scrape success!");
						//logger.log(status.toString(), false);
						request.onSuccess(responseString);
						return;
					}
				} catch (Exception e) {
					// Reset last used
					ip.lastUsed = (new Date()).getTime();

					// Print out error now.
					status.append("; Scrape failed with error: " + e.getMessage());
					logger.log(status.toString(), true);

					// Handle failure
					request.onFailure(e.getMessage());
					if (request.shouldRetryOnFail()) {
						StringBuilder newStatus = new StringBuilder("Retrying crawl "
								+ request.getUrl());
						dispatchCrawlThread(ip, request, newStatus);
					}
				} finally {
					// Memory cleanup
					try { if (client != null) client.close(); }
					catch (IOException e) {
						logger.log(e, "MultiIPCrawler couldn't close client"); }
					try { if (response != null) response.close(); }
					catch (IOException e) {
						logger.log(e, "MultiIpCrawler couldn't close response"); }
				}
			}
		})).start();
	}

	/**
	 * Returns the number of free IPs.
	 *
	 * @return
	 */
	public int countFreeIps() {
		int i = 0;
		for (IP ip : ips) {
			if (!ip.inUse) {
				i++;
			}
		}
		return i;
	}

	private static String getRandomUserAgent() {
		return USER_AGENTS[(int) (Math.random() * USER_AGENTS.length)];
	}
}
