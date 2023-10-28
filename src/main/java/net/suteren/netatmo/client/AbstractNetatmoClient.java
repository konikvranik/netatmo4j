package net.suteren.netatmo.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Setter @Getter @Slf4j
public abstract class AbstractNetatmoClient<T> {

	public static final String URLENCODED_CHARSET_UTF_8 = "application/x-www-form-urlencoded;charset=UTF-8";
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static final String NETATMO_BASE_URL = "https://api.netatmo.com";
	private HttpURLConnection connection;

	public T post(String path, Map<String, String> params, Object content,
		String contentType, Map<String, String> headers) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return callNetatmo("POST", path, content, contentType, params, headers);
	}

	public T post(String path, Map<String, String> params, Object content,
		String contentType) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return post(path, params, content, contentType, null);
	}

	public T post(String path, Map<String, String> params, Object content)
		throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return post(path, params, content, null, null);
	}

	public T post(String path, Map<String, String> params) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return post(path, params, null, null, null);
	}

	public T post(String path) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return post(path, null, null, null, null);
	}

	public T get(String path, Map<String, String> params, Object content,
		String contentType, Map<String, String> headers) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return callNetatmo("GET", path, content, contentType, params, headers);
	}

	public T get(String path, Map<String, String> params, Object content,
		String contentType) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return get(path, params, content, contentType, null);
	}

	public T get(String path, Map<String, String> params, Object content)
		throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return get(path, params, content, null, null);
	}

	public T get(String path, Map<String, String> params) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return get(path, params, null, null, null);
	}

	public T get(String path) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return get(path, null, null, null, null);
	}

	protected T callNetatmo(String method, String path, Object content, String contentType,
		Map<String, String> params, Map<String, String> headers)
		throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		connection = ((HttpURLConnection) (new URL(constructUrl(path, params)).openConnection()));
		connection.setRequestMethod(method);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", contentType);

		Optional.ofNullable(headers)
			.map(Map::entrySet)
			.stream().flatMap(Collection::stream)
			.forEach(e -> connection.setRequestProperty(e.getKey(), e.getValue()));

		log.debug("URL: {}", getConnection().getURL());
		log.debug("Content: {}", content);
		log.debug("Headers: {}", getConnection().getRequestProperties());

		if (content != null) {
			if (content instanceof Reader reader) {
				IOUtils.copy(reader, connection.getOutputStream());
			} else if (content instanceof InputStream inputStream) {
				IOUtils.copy(inputStream, connection.getOutputStream());
			} else if (content instanceof String string) {
				IOUtils.copy(IOUtils.toInputStream(string), connection.getOutputStream());
			} else {
				throw new IllegalStateException("Only InputStream, Reader or String are supported.");
			}
		}

		if (connection.getResponseCode() == 200) {
			return (T) connection.getContent();
		} else {
			throw new ConnectionException(connection);
		}
	}

	public static String queryParams(Map<String, String> params) {
		return params.entrySet().stream()
			.map(e -> "%s=%s".formatted(urlEncode(e.getKey()), urlEncode(e.getValue())))
			.collect(Collectors.joining("&"));
	}

	public static String constructUrl(String path, Map<String, String> params) {
		return NETATMO_BASE_URL + sanitizePath(path) + Optional.ofNullable(params)
			.map(AbstractNetatmoClient::queryParams)
			.map("?%s"::formatted)
			.orElse("");
	}

	public static String sanitizePath(String url) {
		return url.startsWith("/") ? url : ("/" + url);
	}

	public static String urlEncode(String value, String charset) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, charset);
	}

	@SneakyThrows
	public static String urlEncode(String value) {
		return urlEncode(value, "UTF-8");
	}
}
