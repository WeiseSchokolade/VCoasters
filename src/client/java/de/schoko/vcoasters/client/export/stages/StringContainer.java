package de.schoko.vcoasters.client.export.stages;

public class StringContainer {
	private String string;

	public StringContainer(String string) {
		this.string = string;
	}

	public String get() {
		return string;
	}

	public void set(String string) {
		this.string = string;
	}

	public void append(String content) {
		string += content;
	}

	public void appendLine(String content) {
		string += "\n" + content;
	}

	public void replace(String regex, String replacement) {
		string = string.replaceAll(regex, replacement);
	}

	public void replaceFirst(String regex, String replacement) {
		string = string.replaceFirst(regex, replacement);
	}

	public int length() {
		return string.length();
	}
}
