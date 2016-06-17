package jsonobjects;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

public class ActionJsonObject {

    public enum Type {
        APPLIANCE("Appliance"), ELECTRONICS("electronics"), LIGHTS("Lights"), OTHER("Other");

        Type(String type) {
        }
    }

    JsonProvider provider = JsonProvider.provider();
    private final JsonObject actionJsonObject;

    public ActionJsonObject(String action, int id, String name, String type, String status, String description) {
        actionJsonObject = provider.createObjectBuilder().add("action", action).add("id", id).add("name", name).add("type", type)
                .add("status", status).add("description", description).build();
    }

    public JsonObject getActionJsonObject() {
        return actionJsonObject;
    }

}
