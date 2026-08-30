package de.schoko.vcoasters.client.export.numcalcparser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Parser {

	List<Token> parseTokens(String content) {
		List<Token> tokens = new ArrayList<>();
		List<TokenType> availableTokenTypes = new ArrayList<>(List.of(TokenType.NUM, TokenType.OPEN_BRACKET));
		String currentString = "";
		int readIndex = 0;
		int contentLength = content.length();
		while (readIndex < contentLength) {
			char newChar = content.charAt(readIndex);
			String newString = currentString + newChar;
			if (newString.isBlank()) {
				currentString = "";
				readIndex++;
				continue;
			}
			TokenType availableTokenType = availableTokenTypes.getFirst(); // Just an arbitrary available token type
			availableTokenTypes.removeIf(tokenType -> !tokenType.testIfStringMatches(newString));
			if (availableTokenTypes.isEmpty()) {
				if (currentString.isEmpty()) {
					throw new IllegalArgumentException("Content contains unsupported characters");
				}
				tokens.add(new Token(currentString, availableTokenType));
				currentString = "";
				availableTokenTypes.addAll(availableTokenType.getAllowedFollowUps());
				// Reparse current character (no readIndex++)
				continue;
			}
			currentString = newString;
			readIndex++;
		}
		if (!currentString.isEmpty()) tokens.add(new Token(currentString, availableTokenTypes.getFirst()));
		return tokens;
	}

	record Token(String content, TokenType tokenType) {

	}

	enum TokenType {
		NUM("[+-]?[0-9]+(\\.[0-9]+)?"),
		ADD("\\+"),
		SUB("-"),
		MUL("\\*"),
		OPEN_BRACKET("\\("),
		CLOSING_BRACKET("\\)"),
		VALUE_REFERENCE("\\([a-z]+ [A-z/_.]+(:[A-z/_.]+)? [A-z/_.]+\\)");

		private final Pattern regex;
		private final Predicate<String> predicate;
		private List<TokenType> allowedFollowUps;

		TokenType(String regex) {
			this.regex = Pattern.compile("^" + regex + "$");
			this.predicate = this.regex.asMatchPredicate();
		}

		public Pattern getRegex() {
			return regex;
		}

		public boolean testIfStringMatches(String target) {
			return predicate.test(target);
		}

		public List<TokenType> getAllowedFollowUps() {
			if (allowedFollowUps == null) {
				allowedFollowUps = switch (this) {
					case NUM, VALUE_REFERENCE -> List.of(ADD, SUB, MUL, CLOSING_BRACKET);
					case ADD, SUB, MUL -> List.of(OPEN_BRACKET, NUM);
					case OPEN_BRACKET -> List.of(CLOSING_BRACKET, OPEN_BRACKET, NUM);
					case CLOSING_BRACKET -> List.of(CLOSING_BRACKET, ADD, SUB, MUL);
				};
			}
			return allowedFollowUps;
		}
	}

	IntermediateExpression parseExpression(List<Token> tokens) {
		if (tokens.size() == 1) {
			Token token = tokens.getFirst();
			switch (token.tokenType) {
				case NUM:
					return new IntermediateExpression.NumberExpression(token.content);
				case VALUE_REFERENCE:
					String bracketContent = token.content.substring(1, token.content.length() - 1);
					String[] split = bracketContent.split(" ");
					return new IntermediateExpression.ValueReferenceExpression(
						IntermediateExpression.ValueReferenceExpression.ReferenceType.valueOf(split[0].toUpperCase()),
						split[1], split[2]);
				default:
					throw new IllegalArgumentException("Token " + token + " can't be parsed in this position");
			}
		}

		int readIndex = 0;
		List<Token> leftGroup = new ArrayList<>();
		int delta = parseGroup(tokens, leftGroup);
		readIndex += delta;
		if (readIndex == tokens.size()) return new IntermediateExpression.BracketedExpression(parseExpression(leftGroup));
		IntermediateExpression leftExpression = parseExpression(leftGroup);
		if (readIndex > 1) leftExpression = new IntermediateExpression.BracketedExpression(leftExpression); // Left expression is definitely something in brackets
		while (readIndex < tokens.size()) {
			Token operationToken = tokens.get(readIndex);
			switch (operationToken.tokenType) {
				case ADD, SUB, MUL:
					readIndex++;
					List<Token> rightGroup = new ArrayList<>();
					delta = parseGroup(tokens.subList(readIndex, tokens.size()), rightGroup);
					readIndex += delta;
					IntermediateExpression rightExpression = parseExpression(rightGroup);
					if (delta > 1) rightExpression = new IntermediateExpression.BracketedExpression(rightExpression);
					IntermediateExpression.BiOperationExpression.OperationType operationType = switch (operationToken.tokenType) {
						case ADD -> IntermediateExpression.BiOperationExpression.OperationType.ADD;
						case SUB -> IntermediateExpression.BiOperationExpression.OperationType.SUB;
						case MUL -> IntermediateExpression.BiOperationExpression.OperationType.MUL;
						default -> null; // This is impossible
					};
					if (leftExpression instanceof IntermediateExpression.BiOperationExpression leftOperation &&
						!IntermediateExpression.BiOperationExpression.OperationType.captureLeftOperation(leftOperation.operationType(), operationType)) {
						leftExpression = new IntermediateExpression.BiOperationExpression(leftOperation.operationType(), leftOperation.leftHandOperator(),
							new IntermediateExpression.BiOperationExpression(operationType, leftOperation.rightHandOperator(), rightExpression)
						);
					} else {
						leftExpression = new IntermediateExpression.BiOperationExpression(operationType, leftExpression, rightExpression);
					}
					break;
				default:
					throw new IllegalArgumentException("Token " + operationToken + " can't be parsed in this position");
			}
		}
		return leftExpression;
	}

	int parseGroup(List<Token> tokens, List<Token> groupTokens) {
		int bracketAmount = 0;
		int tokenAmount = 0;
		for (Token token : tokens) {
			tokenAmount++;
			if (token.tokenType == TokenType.OPEN_BRACKET) {
				if (bracketAmount != 0) groupTokens.add(token);
				bracketAmount++;
			} else if (token.tokenType == TokenType.CLOSING_BRACKET) {
				bracketAmount--;
				if (bracketAmount != 0) groupTokens.add(token);
				else break;
			} else {
				groupTokens.add(token);
				if (bracketAmount == 0) break;
			}
		}
		return tokenAmount;
	}

}
