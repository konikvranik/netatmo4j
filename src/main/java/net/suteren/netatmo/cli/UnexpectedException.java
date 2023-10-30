package net.suteren.netatmo.cli;

public class UnexpectedException extends RuntimeException {
	public UnexpectedException(Exception e) {
		super(e);
	}
}
