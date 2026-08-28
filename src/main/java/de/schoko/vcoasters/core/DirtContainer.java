package de.schoko.vcoasters.core;

public class DirtContainer implements EditorComponent {
	private boolean dirty;

	public DirtContainer() {
	}

	public DirtContainer(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
