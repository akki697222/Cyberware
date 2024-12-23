package flaxbeard.cyberware.api.hud;

public interface IHudSaveData {
    void setString(String key, String s);
    void setInteger(String key, int i);
    void setBoolean(String key, boolean b);
    void setFloat(String key, float f);
    String getString(String key);
    int getInteger(String key);
    boolean getBoolean(String key);
    float getFloat(String key);
}
