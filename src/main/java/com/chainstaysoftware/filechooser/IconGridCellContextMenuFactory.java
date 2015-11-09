package com.chainstaysoftware.filechooser;

import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.File;

/**
 * Build {@link ContextMenu} instance for a single {@link IconGridCell}.
 */
public interface IconGridCellContextMenuFactory {
   ContextMenu create(final Pair<Image, File> pair);
}
