package flaxbeard.cyberware.api.hud;

import net.minecraft.world.entity.player.Player;

public interface IHudElement {
    enum AnchorHorizontal
    {
        LEFT,
        RIGHT;
    }

    enum AnchorVertical
    {
        TOP,
        BOTTOM;
    }

    void render(Player player, int scaledWidth, int scaledHeight, boolean isHUDjackAvailable, boolean isConfigOpen, float partialTicks);

    boolean canMove();

    void setX(int x);
    void setY(int y);
    int getX();
    int getY();

    int getWidth();
    int getHeight();

    boolean canHide();
    void setHidden(boolean hidden);
    boolean isHidden();

    AnchorHorizontal getHorizontalAnchor();
    void setHorizontalAnchor(AnchorHorizontal anchor);

    AnchorVertical getVerticalAnchor();
    void setVerticalAnchor(AnchorVertical anchor);

    void reset();

    String getUniqueName();

    void save(IHudSaveData data);
    void load(IHudSaveData data);
}
