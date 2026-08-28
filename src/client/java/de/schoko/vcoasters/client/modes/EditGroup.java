package de.schoko.vcoasters.client.modes;

import de.schoko.vcoasters.core.EditorObject;

public interface EditGroup extends EditorObject {
	boolean isMember(EditorObject o);
}
