package net.suteren.netatmo.client;

import java.io.Serializable;

public record NetatmoError(NetatmoErrorInfo error) {

	public static record NetatmoErrorInfo(
		Long code,
		String message
	) implements Serializable {}
}
