package actors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import events.CardClicked;
import events.EndTurnClicked;
import events.EventProcessor;
import events.Heartbeat;
import events.Initalize;
import events.OtherClicked;
import events.TileClicked;
import events.UnitDeathEvent;
import events.UnitMoving;
import events.UnitStopped;
import events.UnitSummonEvent;
import play.libs.Json;
import structures.GameState;
import utils.ImageListForPreLoad;

/**
 * The game actor is an Akka Actor that receives events from the user front-end UI (e.g. when
 * the user clicks on the board) via a websocket connection. When an event arrives, the
 * processMessage() method is called, which can be used to react to the event. The Game actor
 * also includes an ActorRef object which can be used to issue commands to the UI to change
 * what the user sees. The GameActor is created when the user browser creates a websocket
 * connection to back-end services (on load of the game web page).
 */
public class GameActor extends AbstractActor {

    private ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer
    private ActorRef out;                             // The ActorRef can be used to send messages to the front-end UI
    private Map<String,EventProcessor> eventProcessors; 
    private GameState gameState; // A class that can be used to hold game state information

    /**
     * Constructor for the GameActor. This is called by the GameController when the websocket
     * connection to the front-end is established.
     * @param out
     */
    @SuppressWarnings("deprecation")
    public GameActor(ActorRef out) {

        this.out = out;

        // create class instances to respond to the various events that we might receive
        eventProcessors = new HashMap<>();
        eventProcessors.put("initalize", new Initalize());
        eventProcessors.put("heartbeat", new Heartbeat());
        eventProcessors.put("unitMoving", new UnitMoving());
        eventProcessors.put("unitstopped", new UnitStopped());
        eventProcessors.put("tileclicked", new TileClicked());
        eventProcessors.put("cardclicked", new CardClicked());
        eventProcessors.put("endturnclicked", new EndTurnClicked());
        eventProcessors.put("otherclicked", new OtherClicked());
        
        //lu
        eventProcessors.put("unitSummon", new UnitSummonEvent());
        eventProcessors.put("unitDeath", new UnitDeathEvent());
        //lu

        // Initialize a new game state object
        gameState = new GameState();

        // Get the list of image files to pre-load the UI with
        Set<String> images = ImageListForPreLoad.getImageListForPreLoad();

        try {
            ObjectNode readyMessage = Json.newObject();
            readyMessage.put("messagetype", "actorReady");
            readyMessage.put("preloadImages", mapper.readTree(mapper.writeValueAsString(images)));
            out.tell(readyMessage, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method simply farms out the processing of the json messages from the front-end to the
     * processMessage method
     * @return
     */
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, message -> {
                    System.out.println("GameActor received: " + message); // 调试日志
                    String msgType = message.get("messagetype").asText();
                    processMessage(msgType, message);
                })
                .build();
    }

    /**
     * This looks up an event processor for the specified message type.
     * Note that this processing is asynchronous.
     * @param messageType
     * @param message
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"deprecation"})
    public void processMessage(String messageType, JsonNode message) throws Exception{

        if ("initalize".equals(messageType) && gameState.gameInitalised) {
            System.out.println("GameActor: 'initalize' message received but game is already initialized. Skipping...");
            return;
        }

        EventProcessor processor = eventProcessors.get(messageType);
        if (processor == null) {
            // Unknown event type
            System.err.println("GameActor: Received unknown event type: " + messageType);
        } else {
            processor.processEvent(out, gameState, message); // process the event
        }
    }

    public void reportError(String errorText) {
        ObjectNode returnMessage = Json.newObject();
        returnMessage.put("messagetype", "ERR");
        returnMessage.put("error", errorText);
        out.tell(returnMessage, out);
    }
}

