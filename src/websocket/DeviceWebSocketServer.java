package websocket;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import model.Device;
import model.Message;
import utils.Action;

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
        logger.log(Level.INFO, "Session opened");
    }

    @OnClose
    public void close(Session session) {
        logger.log(Level.INFO, "Closing Session");
        sessionHandler.removeSession(session);
        logger.log(Level.INFO, "Session Closed ");
    }

    @OnError
    public void onError(Throwable error) {
        logger.log(Level.INFO, "error Detected");
        logger.log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(String messageJSON, Session session) {

        JSONObject jsonMessage = new JSONObject(messageJSON);
        logger.log(Level.INFO, jsonMessage.toString());

        if (equalAction(Action.ADD, jsonMessage)) {
            Device device = new Device();
            device.setName(jsonMessage.getString("name"));
            device.setDescription(jsonMessage.getString("description"));
            device.setType(jsonMessage.getString("type"));
            device.setStatus("Off");
            sessionHandler.addDevice(device);
        } else if (equalAction(Action.REMOVE, jsonMessage)) {
            int id = jsonMessage.getInt("id");
            sessionHandler.removeDevice(id);
        } else if (equalAction(Action.TOGGLE, jsonMessage)) {
            int id = jsonMessage.getInt("id");
            sessionHandler.toggleDevice(id);

        } else if (equalAction("sendChatMessage", jsonMessage)) {
            Message message = new Message();
            message.setText(jsonMessage.getString("text"));
            message.setDateSent(new LocalDateTime());
            message.setIdSession(session.getId());
            chatHandler.putMessageOnChat(message);
            Set<Message> allMessagesChat = chatHandler.getAllMessages();
            for (Message messageShow : allMessagesChat) {
                logger.log(Level.INFO, messageShow.getText());
            }
            sessionHandler.broadcastChatMessage(message);
        }
    }

    private boolean equalAction(String action, JSONObject jsonMessage) {
        return action.equals(jsonMessage.getString("action"));
    }
}
