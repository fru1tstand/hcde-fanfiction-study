package me.fru1t.web;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.util.Consumer;

/**
 * Represents an asynchronous call defining methods for handling.
 */
public class Request {
	private String url;
	private Consumer<String> onSuccess;
	private @Nullable Consumer<String> onFailure;
	private Boolean shouldRetryOnFail;

	/**
	 * Creates a new request given an onSuccess consumer and failure. Defaults to retrying on
	 * failure.
	 * 
	 * @param url
	 * @param onSuccess
	 * @param onFailure
	 */
	public Request(String url,
			Consumer<String> onSuccess,
			@Nullable Consumer<String> onFailure) {
		this.url = url;
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
		this.shouldRetryOnFail = true;
	}

	public void shouldRetryOnFail(Boolean shouldRetryOnFail) {
		this.shouldRetryOnFail = shouldRetryOnFail;
	}

	public Request(String url, Consumer<String> onSuccess) {
		this(url, onSuccess, null);
	}

	public String getUrl() {
		return url;
	}

	public void onSuccess(String data) {
		onSuccess.eat(data);
	}

	public void onFailure(String data) {
		if (onFailure != null) {
			onFailure.eat(data);
		}
	}

	public boolean shouldRetryOnFail() {
		return shouldRetryOnFail;
	}
}
