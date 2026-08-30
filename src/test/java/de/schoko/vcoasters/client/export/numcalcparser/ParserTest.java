package de.schoko.vcoasters.client.export.numcalcparser;

import de.schoko.vcoasters.client.export.numcalcparser.Parser.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ParserTest {
	@Test
	void testAllTokenTypesMatch() {
		Assertions.assertTrue(TokenType.ADD.testIfStringMatches("+"));
		Assertions.assertTrue(TokenType.SUB.testIfStringMatches("-"));
		Assertions.assertTrue(TokenType.MUL.testIfStringMatches("*"));
		Assertions.assertTrue(TokenType.OPEN_BRACKET.testIfStringMatches("("));
		Assertions.assertTrue(TokenType.CLOSING_BRACKET.testIfStringMatches(")"));
		Assertions.assertTrue(TokenType.VALUE_REFERENCE.testIfStringMatches("(score a b)"));
		Assertions.assertTrue(TokenType.VALUE_REFERENCE.testIfStringMatches("(score a.b.c.d b_ddasd)"));
		Assertions.assertTrue(TokenType.VALUE_REFERENCE.testIfStringMatches("(storage a:b foo/bar.abc/crux)"));
	}

	private void checkIfEveryTypeMatches(List<Parser.Token> parseResults, List<TokenType> expectedTypes) {
		Assertions.assertEquals(parseResults.size(), expectedTypes.size());
		for (int i = 0; i < parseResults.size(); i++) {
			Assertions.assertEquals(parseResults.get(i).tokenType(), expectedTypes.get(i));
		}
	}

	@Test
	void testIfSimpleExpressionsGetTokenized() {
		Parser parser = new Parser();
		checkIfEveryTypeMatches(parser.parseTokens("1 + 2"), List.of(TokenType.NUM, TokenType.ADD, TokenType.NUM));
		checkIfEveryTypeMatches(parser.parseTokens("1 + 2 + 3"), List.of(TokenType.NUM, TokenType.ADD, TokenType.NUM, TokenType.ADD, TokenType.NUM));
	}

	@Test
	void testIfSimpleIncorrectExpressionsFail() {
		Parser parser = new Parser();
		Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parseTokens("abc"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parseTokens("1 + a"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parseTokens("+ + + + +"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parseTokens(")"));
	}

	@Test
	void testIfOneTokenExpressionsParse() {
		Parser parser = new Parser();
		Assertions.assertEquals(new IntermediateExpression.NumberExpression("1"), parser.parseExpression(List.of(new Parser.Token("1", TokenType.NUM))));
		Assertions.assertEquals(new IntermediateExpression.NumberExpression("15"), parser.parseExpression(List.of(new Parser.Token("15", TokenType.NUM))));
		Assertions.assertEquals(new IntermediateExpression.NumberExpression("123"), parser.parseExpression(List.of(new Parser.Token("123", TokenType.NUM))));
		Assertions.assertEquals(new IntermediateExpression.ValueReferenceExpression(IntermediateExpression.ValueReferenceExpression.ReferenceType.SCORE, "a", "b"), parser.parseExpression(List.of(new Parser.Token("(score a b)", TokenType.VALUE_REFERENCE))));
	}

	private IntermediateExpression parse(Parser parser, String content) {
		return parser.parseExpression(parser.parseTokens(content));
	}

	@Test
	void testIfSimpleCalculationsResolveAValidTypeParse() {
		Parser parser = new Parser();
		Assertions.assertEquals(
			new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.ADD,
				new IntermediateExpression.NumberExpression("1"),
				new IntermediateExpression.NumberExpression("2")
			),
			parse(parser, "1 + 2"));
		Assertions.assertEquals(
			new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.ADD,
				new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.ADD,
					new IntermediateExpression.NumberExpression("1"),
					new IntermediateExpression.NumberExpression("2")
				),
				new IntermediateExpression.NumberExpression("3")
			),
			parse(parser, "1 + 2 + 3"));
	}

	@Test
	void testIfPEMDASRelevantCalculationsParseCorrectly() {
		Parser parser = new Parser();
		Assertions.assertEquals(
			new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.ADD,
				new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.MUL,
					new IntermediateExpression.NumberExpression("1"),
					new IntermediateExpression.NumberExpression("2")
				),
				new IntermediateExpression.NumberExpression("3")
			),
			parse(parser, "1 * 2 + 3"));
		Assertions.assertEquals(
			new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.ADD,
				new IntermediateExpression.NumberExpression("1"),
				new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.MUL,
					new IntermediateExpression.NumberExpression("2"),
					new IntermediateExpression.NumberExpression("3")
				)
			),
			parse(parser, "1 + 2 * 3"));
		Assertions.assertEquals(
			new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.MUL,
				new IntermediateExpression.BracketedExpression(
					new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.ADD,
						new IntermediateExpression.NumberExpression("1"),
						new IntermediateExpression.NumberExpression("2")
					)
				),
				new IntermediateExpression.NumberExpression("3")
			),
			parse(parser, "(1 + 2) * 3"));
		Assertions.assertEquals(
			new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.MUL,
				new IntermediateExpression.NumberExpression("3"),
				new IntermediateExpression.BracketedExpression(
					new IntermediateExpression.BiOperationExpression(IntermediateExpression.BiOperationExpression.OperationType.ADD,
						new IntermediateExpression.NumberExpression("1"),
						new IntermediateExpression.NumberExpression("2")
					)
				)
			),
			parse(parser, "3 * (1 + 2)"));
	}
}
