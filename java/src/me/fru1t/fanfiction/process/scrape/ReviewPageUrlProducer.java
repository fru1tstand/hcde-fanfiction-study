package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.database.producers.StoryProducer.Story;
import me.fru1t.util.Producer;
import me.fru1t.util.concurrent.ConcurrentProducer;

public class ReviewPageUrlProducer extends ConcurrentProducer<String> {
	/**
	 * https://www.fanfiction.net/r/[ff_story_id]/[chapter]/[page_number]/;
	 * if [chapter] is 0, then all chapters' reviews are shown
	 * 
	 * `review` table's columns: 
	 * id, ff_story_id, which_chapter, date, reviewer_id
	 */
	
	private static final String REVIEW_BASE_URL = "https://www.fanfiction.net/r/%d/%d/%d/";
	private static boolean isComplete;
	
	private Producer<Story> storyProducer;
	private @Nullable Story currentStory;
	private int currentChapter;
	private int maxChapters;
	
	public ReviewPageUrlProducer(Producer<Story> storyProducer) {
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
			return getReviewUrl(currentStory.ff_story_id, currentChapter++, 1);
		}
		
		// Check if we still have stories to get reviews from:
		// Get the next story object
		do {
			currentStory = storyProducer.take();
			// No more stories from the database, so we're done.
			if (currentStory == null) {
				isComplete = true;
				return null;
			}
		} while (currentStory.reviews < 1);
		
		maxChapters = 0; currentChapter = 0;
		if (currentStory.chapters > 0) {
			maxChapters =  currentStory.chapters;
			currentChapter = 1;
		}
		
		return getReviewUrl(currentStory.ff_story_id, currentChapter++, 1);
	}

	private @Nullable String getReviewUrl(int ff_story_id, int chapter, int page_number) {
		return String.format(REVIEW_BASE_URL, ff_story_id, chapter, page_number);
	}

	@Override
	public boolean isComplete() {
		return isComplete;
	}
}