package com.github.skrethel.json.validator;

public class StringByteProducer implements ByteProducer {

	private final String input;
	private final int end;
	private int current;

	public StringByteProducer(String input) {
		this.input = input;
		this.end = input.length();
		this.current = 0;
	}

	@Override
	public int get() {
		int character = getFromSource();
		return character;
	}

	private int getFromSource() {
		int ret = getCharacter();
		current += Character.charCount(ret);
		return ret;
	}

	private int getCharacter() {
		if (current >= end) {
			throw new ByteStreamEndException();
		}
		return input.codePointAt(current);
	}

	@Override
	public int peek() {
		return getCharacter();
	}

	@Override
	public boolean isDone() {
		return current >= end;
	}

}
