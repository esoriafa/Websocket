package websocket;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.joda.time.LocalDateTime;

import model.Device;
import model.Message;

@ApplicationScoped
@ServerEndpoint("/actions")
public class DeviceWebSocketServer {

    Logger logger = Logger.getLogger(DeviceWebSocketServer.class.getName());
    @Inject
    private DeviceSessionHandler sessionHandler;

    @Inject
    private ChatHandler chatHandler;

    @OnOpen
    public void open(Session session) {
        logger.log(Level.INFO, "opening Session");
        sessionHandler.addSession(session);
    }

    @OnClose
    public void close(Session session) {
        logger.log(Level.INFO, "Closing Session");
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
        logger.log(Level.INFO, "error Detected");
        logger.log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(String messageJSON, Session session) {

        try (JsonReader reader = Json.createReader(new StringReader(messageJSON))) {
            JsonObject jsonMessage = reader.readObject();
            logger.log(Level.INFO, jsonMessage.toString());

            if ("add".equals(jsonMessage.getString("action"))) {
                Device device = new Device();
                device.setName(jsonMessage.getString("name"));
                device.setDescription(jsonMessage.getString("description"));
                device.setType(jsonMessage.getString("type"));
                device.setStatus("Off");
                sessionHandler.addDevice(device);
            } else if ("remove".equals(jsonMessage.getString("action"))) {
                int id = jsonMessage.getInt("id");
                sessionHandler.removeDevice(id);
            } else if ("toggle".equals(jsonMessage.getString("action"))) {
                int id = jsonMessage.getInt("id");
                sessionHandler.toggleDevice(id);

            } else if ("sendChatMessage".equals(jsonMessage.getString("action"))) {
                String messageText = jsonMessage.getString("message");
                Message message = new Message();
                message.setText(jsonMessage.getString("text"));
                message.setDateSent(new LocalDateTime());
                message.setIdSession(session.getId());
                chatHandler.putMessageOnChat(message);
//                for (Message messageShow : get) {
//                    logger.log(Level.INFO, );
//                }
            }
        }
    }
}
