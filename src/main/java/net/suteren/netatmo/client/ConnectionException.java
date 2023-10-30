package net.suteren.netatmo.client;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter public class ConnectionException extends Exception {

	private final transient  HttpURLConnection connection;
	private final NetatmoError.NetatmoErrorInfo errorInfo;

	public ConnectionException(HttpURLConnection connection, NetatmoError.NetatmoErrorInfo errorInfo) throws IOException {
		super(StringUtils.isEmpty(errorInfo.message()) ? connection.getResponseMessage() : errorInfo.message());
		this.connection = connection;
		this.errorInfo = errorInfo;
	}
}
