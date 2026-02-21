package de.schoko.editortestmod.client.editor;

import de.schoko.editortestmod.client.EditorTestModClient;
import de.schoko.editortestmod.core.EndPoint;

public class EditorAction {
	private static final Runnable EMPTY_RUNNABLE = () -> {};

	public static Runnable previewNewLinePreviewProvider = () -> EditorState.isPreviewing = true;
	public static Runnable createNewLinePreviewProvider = () -> EditorState.isPreviewing = false;
	public static Runnable cancelNewLinePreviewProvider = () -> EditorState.isPreviewing = false;

	public static Runnable splitSelectedLineInCenterProvider = EMPTY_RUNNABLE;
	public static Runnable deleteSelectedLineProvider = EMPTY_RUNNABLE;

	public static Runnable spawnFollowerCarProvider = EMPTY_RUNNABLE;
	public static Runnable deleteFollowerCarProvider = EMPTY_RUNNABLE;

	public static Runnable useTranslationGizmoForCurrentlySelectedEndpointProvider = () -> EditorState.endpointRotationMode = false;
	public static Runnable useRotationGizmoForCurrentlySelectedEndpointProvider = () -> EditorState.endpointRotationMode = true;
	public static Runnable resetCurrentlySelectedEndpointRotationProvider = EMPTY_RUNNABLE;

	public static Runnable snapCurrentlySelectedEndpointXZBottom = () -> {
		if (EditorTestModClient.instance.getEditorCtx().getSelectedObject() instanceof EndPoint endPoint) {
			endPoint.setPos(Math.floor(endPoint.x()) + 0.5f, Math.floor(endPoint.y()), Math.floor(endPoint.z()) + 0.5f);
			endPoint.markRendererAsDirty();
		}
	};

	public static Runnable snapCurrentlySelectedEndpointXYZ = () -> {
		if (EditorTestModClient.instance.getEditorCtx().getSelectedObject() instanceof EndPoint endPoint) {
			endPoint.setPos(Math.floor(endPoint.x()) + 0.5f, Math.floor(endPoint.y()) + 0.5f, Math.floor(endPoint.z()) + 0.5f);
			endPoint.markRendererAsDirty();
		}
	};
}
