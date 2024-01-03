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

	public static final String CLIENT_ID_PARAM_NAME = "client_id";
	public static final String REDIRECT_URI_PARAM_NAME = "redirect_uri";
	public static final String SCOPE_PARAM_NAME = "scope";
	public static final String STATE_PARAM_NAME = "state";
	public static final String CLIENT_SECRET_PARAM_NAME = "client_secret";
	public static final String REFRESH_TOKEN_PARAM_NAME = "refresh_token";
	public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
	public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
	private final String clientId;
	private final Collection<Scope> scope;
	private final String state;
	@Setter @Getter private String accessToken;
	@Setter @Getter private String refreshToken;
	@Setter @Getter private Instant validUntil;
	private final String clientSecret;
	private final OAuth2 oauth;
	private final File authFile;

	/**
	 * Retrieves OAUTH2 token.
	 * It is required by {@link net.suteren.netatmo.client.AbstractApiClient} to provide valid `Authentication` token.
	 * See <a href="https://dev.netatmo.com/apidocumentation/oauth">Netatmo Authentication</a> for more details.
	 *
	 * @param clientId     `client_id` used to retrieve the authentication token.
	 * @param clientSecret `client_secret` used to retrieve the authentication token.
	 * @param scope        scopes to authorize to.
	 *                     If no scope is provided during the token request, the default is {@link Scope#READ_STATION}
	 * @param state        to prevent Cross-site Request Forgery.
	 * @param authFile     local file to store tokens to. This file should be protected from unauthorized reading.
	 * @throws IOException in case of connection problems of problems accessing the `authFile`.
	 */
	public AuthClient(String clientId, String clientSecret, Collection<Scope> scope, String state, File authFile) throws IOException {
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

	/**
	 * Entrance URL to log in and approve the client.
	 *
	 * @param redirectUri url of this application to pass the auth code to.
	 * @return URL to open in the browser.
	 */
	public String authorizeUrl(String redirectUri) {
		LinkedHashMap<String, String> map = LinkedHashMap.newLinkedHashMap(4);
		map.put(CLIENT_ID_PARAM_NAME, clientId);
		map.put(REDIRECT_URI_PARAM_NAME, redirectUri);
		map.put(SCOPE_PARAM_NAME, renderScope(scope));
		map.put(STATE_PARAM_NAME, state);
		return constructUrl("oauth2/authorize", map);
	}

	/**
	 * Retrieves the authorization token from cache, and try to refresh it in case it is going to expire.
	 * If there is no authorization token in the cache, start OAuth2 process to log in and retrieve the token.
	 *
	 * @return authorization token
	 * @throws URISyntaxException  if {@link #authorizeUrl(String)} return wrong URL.
	 * @throws IOException         in the case of local callback server issues.
	 * @throws ConnectionException in case of error from Netatmo API server during retrieving the token.
	 */
	public String getToken() throws URISyntaxException, IOException, InterruptedException, ConnectionException {
		if (accessToken == null) {
			oauth.authorize(this::authorizeUrl);
			LinkedHashMap<String, String> parameters =  LinkedHashMap.newLinkedHashMap(6);
			parameters.put(GRANT_TYPE_PARAM_NAME, "authorization_code");
			parameters.put(CLIENT_ID_PARAM_NAME, clientId);
			parameters.put(CLIENT_SECRET_PARAM_NAME, clientSecret);
			parameters.put("code", oauth.getCode());
			parameters.put(REDIRECT_URI_PARAM_NAME, oauth.getRedirectUri());
			parameters.put(SCOPE_PARAM_NAME, renderScope(scope));
			token(parameters);
		} else if (validUntil.isBefore(Instant.now().minus(1, ChronoUnit.MINUTES))) {
			LinkedHashMap<String, String> parameters =  LinkedHashMap.newLinkedHashMap(4);
			parameters.put(GRANT_TYPE_PARAM_NAME, REFRESH_TOKEN_PARAM_NAME);
			parameters.put(CLIENT_ID_PARAM_NAME, clientId);
			parameters.put(CLIENT_SECRET_PARAM_NAME, clientSecret);
			parameters.put(REFRESH_TOKEN_GRANT_TYPE, refreshToken);
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
		LinkedHashMap<String, Serializable> result =  LinkedHashMap.newLinkedHashMap(5);
		result.put("access_token", getAccessToken());
		result.put(REFRESH_TOKEN_GRANT_TYPE, getRefreshToken());
		result.put("valid_until", getValidUntil().getEpochSecond());
		result.put(CLIENT_ID_PARAM_NAME, clientId);
		result.put(CLIENT_SECRET_PARAM_NAME, clientSecret);
		try (FileWriter w = new FileWriter(authFile)) {
			OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(w, result);
		}
		fixFilePermissions(authFile);
	}

	private static String renderScope(Collection<Scope> scope) {
		return scope.stream()
			.map(Object::toString)
			.map(String::toLowerCase)
			.collect(Collectors.joining(" "));
	}

	private static void fixFilePermissions(File authFile) throws IOException {
		Files.setPosixFilePermissions(authFile.toPath(), Set.of(OWNER_READ, OWNER_WRITE));
	}

	public enum Scope {
		/**
		 * to retrieve weather station data (Getstationsdata, Getmeasure)
		 */
		READ_STATION,

		/**
		 * to retrieve thermostat data ( Homestatus, Getroommeasure...)
		 */
		READ_THERMOSTAT,

		/**
		 * to set up the thermostat (Synchomeschedule, Setroomthermpoint...)
		 */
		WRITE_THERMOSTAT,

		/**
		 * to retrieve Smart Indoor Cameradata (Gethomedata, Getcamerapicture...)
		 */
		READ_CAMERA,

		/**
		 * to inform the Smart Indoor Camera that a specific person or everybody has left the Home (Setpersonsaway, Setpersonshome)
		 */
		WRITE_CAMERA,

		/**
		 * to access the camera, the videos and the live stream.
		 * Netatmo cares a lot about users privacy and security. The "access" scope grants you access to sensitive data and is delivered by Netatmo teams on a per-app basis. To submit an access scope request, see <a href="https://dev.netatmo.com/request-scope-form">here</a>.
		 */
		ACCESS_CAMERA,

		/**
		 * to retrieve Smart Outdoor Camera data (Gethomedata, Getcamerapicture...)
		 */
		READ_PRESENCE,

		/**
		 * to access the camera, the videos and the live stream.
		 * Netatmo cares a lot about users privacy and security. The "access" scope grants you access to sensitive data and is delivered by Netatmo teams on a per-app basis. To submit an access scope request, see <a href="https://dev.netatmo.com/request-scope-form">here</a>.
		 */
		ACCESS_PRESENCE,

		/**
		 * to retrieve the Smart Smoke Alarm informations and events (Gethomedata, Geteventsuntil...)
		 */
		READ_SMOKEDETECTOR,

		/**
		 * to read data coming from Smart Indoor Air Quality Monitor (gethomecoachsdata)
		 */
		READ_HOMECOACH,
	}
}
