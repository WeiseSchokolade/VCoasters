package de.schoko.vcoasters.client.export.numcalcparser;


public interface IntermediateExpression {
	NumberProviderExpression toNumberProvider();

	record NumberExpression(String value) implements IntermediateExpression {
		@Override
		public NumberProviderExpression toNumberProvider() {
			return new NumberProviderExpression.NumberExpression(value);
		}
	}

	record BiOperationExpression(OperationType operationType, IntermediateExpression leftHandOperator, IntermediateExpression rightHandOperator) implements IntermediateExpression {
		@Override
		public NumberProviderExpression toNumberProvider() {
			return switch (operationType) {
				case ADD -> new NumberProviderExpression.SumExpression(leftHandOperator.toNumberProvider(), rightHandOperator.toNumberProvider());
				case MUL -> new NumberProviderExpression.ProductExpression(leftHandOperator.toNumberProvider(), rightHandOperator.toNumberProvider());
				case SUB ->
					new NumberProviderExpression.SumExpression(
						leftHandOperator.toNumberProvider(),
						new NumberProviderExpression.ProductExpression(
							rightHandOperator.toNumberProvider(),
							new NumberProviderExpression.NumberExpression("-1")
						)
					);
			};
		}

		enum OperationType {
			ADD,
			SUB,
			MUL;

			public static boolean captureLeftOperation(OperationType leftType, OperationType rightType) {
				return switch (rightType) {
					case ADD, SUB -> true;
					case MUL -> {
						if (leftType == ADD || leftType == SUB) yield false;
						yield true;
					}
				};
			}
		}
	}

	record ValueReferenceExpression(ReferenceType referenceType, String container, String path) implements IntermediateExpression {
		@Override
		public NumberProviderExpression toNumberProvider() {
			return switch (referenceType) {
				case SCORE -> new NumberProviderExpression.ScoreReferenceExpression(container, path);
				case STORAGE -> new NumberProviderExpression.StorageReferenceExpression(container, path);
			};
		}

		enum ReferenceType {
			SCORE,
			STORAGE
		}
	}

	record BracketedExpression(IntermediateExpression content) implements IntermediateExpression {
		@Override
		public NumberProviderExpression toNumberProvider() {
			return content.toNumberProvider();
		}
	}
}