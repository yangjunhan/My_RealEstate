package dataSourceLayer.ConcurrencyUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Chuang Wang
 * @studentID 791793
 * @institution University of Melbourne
 */
public class LockerManager {
    private static LockerManager lockerManager;
    private Map<Object, String> lockMap = new ConcurrentHashMap<>();

    private LockerManager(){ }

    public static LockerManager getInstance(){
        if (lockerManager == null) {
            lockerManager = new LockerManager();
        }
        return lockerManager;
    }

    public boolean acquireLock(Object lockable, String owner) {
        boolean result = true;
        if (hasLock(lockable)){
            result = false;
        } else {
            lockMap.put(lockable, owner);
        }
        return result;
    }

    public void releaseLock(Object lockable, String owner){
        if (hasLock(lockable))
            lockMap.remove(lockable, owner);
    }

    public void releaseAllLocks(String owner) {
        lockMap.entrySet().removeIf(entry -> (owner.equals(entry.getValue())));
    }

    private boolean hasLock(Object lockable){
        return lockMap.containsKey(lockable);
    }
}
