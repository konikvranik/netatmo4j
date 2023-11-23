package net.suteren.netatmo.therm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.client.AbstractApiClient;
import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.client.NetatmoResponse;
import net.suteren.netatmo.domain.therm.HomesData;

/**
 * Netatmo API client to retrieve the home data.
 */
public final class HomeClient extends AbstractApiClient {
	public HomeClient(AuthClient auth) {
		super(auth);
	}

	public HomesData getHomesData() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return OBJECT_MAPPER.treeToValue(OBJECT_MAPPER.readValue(get("homesdata"), NetatmoResponse.class).body(), HomesData.class);
	}

	public JsonNode getStatus(java.lang.String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return OBJECT_MAPPER.readTree(get("homestatus", Map.of("home_id", homeId)));
	}

}
