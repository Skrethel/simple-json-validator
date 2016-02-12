package com.github.skrethel.json.validator;

import org.junit.Test;

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.fail;


public class JsonParserTest {

	private String loadInput(String name) {
		InputStream stream = JsonParserTest.class.getResourceAsStream("/" + name);
		if (stream == null) {
			return null;
		}
		return new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
	}

	@Test
	public void testValid() throws Exception {
		for (int i = 1; i <= 20; i++) {
			String name = "v" + i + ".json";
			String input = loadInput(name);
			if (input == null) {
				continue;
			}
			System.out.println("Testing " + name);
			new JsonValidator().validate(input);
		}
	}

	@Test
	public void testInvalid() throws Exception {
		for (int i = 1; i <= 20; i++) {
			String name = "i" + i + ".json";
			String input = loadInput(name);
			if (input == null) {
				continue;
			}
			System.out.println("Testing " + name);
			try {
				new JsonValidator().validate(input);
				fail("Test case " + name + " should fail validation but it didn't");
			} catch (ParserException e) {
				// ignore
				System.out.println("File " + name + " failed with error: " + e);
			}
		}
	}

	@Test
	public void testBig() throws Exception {
		String input = loadInput("big.json");
		long start = System.currentTimeMillis();
		new JsonValidator().validate(input);
		long end = System.currentTimeMillis();
		System.out.println("Time taken: " + (end - start));
	}

}
