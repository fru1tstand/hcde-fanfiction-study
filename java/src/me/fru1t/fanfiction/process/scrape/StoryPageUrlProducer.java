package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.database.producers.StoryProducer.Story;
import me.fru1t.util.Producer;
import me.fru1t.util.concurrent.ConcurrentProducer;

public class StoryPageUrlProducer extends ConcurrentProducer<String> {
	/**
	 * https://www.fanfiction.net/s/[ff_story_id]/[chapter]/;
	 * 
	 */
	
	private static final String STORY_BASE_URL = "https://www.fanfiction.net/s/%d/%d/";
	private static boolean isComplete;
	
	private Producer<Story> storyProducer;
	private @Nullable Story currentStory;
	private int currentChapter;
	private int maxChapters;
	
	public StoryPageUrlProducer(Producer<Story> storyProducer) {
		this.storyProducer = storyProducer;
		this.currentStory = null;
		this.currentChapter = Integer.MAX_VALUE;
		this.maxChapters = Integer.MIN_VALUE;
		isComplete = false;
	}

	@Override
	@Nullable
	public synchronized String take() {
		if (isComplete) {
			return null;
		}
		
		// Check if we still have pages ready to serve
		if (currentChapter <= maxChapters) {
			return getStoryUrl(currentStory.ff_story_id, currentChapter++);
		}
		
		currentStory = storyProducer.take();
		// No more stories from the database, so we're done.
		if (currentStory == null) {
			isComplete = true;
			return null;
		}
		
		maxChapters = currentStory.chapters;
		currentChapter = maxChapters == 0 ? 0 : 1;
		
		return getStoryUrl(currentStory.ff_story_id, currentChapter++);
	}

	private @Nullable String getStoryUrl(int ff_story_id, int chapter) {
		return String.format(STORY_BASE_URL, ff_story_id, chapter);
	}

	@Override
	public boolean isComplete() {
		return isComplete;
	}
}