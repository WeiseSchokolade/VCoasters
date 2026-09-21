package de.schoko.vcoasters.client.core;

import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public final class FileDialogUtils {
	private FileDialogUtils() {
	}

	public static CompletableFuture<String> saveFolderDialog(String defaultLocation) {
		CompletableFuture<String> future = new CompletableFuture<>();
		SDLDialog.SDL_ShowOpenFolderDialog((userdata, folderlist, filter) -> {
			if (folderlist == MemoryUtil.NULL) {
				future.complete(null);
				return;
			}
			long folderPtr = MemoryUtil.memGetAddress(folderlist);
			future.complete(MemoryUtil.memUTF8Safe(folderPtr));
		}, 0, Minecraft.getInstance().getWindow().handle(), defaultLocation, false);
		return future;
	}

	public static CompletableFuture<String> saveFileDialog(String defaultLocation) {
		CompletableFuture<String> future = new CompletableFuture<>();
		SDLDialog.SDL_ShowSaveFileDialog((userdata, filelist, filter) -> {
			if (filelist == MemoryUtil.NULL) {
				future.complete(null);
				return;
			}
			long filePtr = MemoryUtil.memGetAddress(filelist);
			future.complete(MemoryUtil.memUTF8Safe(filePtr));
		}, 0, Minecraft.getInstance().getWindow().handle(), new SDL_DialogFileFilter.Buffer(ByteBuffer.allocate(0)), defaultLocation);
		return future;
	}

	public static void showMessageBox(String title, String message) {
		SDLMessageBox.SDL_ShowSimpleMessageBox(SDLMessageBox.SDL_MESSAGEBOX_INFORMATION, title, message, Minecraft.getInstance().getWindow().handle());
	}

	public static void showWarningMessageBox(String title, String message) {
		SDLMessageBox.SDL_ShowSimpleMessageBox(SDLMessageBox.SDL_MESSAGEBOX_WARNING, title, message, Minecraft.getInstance().getWindow().handle());
	}

	public static void showErrorMessageBox(String title, String message) {
		SDLMessageBox.SDL_ShowSimpleMessageBox(SDLMessageBox.SDL_MESSAGEBOX_ERROR, title, message, Minecraft.getInstance().getWindow().handle());
	}
}
