package com.accenture.covid19;

public class CovidException extends RuntimeException {
	private static final long serialVersionUID = 453447L;

	CovidException(String what) {
		super(what);
	}
	CovidException(String what, Throwable proto) {
		super(what, proto);
	}
}
