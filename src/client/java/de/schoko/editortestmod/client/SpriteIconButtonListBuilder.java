package de.schoko.editortestmod.client;

import net.minecraft.client.gui.components.SpriteIconButton;

import java.util.ArrayList;
import java.util.List;

public class SpriteIconButtonListBuilder {
	private final int offset;
	private final ScreenView view;
	private final List<SpriteIconButton> buttons;

	private Direction direction;
	private Alignment alignment;

	private int gap;

	public SpriteIconButtonListBuilder(int offset, ScreenView view) {
		this.offset = offset;
		this.view = view;
		buttons = new ArrayList<>();
		direction = Direction.VERTICAL;
		alignment = Alignment.START;
		gap = 5;
	}

	public void build() {
		int availableLength = 0;
		int totalLength = 0;
		switch (direction) {
			case HORIZONTAL:
				availableLength = view.getButtonScreen().width;
				for (SpriteIconButton button : buttons) {
					totalLength += button.getWidth();
				}
				break;
			case VERTICAL:
				availableLength = view.getButtonScreen().height;
				for (SpriteIconButton button : buttons) {
					totalLength += button.getHeight();
				}
				break;
		}
		totalLength += (buttons.size() - 1) * gap;
		int runningPos = switch (alignment) {
			case START -> gap;
			case CENTER -> availableLength / 2 - totalLength / 2;
			case END -> availableLength - totalLength - gap;
		};
		for (SpriteIconButton button : buttons) {
			switch (direction) {
				case HORIZONTAL:
					button.setPosition(runningPos, offset);
					runningPos += button.getWidth() + gap;
					break;
				case VERTICAL:
					button.setPosition(offset, runningPos);
					runningPos += button.getHeight() + gap;
					break;
			}
			view.addWidget(button);
		}
		buttons.clear();
	}

	public SpriteIconButtonListBuilder add(SpriteIconButton button) {
		buttons.add(button);
		return this;
	}

	public SpriteIconButtonListBuilder setAlignment(Alignment alignment) {
		this.alignment = alignment;
		return this;
	}

	public SpriteIconButtonListBuilder setDirection(Direction direction) {
		this.direction = direction;
		return this;
	}

	public SpriteIconButtonListBuilder setGap(int gap) {
		this.gap = gap;
		return this;
	}

	public enum Alignment {
		START,
		CENTER,
		END;
	}

	public enum Direction {
		HORIZONTAL,
		VERTICAL;
	}
}
