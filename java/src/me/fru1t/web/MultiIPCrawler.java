package me.fru1t.web;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;

public class MultiIPCrawler {
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
			"Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko"
	};
	
	private byte[][] ips;
	private int currentPointer;
	
	public MultiIPCrawler(byte[]... ips) {
		if (ips.length < 1) {
			throw new RuntimeException("Must have 1 or more IPs specified");
		}
		
		this.ips = ips;
		this.currentPointer = 0;
	}
	
	/**
	 * Retrieves the data returned by the server.
	 * 
	 * @param url
	 * @return
	 */
	@Nullable
	public String getContents(String url) {
		try {
			RequestConfig config = RequestConfig.custom()
					.setLocalAddress(InetAddress.getByAddress(ips[currentPointer++]))
					.build();
			currentPointer %= ips.length;
			HttpGet get = new HttpGet(url);
			get.setConfig(config);
			get.setHeader("user-agent", getRandomUserAgent());
			
			CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(get);
			return EntityUtils.toString(response.getEntity());
			
		} catch (IOException e) {
			Boot.getLogger().log(e);
		}
		
		return null;
	}
	
	private static String getRandomUserAgent() {
		return USER_AGENTS[(int) (Math.random() * USER_AGENTS.length)];
	}
}
