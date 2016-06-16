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

import model.Device;
import model.IDevice;
import model.nullDevice;

@ApplicationScoped
public class DeviceSessionHandler {

    private int deviceId = 1;
    private final Set<Session> sessions = new HashSet<>();
    private final Set<Device> devices = new HashSet<>();

    public void addSession(Session session) {
        sessions.add(session);
        sendListDevicesToClient(session);
    }

    private void sendListDevicesToClient(Session session) {
        for (Device device : devices) {
            JsonObject addMessage = createAddMessage(device);
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

        JsonObject addMessage = createAddMessage(device);
        sendToAllConnectedSessions(addMessage);
    }

    public void removeDevice(int id) {
        Device device = (Device) getDeviceById(id);
        if (device != null) {
            devices.remove(device);
            JsonProvider provider = JsonProvider.provider();
            JsonObject removeMessage = provider.createObjectBuilder().add("action", "remove").add("id", id).build();

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
            JsonObject updateDevMessage = provider.createObjectBuilder().add("action", "toggle").add("id", device.getId()).add("status", device.getStatus()).build();
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

    private JsonObject createAddMessage(Device device) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder().add("action", "add").add("id", device.getId()).add("name", device.getName()).add("type", device.getType())
                .add("status", device.getStatus()).add("description", device.getDescription()).build();
        return addMessage;

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
            Logger.getLogger(DeviceSessionHandler.class.getName()).log(Level.SEVERE, null, IOex);
        }
    }
}
