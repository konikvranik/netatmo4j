package netatmo.client;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class ConnectionException extends Exception {
	public ConnectionException(HttpURLConnection connection) throws IOException {
		super(StringUtils.isEmpty(connection.getResponseMessage()) ? connection.getResponseMessage() : IOUtils.toString(connection.getErrorStream()));
		this.connection = connection;
	}

	public final HttpURLConnection getConnection() {
		return connection;
	}

	private final HttpURLConnection connection;
}
