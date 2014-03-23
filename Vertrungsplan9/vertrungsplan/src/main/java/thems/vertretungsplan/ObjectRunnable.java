package thems.vertretungsplan;

/**
 * Created by Michael on 22.03.14.
 */
public class ObjectRunnable implements Runnable {
    Object object;
    public ObjectRunnable(Object object) { this.object = object; }
    @Override
    public void run() {
    }
}