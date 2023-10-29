package net.suteren.netatmo.auth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import net.suteren.netatmo.client.AbstractNetatmoClient;
import net.suteren.netatmo.client.ConnectionException;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

public final class AuthClient extends AbstractNetatmoClient {

	private final String clientId;
	private final Object scope;
	private final String state;
	@Setter @Getter private String accessToken;
	@Setter @Getter private String refreshToken;
	@Setter @Getter private Instant validUntil;
	private final String clientSecret;
	private final OAuth2 oauth;
	private final File authFile;

	public AuthClient(String clientId, String clientSecret, Object scope, String state, File authFile) throws IOException {
		this.authFile = authFile;
		if (this.authFile.exists()) {
			fixFilePermissions(this.authFile);
			JsonNode tokens = OBJECT_MAPPER.readTree(this.authFile);
			accessToken = tokens.at("/access_token").textValue();
			refreshToken = tokens.at("/refresh_token").textValue();
			validUntil = Instant.ofEpochSecond(tokens.at("/valid_until").longValue());
			if (StringUtils.isBlank(clientId)) {
				clientId = tokens.at("/client_id").textValue();
			}
			if (StringUtils.isBlank(clientSecret)) {
				clientSecret = tokens.at("/client_secret").textValue();
			}
		}

		this.scope = scope;
		this.state = state;
		oauth = new OAuth2();
		this.clientSecret = clientSecret;
		this.clientId = clientId;

	}

	public String authorizeUrl(String redirectUri) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(4);
		map.put("client_id", clientId);
		map.put("redirect_uri", redirectUri);
		map.put("scope", renderScope(scope));
		map.put("state", state);
		return constructUrl("oauth2/authorize", map);
	}

	public String getToken() throws URISyntaxException, IOException, InterruptedException, ConnectionException {
		if (accessToken == null) {
			oauth.authorize(this::authorizeUrl);
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>(6);
			parameters.put("grant_type", "authorization_code");
			parameters.put("client_id", clientId);
			parameters.put("client_secret", clientSecret);
			parameters.put("code", oauth.getCode());
			parameters.put("redirect_uri", oauth.getRedirectUri());
			parameters.put("scope", renderScope(scope));
			token(parameters);
		} else if (validUntil.isBefore(Instant.now().minus(1, ChronoUnit.MINUTES))) {
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>(4);
			parameters.put("grant_type", "refresh_token");
			parameters.put("client_id", clientId);
			parameters.put("client_secret", clientSecret);
			parameters.put("refresh_token", refreshToken);
			token(parameters);
		}

		return accessToken;
	}

	private void token(LinkedHashMap<String, String> parameters) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		JsonNode response =
			OBJECT_MAPPER.readTree(post("oauth2/token", null, queryParams(parameters), URLENCODED_CHARSET_UTF_8));
		accessToken = response.at("/access_token").textValue();
		refreshToken = response.at("/refresh_token").textValue();
		validUntil = Instant.now().plusSeconds(response.at("/expires_in").longValue());
		LinkedHashMap<String, Serializable> result = new LinkedHashMap<String, Serializable>(5);
		result.put("access_token", getAccessToken());
		result.put("refresh_token", getRefreshToken());
		result.put("valid_until", getValidUntil().getEpochSecond());
		result.put("client_id", clientId);
		result.put("client_secret", clientSecret);
		try (FileWriter w = new FileWriter(authFile)) {
			OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(w, result);
		}
		fixFilePermissions(authFile);
	}

	private static String renderScope(Object scope) {
		return scope instanceof Collection<?> scopes
			? scopes.stream()
			.map(Object::toString)
			.collect(Collectors.joining(" "))
			: (String) scope;
	}

	private static void fixFilePermissions(File authFile) throws IOException {
		Files.setPosixFilePermissions(authFile.toPath(), Set.of(OWNER_READ, OWNER_WRITE));
	}

}
