package com.github.skrethel.json.validator;

// TODO add position indicator to ParseException
public class JsonValidator {

	private static final int LEFT_CURLY = 0x7b;
	private static final int RIGHT_CURLY = 0x7d;
	private static final int LEFT_SQUARE = 0x5b;
	private static final int RIGHT_SQUARE = 0x5d;
	private static final int BACKSLASH = 0x5c;
	private static final int SLASH = 0x2f;
	private static final int COLON = 0x3A;
	private static final int COMMA = 0x2C;
	private static final int MINUS = 0x2D;
	private static final int PLUS = 0x2b;
	private static final int DOT = 0x2e;
	private static final int QUOTATION = 0x22;
	private static final int SMALL_T = 0x74;
	private static final int SMALL_R = 0x72;
	private static final int SMALL_U = 0x75;
	private static final int SMALL_E = 0x65;
	private static final int SMALL_F = 0x66;
	private static final int SMALL_A = 0x61;
	private static final int SMALL_L = 0x6c;
	private static final int SMALL_S = 0x73;
	private static final int SMALL_N = 0x6e;
	private static final int SMALL_B = 0x62;
	private static final int BIG_E = 0x45;
	private static final int ZERO = 0x30;
	private static final int NINE = 0x39;

	public void validate(String input) {
		StringByteProducer source = new StringByteProducer(input);
		validate0(source);
	}

	private void validate0(StringByteProducer source) {
		try {
			consume(source);
			if (!source.isDone()) {
				throw new ParserException("Trailing data found");
			}
		} catch (ByteStreamEndException e) {
			throw new ParserException("Unexpected end of data");
		}
	}

	private void consume(ByteProducer source) {
		consumeWhiteSpace(source);
		if (isLeftCurlyBracket(source)) {
			consumeObject(source);
		} else if (isLeftSquareBracket(source)) {
			consumeArray(source);
		} else {
			throw new ParserException("Json top level entity must be either object or array");
		}
		try {
			consumeWhiteSpace(source);
		} catch (ByteStreamEndException e) {
			// ignore trailing white spaces
		}
	}

	private void consumeArray(ByteProducer source) {
		check(source.get(), LEFT_SQUARE, "Array must start with [");
		boolean noValue = true;
		do {
			consumeWhiteSpace(source);
			if (source.peek() == RIGHT_SQUARE) {
				if (noValue) {
					break;
				} else {
					throw new ParserException("Array cannot have trailing commas inside");
				}
			}
			consumeValue(source);
			noValue = false;
			consumeWhiteSpace(source);
		} while (hasNextItem(source));
		check(source.get(), RIGHT_SQUARE, "Array must end with ]");
	}

	private void consumeObject(ByteProducer source) {
		check(source.get(), LEFT_CURLY, "Object must start with {");
		boolean noValue = true;
		do {
			consumeWhiteSpace(source);
			if (source.peek() == RIGHT_CURLY) {
				if (noValue) {
					break;
				} else {
					throw new ParserException("Object cannot have trailing commas inside");
				}
			}
			consumeString(source);
			consumeWhiteSpace(source);
			checkColon(source, "Name value pair in object must be separated with :");
			consumeWhiteSpace(source);
			consumeValue(source);
			noValue = false;
			consumeWhiteSpace(source);
		} while (hasNextItem(source));
		check(source.get(), RIGHT_CURLY, "Object must end with }");
	}

	private void check(int character, int expected, String errorMessage) {
		if (character != expected) {
			throw new ParserException(errorMessage);
		}
	}

	private void checkColon(ByteProducer source, String errorMessage) {
		int character = source.get();
		if (!isColon(character)) {
			throw new ParserException(errorMessage);
		}
	}

	private void consumeValue(ByteProducer source) {
		int character = source.peek();
		if (isLeftCurlyBracket(character)) {
			consumeObject(source);
		} else if (isLeftSquareBracket(character)) {
			consumeArray(source);
		} else if (isTrue(character)) {
			consumeTrue(source);
		} else if (isFalse(character)) {
			consumeFalse(source);
		} else if (isNull(character)) {
			consumeNull(source);
		} else if (isQuotation(character)) {
			consumeString(source);
		} else if (isNumber(character)) {
			consumeNumber(source);
		} else {
			throw new ParserException("Invalid value");
		}
	}

	private void consumeNumber(ByteProducer source) {
		int character = source.get();
		if (character == MINUS) {
			// skip sign
			character = source.get();
		}
		if (character > ZERO && character <= NINE) {
			consumeDigits(source);
		} else {
			if (character != ZERO) {
				throw new ParserException("Invalid character in number");
			} else {
				if (isDigit(source.peek())) {
					throw new ParserException("Superfluous leading zero");
				}
			}
		}
		character = source.peek();
		if (character == DOT) {
			source.get();
			consumeDigits(source);
		}
		character = source.peek();
		if (character == SMALL_E || character == BIG_E) {
			consumeExp(source);
		}
	}

	private void consumeExp(ByteProducer source) {
		int character = source.get();
		if (character != SMALL_E && character != BIG_E) {
			throw new ParserException("Expected a literal in exponential number");
		}
		character = source.get();
		if (character == MINUS || character == PLUS) {
			// skip sign
			character = source.get();
		}
		if (!isDigit(character)) {
			throw new ParserException("Missing digits in exponent");
		}
		consumeDigits(source);
	}

	private void consumeDigits(ByteProducer source) {
		while(true) {
			if (!(isDigit(source.peek()))) {
				break;
			}
			source.get();
		}
	}

	private boolean isDigit(int character) {
		return (character >= ZERO && character <= NINE);
	}

	private boolean isNumber(int character) {
		return character == MINUS || (character >= ZERO && character <= NINE);
	}

	private boolean isNull(int character) {
		return character == SMALL_N;
	}

	private boolean isFalse(int character) {
		return character == SMALL_F;
	}

	private void consumeTrue(ByteProducer source) {
		String errorMessage = "Invalid true value";
		check(source.get(), SMALL_T, errorMessage);
		check(source.get(), SMALL_R, errorMessage);
		check(source.get(), SMALL_U, errorMessage);
		check(source.get(), SMALL_E, errorMessage);
	}

	private void consumeNull(ByteProducer source) {
		String errorMessage = "Invalid null value";
		check(source.get(), SMALL_N, errorMessage);
		check(source.get(), SMALL_U, errorMessage);
		check(source.get(), SMALL_L, errorMessage);
		check(source.get(), SMALL_L, errorMessage);
	}

	private void consumeFalse(ByteProducer source) {
		String errorMessage = "Invalid false value";
		check(source.get(), SMALL_F, errorMessage);
		check(source.get(), SMALL_A, errorMessage);
		check(source.get(), SMALL_L, errorMessage);
		check(source.get(), SMALL_S, errorMessage);
		check(source.get(), SMALL_E, errorMessage);
	}

	private boolean isTrue(int character) {
		return character == SMALL_T;
	}

	private boolean isColon(int character) {
		return character == COLON;
	}

	private boolean hasNextItem(ByteProducer source) {
		if (source.peek() == COMMA) {
			source.get();
			return true;
		}
		return false;
	}

	private void consumeString(ByteProducer source) {
		int character = source.get();
		if (!isQuotation(character)) {
			throw new ParserException("String must start with \"");
		}
		while (true) {
			character = source.peek();
			if (isQuotation(character)) {
				source.get();
				break;
			} else if (isBackSlash(character)) {
				consumeEscaped(source);
			} else if (isControl(character)) {
				throw new ParserException("Control characters are not allowed in strings");
			} else {
				// eat it
				source.get();
			}
		}
	}

	private boolean isControl(int character) {
		return character >= 0x0 && character <= 0x1F;
	}

	private void consumeEscaped(ByteProducer source) {
		check(source.get(), BACKSLASH, "Escape sequence must start with \\");
		int character = source.peek();
		if (character == QUOTATION || character == BACKSLASH || character == SLASH ||
			character == SMALL_B || character == SMALL_F ||
			character == SMALL_N || character == SMALL_R ||
			character == SMALL_T) {
			source.get();
		} else if (character == SMALL_U) {
			checkHex(source, "Invalid hex in \\u escaped sequence");
			checkHex(source, "Invalid hex in \\u escaped sequence");
			checkHex(source, "Invalid hex in \\u escaped sequence");
			checkHex(source, "Invalid hex in \\u escaped sequence");
		} else {
			throw new ParserException("Invalid escape sequence in string");
		}
	}

	private void checkHex(ByteProducer source, String errorMessage) {
		int character = source.get();
		if  (!(character >= ZERO && character <= NINE) || (character >= 0x41 && character <= 0x46) || (character >= 0x61 && character <= 0x66)) {
			throw new ParserException(errorMessage);
		}
	}

	private boolean isBackSlash(int character) {
		return character == BACKSLASH;
	}

	private void consumeWhiteSpace(ByteProducer source) {
		while (true) {
			int character = source.peek();
			if (isWhiteSpace(character)) {
				source.get();
			} else {
				break;
			}
		}
	}

	private static boolean isWhiteSpace(int character) {
		return character == 0x9 || character == 0xA || character == 0xD || character == 0x20;
	}

	private static boolean isLeftSquareBracket(ByteProducer source) {
		return isLeftSquareBracket(source.peek());
	}

	private static boolean isLeftSquareBracket(int character) {
		return character == LEFT_SQUARE;
	}

	private static boolean isLeftCurlyBracket(ByteProducer source) {
		return isLeftCurlyBracket(source.peek());
	}

	private static boolean isLeftCurlyBracket(int character) {
		return character == LEFT_CURLY;
	}

	private static boolean isQuotation(int character) {
		return character == QUOTATION;
	}

}
