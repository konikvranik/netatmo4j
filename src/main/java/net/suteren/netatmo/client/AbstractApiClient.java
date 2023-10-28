package net.suteren.netatmo.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import net.suteren.netatmo.auth.AuthClient;

public class AbstractApiClient<T> extends AbstractNetatmoClient<T> {
	public AbstractApiClient(AuthClient auth) {
		this.auth = auth;
	}

	@Override
	protected T callNetatmo(String method, String path, Object content, String contentType,
		Map<String, String> params, Map<String, String> headers)
		throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		if (MapUtils.isEmpty(headers)) {
			headers = new LinkedHashMap<>();
		}
		headers.put("Authorization", "Bearer %s".formatted(auth.getToken()));
		return super.callNetatmo(method, "/api" + sanitizePath(path), content, contentType, params, headers);
	}

	private final AuthClient auth;
}