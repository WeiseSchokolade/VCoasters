package de.schoko.editortestmod.client.editor;

public class EditorAction {
	public static Runnable createNewLinePreviewProvider = () -> EditorState.isPreviewing = false;
	public static Runnable cancelNewLinePreviewProvider = () -> EditorState.isPreviewing = false;
}
