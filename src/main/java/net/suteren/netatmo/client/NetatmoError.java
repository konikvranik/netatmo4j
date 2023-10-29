package net.suteren.netatmo.client;

public record NetatmoError(NetatmoErrorInfo error) {

	public static record NetatmoErrorInfo(
		Long code,
		String message
	) {}
}
