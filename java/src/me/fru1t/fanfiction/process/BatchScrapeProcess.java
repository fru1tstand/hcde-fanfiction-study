package me.fru1t.fanfiction.process;

import java.util.HashMap;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.util.Consumer;
import me.fru1t.util.Logger;
import me.fru1t.util.ThreadUtils;
import me.fru1t.util.concurrent.ConcurrentProducer;
import me.fru1t.web.Request;

/**
 * Scrapes whatever urls are given from a producer into the database.
 */
public class BatchScrapeProcess implements Runnable {
    private static final int WATCHDOG_SLEEP_TIME_MS = 500;
    private static final int BUFFER_SIZE = 1024 * 1024 * 10;

    private ConcurrentProducer<String> urlProducer;
    private String[] queuedCrawlUrl;
    private HashMap<String, String> batchCrawlContent;
    private int crawledLen;
    
    /**
     * Creates a new scrape process given the urls in the form of a producer and the session name.
     * @param urlProducer
     * @throws InterruptedException 
     */
    public BatchScrapeProcess(ConcurrentProducer<String> urlProducer) throws InterruptedException {
        this.urlProducer = urlProducer;
        this.queuedCrawlUrl = new String[1];
        this.queuedCrawlUrl[0] = null;
        this.batchCrawlContent = new HashMap<>();
        this.crawledLen = 0;
    }

    @Override
    public void run() {
        Boot.getLogger().log("Running BatchScrapeProcess with : "
        		+ "\n\t\t server_name  		: " + Boot.getServerName() 
        		+ "\n\t\t command      		: " + Boot.getCommand()
        		+ "\n\t\t scrape_tablename 	: " + Boot.getScrapeTablename()
        		+ "\n\t\t session_name 		: " + Boot.getSessionOfThisRun().getName(), true);

        // Startup loop, saturate queue by calling until it returns false.
        while (queueNextScrape());

        // Watchdog for when we reach the end of a fandom
        while (!urlProducer.isComplete() && !Thread.currentThread().isInterrupted()) {
            if (Boot.getCrawler().countFreeIps() > 0) {
                while (queueNextScrape());
            }

            try {
                ThreadUtils.waitGauss(WATCHDOG_SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                Boot.getLogger().log("Watchdog was interrupted and is shutting down all children", true);
                return;
            }
        }
        
        Boot.getLogger().log("Completed BatchScrapeProcess with session name: " 
        						+ Boot.getSessionOfThisRun().getName(), true);
    }

    private synchronized boolean queueNextScrape() {
        synchronized (queuedCrawlUrl) {
            if (queuedCrawlUrl[0] == null) {
                queuedCrawlUrl[0] = urlProducer.take();
            }
            if (queuedCrawlUrl[0] == null) {
                return false;
            }
            
            String tCrawlUrl = queuedCrawlUrl[0];
            if (Boot.getCrawler().sendRequest(new Request(tCrawlUrl, new Consumer<String>() {
                    @Override
                    public void eat(String crawlContent) {
                        // If we're super fast, this might happen.
                        synchronized (queuedCrawlUrl) {
                        	if (queuedCrawlUrl[0] == tCrawlUrl) {
                        		queuedCrawlUrl[0] = null;
                        	}
                        }

                        queueNextScrape();

                        HashMap<String, String> temp = null;
                        synchronized(batchCrawlContent) {
                        	int contentLen = 2 * crawlContent.length();
                        	crawledLen += contentLen;
                        	batchCrawlContent.put(tCrawlUrl, crawlContent);
                        	
                        	if (crawledLen > BUFFER_SIZE || urlProducer.isComplete()) {
                        		temp = new HashMap<String, String>();
                        		temp.putAll(batchCrawlContent);
                        		crawledLen = 0;
                        		batchCrawlContent.clear();
                        	}
                        }

                        try {
                        	if (temp != null) {
                        		Boot.getLogger().log("Batching " + temp.size() + " inserts until " + tCrawlUrl, true);
                        		StoredProcedures.addScrapeBatch(temp);
                        	}
                        } catch (InterruptedException e) {
                        	Boot.getLogger().log(e, "While adding the scrapes to the database the thread was interrupted.");
                        	Logger.writeToFile(e, "queueNextScrape", "url", temp.keySet());
                        }

                    }
            })))
            {
                queuedCrawlUrl[0] = null;
                return true;
            }
            
            return false;
        }
    }
}
