package websocket;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import model.Message;

@ApplicationScoped
public class ChatHandler {

    private final Set<Message> messages = new HashSet<>();

    public void putMessageOnChat(Message message) {
        messages.add(message);
    }

    public Set<Message> getAllMessages() {
        return messages;
    }
}
