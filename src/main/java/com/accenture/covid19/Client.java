package com.accenture.covid19;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

public class Client {
	public final static String ALL = "All";
	private final static String COUNTRY_PFX = "@";
	private final static String API = "https://covid-api.mmediagroup.fr/v1";

	private final HttpClient http;
	private Boolean all;
	private boolean failSafe = true;

	Client(HttpClient http) {
		this.http = http;
	}

	public boolean isFailSafe() {
		return failSafe;
	}

	public void setFailSafe(boolean failSafe) {
		this.failSafe = failSafe;
	}

	public Boolean isAll() {
		return all;
	}

	public void setAll(Boolean all) {
		this.all = all;
	}

	private static String getCheckedResponse(HttpResponse<String> resp) throws CovidException {
		int code = resp.statusCode();
		if (code != 200) {
			throw new CovidException("Failure response " + code + ": " + resp.body());
		}
		return resp.body();
	}

	private Any toJson(HttpRequest request) throws IOException, InterruptedException {
		HttpResponse<String> resp = http.send(request, BodyHandlers.ofString());
		String jsonStr = getCheckedResponse(resp);
		Any ret = JsonIterator.deserialize(jsonStr);
		if (ret.size() < 1)
			throw new CovidException("Empty response " + ret + " can not be processed for " + request);
		return ret;
	}

	private Any fetchJson(String endPoint) {
		try {
			HttpRequest request = HttpRequest.newBuilder() //
					.uri(new URI(API + endPoint)).GET().build();
			return toJson(request);
		} catch (IOException | URISyntaxException | InterruptedException ex) {
			throw new CovidException("Unable to use " + endPoint, ex);
		}
	}

	private Map<String, Any> toMap(Any resp, String singleCountry) {
		Map<String, Any> map;
		if (singleCountry == null)
			map = resp.asMap();
		else {
			map = new HashMap<String, Any>();
			map.put(singleCountry, resp);
		}
		return map;
	}

	public double coefficient(String spec) {
		String arg = "?";
		boolean mainCountriesRoot = true;
		if (spec == null || spec.isEmpty()) {
			// All countries
		} else if (spec.startsWith(COUNTRY_PFX)) {
			arg += "country=" + spec.substring(1);
			mainCountriesRoot = false;
		} else if (spec.length() == 2) {
			arg += "ab=" + spec; // I checked there is no country with 2 characters
			mainCountriesRoot = false;
		} else {
			arg += "continent=" + spec; // A continent can be given
		}
		double population = 0;
		boolean all = this.all == null ? mainCountriesRoot : this.all;
		String endPoint = "/cases" + arg;
		Any resp = fetchJson(endPoint);
		String singleCountry = mainCountriesRoot ? null : resp.get(ALL, "country").as(String.class);
		Map<String, Any> mapDeaths = toMap(resp, singleCountry);
		endPoint = "/vaccines" + arg;
		resp = fetchJson(endPoint);
		Map<String, Any> mapVaccines = toMap(resp, singleCountry);
		if (failSafe) {
			// Only countries reported by both endpoints:
			mapDeaths.keySet().retainAll(mapVaccines.keySet());
		}
		// First get the total population and the key order to make sure we relate the
		// same numbers:
		Map<String, Double> deaths = new LinkedHashMap<String, Double>();
		List<String> mainCountriesList = new LinkedList<String>(); // To ensure order
		List<String> individualCountriesList = new LinkedList<String>(); // To ensure order
		for (Map.Entry<String, Any> mainCountry : mapDeaths.entrySet()) {
			mainCountriesList.add(mainCountry.getKey());
			population += mainCountry.getValue().get(ALL, "population").as(Double.class);
			addCountriesSamples(mainCountry.getValue(), mainCountry.getKey(), "deaths", deaths, all, individualCountriesList, true);
		}
		Map<String, Double> vaccinated = new LinkedHashMap<String, Double>();
		for (String countryName : mainCountriesList) {
			Any mainCountry = mapVaccines.get(countryName);
			addCountriesSamples(mainCountry, countryName, "people_vaccinated", vaccinated, all, individualCountriesList, false);
		}
		try {
			PearsonsCorrelation pc = new PearsonsCorrelation();
			if(failSafe) {
				// Also for individual countries we get the common set
				deaths.keySet().retainAll(vaccinated.keySet());
				vaccinated.keySet().retainAll(deaths.keySet());
			}
			return pc.correlation(toPercentages(deaths.values(), population), toPercentages(vaccinated.values(), population));
		} catch (MathIllegalArgumentException mia) {
			throw new CovidException(
					"Unable to process the results of " + arg + " using " + (all ? "all" : "individual countries") + " with " + this,
					mia);
		}
	}

	private double[] toPercentages(Collection<Double> list, double population) {
		double[] ret = new double[list.size()];
		int i = 0;
		for (Double d : list) {
			ret[i++] = d * 100 / population;
		}
		return ret;
	}

	private void addCountriesSamples(Any json, String country, String name, Map<String, Double> samples, boolean all,
			List<String> indivCountries, boolean add) {
		if (json == null)
			return;
		double cnt;
		for (Map.Entry<String, Any> e : json.asMap().entrySet()) {
			if (!all && e.getKey().equals(ALL))
				continue;
			if (!add && !indivCountries.contains(e.getKey()))
				continue;
			cnt = e.getValue().get(name).as(Double.class);
			samples.put(country, cnt);
			if (add)
				indivCountries.add(e.getKey());
			if (all && e.getKey().equals(ALL))
				break;
		}
	}

	@Override
	public String toString() {
		return "Client [all=" + all + ", failSafe=" + failSafe + "]";
	}
}
