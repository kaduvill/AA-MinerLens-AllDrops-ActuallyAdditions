package com.kaduvill.aaminerlensall.jei;

final class MinerLensRecipeLayout {
    static final int COLUMNS = 9;
    static final int SLOT_SIZE = 18;
    static final int WIDTH = COLUMNS * SLOT_SIZE;
    static final int LENS_X = (WIDTH - SLOT_SIZE) / 2;
    static final int LENS_Y = 22;
    static final int INPUT_X = (WIDTH - SLOT_SIZE) / 2;
    static final int INPUT_Y = 0;
    static final int ARROW_Y = 44;
    static final int OUTPUT_Y = 72;
    static final int OUTPUT_OFFSET = 2;
    static final int INFO_HEIGHT = 13;
    static final int FOOTER_HEIGHT = 13;
    private static final int MAX_OUTPUT_ROWS = 4;

    final int pageSize;
    final int infoY;
    final int footerY;
    final int height;

    MinerLensRecipeLayout(int dropCount) {
        if (dropCount < 0) throw new IllegalArgumentException("Drop count cannot be negative");
        int rows = Math.max(1, Math.min(MAX_OUTPUT_ROWS, 1 + (Math.max(1, dropCount) - 1) / COLUMNS));
        pageSize = rows * COLUMNS;
        infoY = OUTPUT_Y + rows * SLOT_SIZE;
        footerY = dropCount > pageSize ? infoY + INFO_HEIGHT : -1;
        height = infoY + INFO_HEIGHT + (footerY < 0 ? 0 : FOOTER_HEIGHT);
    }

    int outputX(int visibleIndex) {
        return visibleIndex % COLUMNS * SLOT_SIZE;
    }

    int outputY(int visibleIndex) {
        return OUTPUT_Y + visibleIndex / COLUMNS * SLOT_SIZE;
    }

    int pageCount(int dropCount) {
        return Math.max(1, 1 + (Math.max(1, dropCount) - 1) / pageSize);
    }
}
