package de.schoko.vcoasters.client.export.numcalcparser;

public interface NumberProviderExpression {
	record NumberExpression(String value) implements NumberProviderExpression {

	}

	record SumExpression(NumberProviderExpression a, NumberProviderExpression b) implements NumberProviderExpression {

	}

	record ProductExpression(NumberProviderExpression a, NumberProviderExpression b) implements NumberProviderExpression {

	}

	record ScoreReferenceExpression(String objective, String holder) implements NumberProviderExpression {

	}

	record StorageReferenceExpression(String storage, String path) implements NumberProviderExpression {

	}
}
