package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2;

import com.advancedweb2022groupylt.finalpj.bean.http.response.Message;
import com.advancedweb2022groupylt.finalpj.bean.http.response.SetSceneResponse;
import com.advancedweb2022groupylt.finalpj.service.logic.middleStage.SceneRecordService;
import com.advancedweb2022groupylt.finalpj.service.logic.middleStage.SceneTimeRecordService;
import com.advancedweb2022groupylt.finalpj.util.SessionUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Service
public final class UserRoomTable
{
    public static final int ROOM_CNT = 17;
    private static final int MAX_PEOPLE_PER_ROOM = 10;

    /*
     every method have to
          either call another method
          or acquire the lock before read and write and release the lock before return
    */
    private final Lock lock = new ReentrantLock();

    /*
     reason for param name 'index' : I think this is like mysql index:
     we can get entry containing user's session , username , roomId by username or roomId
     this is like sql index
    */
    private Map<String, UserSessionEntry> userIndex = new HashMap<>();
    private final Set<UserSessionEntry>[] roomIndex = new Set[ROOM_CNT];
    // note : jb idea may think this gramma is not good , just ignore the suggestion of jb idea

    @Resource
    private SceneRecordService sceneRecordService;
    @Resource
    private SceneTimeRecordService sceneTimeRecordService;

    { // init block
        for (int i = 0; i < ROOM_CNT; i++)
        {
            roomIndex[i] = new HashSet<>();
        }
    }

    public void clearDeadSessions(ISaveStrategy saveStrategy)
    {
        lock.lock();
        for (int i = 0; i < ROOM_CNT; i++)
        {
            roomIndex[i].clear();
        }
        boolean[] somebodyDead = new boolean[ROOM_CNT];
/*        Set<Map.Entry<String, UserSessionEntry>> allUserInfo = userIndex.entrySet();
        Map<String, UserSessionEntry> newUserIndex = new HashMap<>();
        for (Map.Entry<String, UserSessionEntry> userInfo : allUserInfo)
        {
            if (saveStrategy.shouldSave(userInfo.getValue()))
            {
                newUserIndex.put(userInfo.getKey(), userInfo.getValue());
                roomIndex[userInfo.getValue().currentRoomIdx].add(userInfo.getValue());
            }
            else
            {
                somebodyDead[userInfo.getValue().getCurrentRoomIdx()] = true;
                SessionUtils.closeSessionWithoutException(userInfo.getValue().wsSession);
                StaticLog.info("user {} is wiped out", userInfo.getKey());
            }
        }
        userIndex = newUserIndex;*/
        List<Session> sessionsToClose = new ArrayList<>();
        Map<String, UserSessionEntry> newUserIndex = new HashMap<>();
        for (String username : userIndex.keySet())
        {
            UserSessionEntry entry = userIndex.get(username);
            if (saveStrategy.shouldSave(entry))
            {
                newUserIndex.put(username, userIndex.get(username));
                roomIndex[entry.currentRoomIdx].add(entry);
            }
            else
            {
                somebodyDead[entry.currentRoomIdx] = true;
                sessionsToClose.add(entry.wsSession);
                sceneTimeRecordService.add(entry.username,entry.currentRoomIdx,new Date().getTime()-entry.lastChangeRoomTime);
                sceneRecordService.record(username,entry.currentRoomIdx);
            }
        }
        userIndex = newUserIndex;
        for (Session s : sessionsToClose) SessionUtils.closeSessionWithoutException(s);
        for (int i = 0; i < ROOM_CNT; i++)
        {
            if (somebodyDead[i])
                notifyAllUsersInRoomWithoutLock(i);
        }
        lock.unlock();
    }

    public void clearDeadSessions(long timeout)
    {
        long currentTime = new Date().getTime();
        clearDeadSessions(entry -> (entry.lastActiveTime + timeout) >= currentTime);
    }

    public void forEachInRoom(int roomIdx, Consumer<? super UserSessionEntry> action)
    {
        if (roomIdx < 0 || roomIdx > ROOM_CNT) throw new IllegalArgumentException();
        lock.lock();
        forEachInRoomWithoutLock(roomIdx, action);
        lock.unlock();
    }

    private void forEachInRoomWithoutLock(int roomIdx, Consumer<? super UserSessionEntry> action)
    {
        roomIndex[roomIdx].forEach(action);
    }

    public UserSessionEntry getUserInfo(String username)
    {
        lock.lock();
        UserSessionEntry res = userIndex.get(username);
        lock.unlock();
        return res;
    }

    public SetSceneResponse changeChatRoom(String username, int newRoomIdx)
    {
        if (newRoomIdx < 0 || newRoomIdx > ROOM_CNT) throw new IllegalArgumentException();
        SetSceneResponse response = new SetSceneResponse();
        lock.lock();
        UserSessionEntry entry = userIndex.get(username);
        if (entry != null)
        {
            if (roomIndex[newRoomIdx].size() < MAX_PEOPLE_PER_ROOM)
            {
                int oldERoomIdx = entry.currentRoomIdx;
                if (oldERoomIdx != newRoomIdx)
                {
                    sceneTimeRecordService.add(username,oldERoomIdx,new Date().getTime()-entry.lastChangeRoomTime);
                    roomIndex[oldERoomIdx].remove(new UserSessionEntry(username, -1, null));
                    entry.currentRoomIdx = newRoomIdx;
                    entry.lastChangeRoomTime = new Date().getTime();
                    roomIndex[newRoomIdx].add(entry);
                    notifyAllUsersInRoomWithoutLock(newRoomIdx);
                    notifyAllUsersInRoomWithoutLock(oldERoomIdx);
                }
                response.setMessage(new Message(true, "set scene success"));
                response.setScene(newRoomIdx);
            }
            else
            {
                response.setMessage(new Message(false, "the room is full"));
                response.setScene(entry.currentRoomIdx);
            }
        }
        else
        {
            response.getMessage().setSuccess(false);
            response.getMessage().setInformation("user does not exist");
            response.setScene(-1);
        }
        lock.unlock();
        return response;
    }

    public void addUser(String username, Session session)
    {
        addUser(username, 0, session);
    }

    public void addUser(String username, int initialRoomIdx, Session session)
    {
        if (initialRoomIdx < 0 || initialRoomIdx > ROOM_CNT) throw new IllegalArgumentException();
        lock.lock();
/*        if (userIndex.containsKey(username))
        {
            StaticLog.info("user {} already connected , refuse to establish a new connection", username);
            SessionUtils.closeSessionWithoutException(session);
        }
        else
        {*/
        UserSessionEntry entry = new UserSessionEntry(username, initialRoomIdx, session);
        roomIndex[initialRoomIdx].add(entry);
        userIndex.put(username, entry);
        notifyAllUsersInRoomWithoutLock(initialRoomIdx);
        //}
        lock.unlock();
    }

    private static boolean sessionEquals(Session s1, Session s2)
    {
        if (s1 == null)
        {
            return s2 == null;
        }
        return s1.equals(s2);
    }

/*    private void removeUserIfSessionEqualsWithoutLock(String username, Session session)
    {
        UserSessionEntry entry = userIndex.get(username);
        if (entry != null && sessionEquals(session, entry.wsSession))
        {
            SessionUtils.closeSessionWithoutException(entry.wsSession);
            roomIndex[entry.currentRoomIdx].remove(entry);
            userIndex.remove(entry.username);
            notifyAllUsersInRoomWithoutLock(entry.currentRoomIdx);
        }
    }*/

    private void notifyAllUsersInRoomWithoutLock(int roomIdx)
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "get_all_users");
        List<String> currentUsers = new ArrayList<>();
        for (UserSessionEntry entry : roomIndex[roomIdx])
        {
            currentUsers.add(entry.username);
        }
        jsonObject.put("users", currentUsers);
        String jsonStr = jsonObject.toJSONString();
        forEachInRoomWithoutLock(roomIdx, userSessionEntry -> SessionUtils.sendMessage(userSessionEntry.wsSession, jsonStr));
    }

    public void removeUser(String username)
    {
        lock.lock();
        removeUserWithoutLock(username);
        lock.unlock();
    }

    private void removeUserWithoutLock(String username)// this method must be wrapped by lock.lock()
    {
        UserSessionEntry entry = userIndex.get(username);
        if (entry != null)
        {
            SessionUtils.closeSessionWithoutException(entry.wsSession);
            roomIndex[entry.currentRoomIdx].remove(entry);
            sceneTimeRecordService.add(username,entry.currentRoomIdx,new Date().getTime()-entry.lastChangeRoomTime);
            sceneRecordService.record(username,entry.currentRoomIdx);
            userIndex.remove(entry.username);
            notifyAllUsersInRoomWithoutLock(entry.currentRoomIdx);
        }
    }

    @FunctionalInterface
    public interface ISaveStrategy
    {
        boolean shouldSave(UserSessionEntry entry);
    }

    public static final class UserSessionEntry
    {
        final String username;
        int currentRoomIdx;
        final Session wsSession;
        long lastActiveTime;
        private long lastChangeRoomTime;

        public UserSessionEntry(String username, int currentRoomIdx, Session wsSession)
        {
            this.username = username;
            this.currentRoomIdx = currentRoomIdx;
            this.wsSession = wsSession;
            this.lastActiveTime = new Date().getTime();
            this.lastChangeRoomTime = this.lastActiveTime;
        }

        public String getUsername()
        {
            return username;
        }

        public int getCurrentRoomIdx()
        {
            return currentRoomIdx;
        }

        public Session getWsSession()
        {
            return wsSession;
        }

        public long getLastActiveTime()
        {
            return lastActiveTime;
        }

        public void setLastActiveTime(long lastActiveTime)
        {
            this.lastActiveTime = lastActiveTime;
        }

        public void setCurrentRoomIdx(int currentRoomIdx)
        {
            this.currentRoomIdx = currentRoomIdx;
        }

        public long getLastChangeRoomTime()
        {
            return lastChangeRoomTime;
        }

        public void setLastChangeRoomTime(long lastChangeRoomTime)
        {
            this.lastChangeRoomTime = lastChangeRoomTime;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserSessionEntry entry = (UserSessionEntry) o;
            return Objects.equals(username, entry.username);
        }

        @Override
        public int hashCode()
        {
            return username.hashCode();
        }

        @Override
        public String toString()
        {
            return "UserSessionEntry{" +
                    "username='" + username + '\'' +
                    ", currentRoomIdx=" + currentRoomIdx +
                    ", lastActiveTime=" + lastActiveTime +
                    '}';
        }
    }
}
