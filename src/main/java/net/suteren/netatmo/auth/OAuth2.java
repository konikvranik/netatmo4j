package net.suteren.netatmo.auth;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sun.net.httpserver.HttpServer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for Oauth2 callback server handling and opening of the URL in the browser.
 * It is intended just for internal use.
 */
@Slf4j final class OAuth2 {
	private final CountDownLatch latch = new CountDownLatch(1);
	@Setter @Getter private String code;
	@Setter @Getter private String redirectUri;

	void authorize(UnaryOperator<String> getUrl) throws URISyntaxException, IOException, InterruptedException {
		final HttpServer server = startServer();
		redirectUri = "http://localhost:%d/".formatted(server.getAddress().getPort());
		String authUrl = getUrl.apply(redirectUri);
		log.info("Authorize the app in the browser...\nIf it did not happen automatically, open following URL in your browser: {}", authUrl);
		Desktop.getDesktop().browse(new URI(authUrl));
		latch.await();
	}

	private HttpServer startServer() throws IOException {
		final HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", http -> {

			Map<String, List<String>> params = Arrays.stream(http.getRequestURI().getRawQuery().split("&"))
				.map(s -> s.split("=", 2))
				.filter(it -> it.length > 1)
				.collect(Collectors.groupingBy(it -> it[0],
					Collectors.mapping(it -> it[1],
						Collectors.mapping(it -> URLDecoder.decode(it, StandardCharsets.UTF_8),
							Collectors.toList()))));

			code = params.get("code")
				.stream()
				.findAny()
				.orElse(null);

			if (StringUtils.isNoneBlank(code)) {
				http.sendResponseHeaders(200, 0);
				http.getResponseHeaders().add("Content-type", "text/plain");
				new PrintWriter(http.getResponseBody()).println("Go back to the application.");
				http.getResponseBody().close();
			} else {
				http.getResponseHeaders().add("Content-type", "text/plain");
				http.sendResponseHeaders(500, 0);
				new PrintWriter(http.getResponseBody()).println("No code was posted!");
			}

			server.stop(0);
			latch.countDown();
		});
		server.start();
		return server;
	}
}
