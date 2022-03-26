package com.accenture.covid19;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.Proxy;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.math3.exception.MathIllegalArgumentException;

/**
 * An application that calculates the correlation coefficient between the
 * percentage of people that died and got vaccinated of COVID-19 given a
 * continent or all available countries.
 */
public class App {
	private final static String STDIN = "-";
	private final static String SAFE = "Safe";
	private static App app;

	public static void main(String[] args) throws IOException {
		app = new App();
		if (args.length == 0) {
			app.readStdin();
		}
		for (String arg : args) {
			if (STDIN.equals(arg)) {
				app.readStdin();
			} else {
				app.process(arg);
			}
		}
	}

	private Client client;

	protected void process(String spec) {
		if (Client.ALL.equalsIgnoreCase(spec)) {
			Boolean all = client().isAll();
			if (all == null)
				client.setAll(false);
			else
				client.setAll(!all);
			System.out.println("Using all: " + client.isAll());
		} else if (SAFE.equalsIgnoreCase(spec)) {
			client.setFailSafe(!client.isFailSafe());
			System.out.println("Using failSafe: " + client.isFailSafe());
		} else
			try {
				double coeff = client().coefficient(spec);
				System.out.println(spec + "\t" + coeff);
			} catch (CovidException ex) {
				ex.printStackTrace(); // Just to keep processing input
			}
	}

	private static boolean isEndStdIn(String ln) {
		return ln == null || STDIN.equals(ln);
	}

	private void readStdin() throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			String ln;
			while (!isEndStdIn(ln = br.readLine())) {
				process(ln);
			}
		}
	}

	public Client client() {
		if (client == null) {
			HttpClient.Builder clientBuilder = HttpClient.newBuilder().version(Version.HTTP_1_1)
					.followRedirects(Redirect.NORMAL).connectTimeout(Duration.ofSeconds(20));
			client = new Client(clientBuilder.build());
		}
		return client;
	}

}
