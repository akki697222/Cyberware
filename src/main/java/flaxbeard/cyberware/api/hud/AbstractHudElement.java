package flaxbeard.cyberware.api.hud;

import net.minecraft.world.entity.player.Player;

public abstract class AbstractHudElement implements IHudElement {
    private int defaultX = 0;
    private int defaultY = 0;
    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;
    private boolean hidden = false;
    private String name;

    private AnchorHorizontal defaultHAnchor = AnchorHorizontal.LEFT;
    private AnchorVertical defaultVAnchor = AnchorVertical.TOP;
    private AnchorHorizontal hAnchor = AnchorHorizontal.LEFT;
    private AnchorVertical vAnchor = AnchorVertical.TOP;

    public AbstractHudElement(String name)
    {
        this.name = name;
    }

    @Override
    public void render(Player player, int scaledWidth, int scaledHeight, boolean isHUDjackAvailable, boolean isConfigOpen, float partialTicks) {

    }

    public abstract void renderElement(int x, int y, Player player, int scaledWidth, int scaledHeight, boolean hudjackAvailable, boolean isConfigOpen, float partialTicks);

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public void setX(int x)
    {
        this.x = x;
    }

    @Override
    public void setY(int y)
    {
        this.y = y;
    }

    public void setDefaultX(int x)
    {
        this.defaultX = x;
        setX(x);
    }

    public void setDefaultY(int y)
    {
        this.defaultY = y;
        setY(y);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public boolean canHide()
    {
        return true;
    }

    @Override
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    @Override
    public AnchorHorizontal getHorizontalAnchor() {
        return hAnchor;
    }

    @Override
    public void setHorizontalAnchor(AnchorHorizontal anchor)
    {
        hAnchor = anchor;
    }

    public void setDefaultHorizontalAnchor(AnchorHorizontal anchor)
    {
        defaultHAnchor = anchor;
        setHorizontalAnchor(anchor);
    }

    @Override
    public AnchorVertical getVerticalAnchor()
    {
        return vAnchor;
    }

    public void setDefaultVerticalAnchor(AnchorVertical anchor)
    {
        defaultVAnchor = anchor;
        setVerticalAnchor(anchor);
    }

    @Override
    public void setVerticalAnchor(AnchorVertical anchor)
    {
        vAnchor = anchor;
    }

    public void setWidth(int w)
    {
        width = w;
    }

    public void setHeight(int h)
    {
        height = h;
    }

    @Override
    public void reset()
    {
        x = defaultX;
        y = defaultY;
        vAnchor = defaultVAnchor;
        hAnchor = defaultHAnchor;
    }

    @Override
    public String getUniqueName()
    {
        return name;
    }

    @Override
    public void save(IHudSaveData data)
    {
        data.setInteger("x", x);
        data.setInteger("y", y);
        data.setBoolean("top", vAnchor == AnchorVertical.TOP);
        data.setBoolean("left", hAnchor == AnchorHorizontal.LEFT);
    }

    @Override
    public void load(IHudSaveData data)
    {
        x = data.getInteger("x");
        y = data.getInteger("y");
        vAnchor = data.getBoolean("top") ? AnchorVertical.TOP : AnchorVertical.BOTTOM;
        hAnchor = data.getBoolean("left") ? AnchorHorizontal.LEFT : AnchorHorizontal.RIGHT;
    }
}
