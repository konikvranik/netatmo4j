package net.suteren.netatmo.client;

import java.io.Serializable;

/**
 * Java representation of the generic Netatmo API error.
 *
 * @param error detail.
 */
public record NetatmoError(NetatmoErrorInfo error) {

	/**
	 * Content of the Netatmo API error message.
	 *
	 * @param code error code. See the list of documented error codes at <a href="https://dev.netatmo.com/apidocumentation/general#status-ok">Netatmo API Glossary</a>.
	 * @param message error message.
	 */
	public static record NetatmoErrorInfo(
		Long code,
		String message
	) implements Serializable {}
}
