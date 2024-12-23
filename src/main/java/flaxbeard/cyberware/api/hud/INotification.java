package flaxbeard.cyberware.api.hud;

public interface INotification {
    void render(int x, int y);
    int getDuration();
}
