package de.schoko.vcoasters.core;

public class DirtContainer implements EditorComponent {
	private boolean dirty;

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
