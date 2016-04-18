package me.fru1t.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;


import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;

/**
 *
 */
public class Crawler {
	@Nullable
	public static String getContents(String url) {
		StringBuilder response = new StringBuilder();
		try {
			java.net.URL website = new java.net.URL(url);
			URLConnection conn = website.openConnection();
			conn.setRequestProperty("accept-encoding", "gzip, deflate, sdch");
			conn.setRequestProperty("accept-language", "en-US,en;q=0.8");
			conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
			conn.connect();
			
			InputStreamReader isReader = null;
			if (conn.getContentEncoding().equals("gzip")) {
				isReader = new InputStreamReader(new GZIPInputStream(conn.getInputStream()), StandardCharsets.UTF_8);
			} else {
				isReader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
			}
			BufferedReader in = new BufferedReader(isReader);
			while (in.ready()) {
				response.append(in.readLine());
			}
			in.close();
			isReader.close();
			return response.toString();
		} catch (Exception e) {
			Boot.getLogger().log(e);
			return null;
		}
	}
}
