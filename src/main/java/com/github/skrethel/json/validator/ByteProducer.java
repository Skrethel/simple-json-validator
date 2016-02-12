package com.github.skrethel.json.validator;

public interface ByteProducer {

	int get();

	int peek();

	boolean isDone();
}
