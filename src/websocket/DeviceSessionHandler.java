package websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;

import jsonobjects.ActionJsonObject;
import model.Device;
import model.IDevice;
import model.Message;
import model.nullDevice;
import utils.Action;

@ApplicationScoped
public class DeviceSessionHandler {

    Logger logger = Logger.getLogger(DeviceSessionHandler.class.getName());
    private int deviceId = 1;
    private final Set<Session> sessions = new HashSet<>();
    private final Set<Device> devices = new HashSet<>();

    public void addSession(Session session) {
        sessions.add(session);
        sendListDevicesToClient(session);
    }

    private void sendListDevicesToClient(Session session) {
        for (Device device : devices) {
            JsonObject addMessage = createActionMessage(Action.ADD, device);
            sendToSession(session, addMessage);
        }

    }

    public void removeSession(Session session) {
        if (session != null) {
            sessions.remove(session);
        } else {
            System.out.println("session already closed");
        }
    }

    public void addDevice(Device device) {
        device.setId(deviceId);
        devices.add(device);
        deviceId++;

        JsonObject addMessage = createActionMessage(Action.ADD, device);
        sendToAllConnectedSessions(addMessage);
    }

    public void removeDevice(int id) {
        Device device = (Device) getDeviceById(id);
        if (device != null) {
            devices.remove(device);
            JsonProvider provider = JsonProvider.provider();
            JsonObject removeMessage = createActionMessage(Action.REMOVE, device);

            sendToAllConnectedSessions(removeMessage);
        }
    }

    public void toggleDevice(int id) {
        JsonProvider provider = JsonProvider.provider();
        Device device = (Device) getDeviceById(id);
        if (device != null) {
            if ("On".equals(device.getStatus())) {
                device.setStatus("Off");
            } else {
                device.setStatus("On");
            }
            JsonObject updateDevMessage = createActionMessage(Action.TOGGLE, device);
            sendToAllConnectedSessions(updateDevMessage);
        }
    }

    private IDevice getDeviceById(int id) {
        IDevice deviceFound = null;
        for (Device device : devices) {
            if (device.getId() == id) {
                deviceFound = device;
                break;
            } else {
                deviceFound = new nullDevice();
            }
        }
        return deviceFound;
    }

    private JsonObject createActionMessage(String action, Device device) {
        ActionJsonObject addMessage = new ActionJsonObject(action, device.getId(), device.getName(), device.getType(), device.getStatus(),
                device.getDescription());
        return addMessage.getActionJsonObject();

    }

    private void sendToAllConnectedSessions(JsonObject message) {
        for (Session session : sessions) {
            sendToSession(session, message);
        }

    }

    private void sendToSession(Session session, JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException IOex) {
            sessions.remove(session);
            logger.log(Level.SEVERE, null, IOex);
        }
    }

    public void broadcastChatMessage(Message message) {
        logger.log(Level.SEVERE, null, message.getText());
    }
}
