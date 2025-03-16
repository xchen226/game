package events;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.SpellCard;
import structures.basic.Unit;
import structures.basic.Tile;
import structures.GameState;


public class CardClicked implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        int handPosition = message.get("position").asInt();
        gameState.handPosition = handPosition;

        Card clickedCard = gameState.playerHands.get(handPosition - 1);

        // **Card Deselection Logic Start**
        if (gameState.isCardClicked != null) {
            if (gameState.isCardClicked.equals(clickedCard)) {
                // Clicked the same card again, deselect it
                System.out.println("[DEBUG - CardClicked.processEvent] Deselecting card (same card click): " + gameState.isCardClicked.getCardname() + " at position: " + handPosition);
                gameState.isCardClicked = null;
                System.out.println("[DEBUG - CardClicked.processEvent] Calling clearHighlights (same card click, forceClear=true)"); // Debug Log
                clearHighlights(out, gameState, true); // Clear tile highlights as card is deselected
                return; // Exit processEvent as card is deselected
            } else {
                // Clicked a different card, deselect the previously selected card
                System.out.println("[DEBUG - CardClicked.processEvent] Deselecting card (different card click): " + gameState.isCardClicked.getCardname());
                System.out.println("[DEBUG - CardClicked.processEvent] Previously selected card: " + gameState.isCardClicked.getCardname()); // Debug Log
                gameState.isCardClicked = null; // Deselect previous card
                System.out.println("[DEBUG - CardClicked.processEvent] Calling clearHighlights (different card click, forceClear=true)"); // Debug Log
                clearHighlights(out, gameState, true); // Clear tile highlights of previous selection
                // No need to return, fall through to select the newly clicked card
            }
        }

        // **Card Deselection Logic End**

        Card selectedCard = gameState.playerHands.get(handPosition - 1); // Re-assign selectedCard after deselection logic, though it's actually 'clickedCard'

        System.out.println("[DEBUG - CardClicked.processEvent] Calling clearHighlights (before new highlight, forceClear=true)"); // Debug Log
        clearHighlights(out, gameState, true); // Clear highlights again just before highlighting for new card (redundant but harmless)


        BasicCommands.drawCard(out, selectedCard, handPosition, 1);

        if (selectedCard instanceof SpellCard) {
            if (selectedCard.getCardname().equals("Wraithling Swarm")){
            gameState.isWraithlingSwarmActive = true;
            System.out.println("[DEBUG - CardClicked.processEvent] Selected a spell card: " + selectedCard.getCardname());
            gameState.isCardClicked = (SpellCard) selectedCard; // Update isCardClicked to the *newly* clicked card
            gameState.SpellUsing = (SpellCard) selectedCard;
            highlightSpellTargets(out, gameState, (SpellCard) selectedCard);
            }
            System.out.println("[DEBUG - CardClicked.processEvent] Selected a spell card: " + selectedCard.getCardname());
            gameState.isCardClicked = (SpellCard) selectedCard; // Update isCardClicked to the *newly* clicked card
            gameState.SpellUsing = (SpellCard) selectedCard;
            highlightSpellTargets(out, gameState, (SpellCard) selectedCard);
        } else {
            System.out.println("[DEBUG - CardClicked.processEvent] Selected a unit card: " + selectedCard.getCardname());
            gameState.isCardClicked = selectedCard; // Update isCardClicked to the *newly* clicked card
            gameState.SpellUsing = null;
            highlightUnitCardTargets(out, gameState, selectedCard); 
        }
    }

    public static void highlightSpellTargets(ActorRef out, GameState gameState, SpellCard spellCard) {
        if (gameState.tileMap == null) {
            System.out.println("[ERROR] gameState.tileMap is NULL!");
            return;
        }

        if (spellCard.getCardname().equals("Wraithling Swarm") && !gameState.isWraithlingSwarmActive) {
            System.out.println("[DEBUG - SpellCard] Wraithling Swarm is NOT active and is the current spell card. Skipping highlightSpellTargets.");
            return;
        }

        System.out.println("[DEBUG] Highlighting spell targets for: " + spellCard.getCardname());

        gameState.clearHighlightedTiles();

        for (int x = 0; x < gameState.tileMap.length; x++) {
            for (int y = 0; y < gameState.tileMap[x].length; y++) {
                Tile tile = gameState.tileMap[x][y];
                Unit unit = gameState.checkmap(x, y);
                int highlightColor = 0;

                if (tile == null) continue;

                switch (spellCard.getCardname()) {
                    case "Horn of the Forsaken":
                    if (unit != null && gameState.player1Unit.contains(unit) && unit.getHealth() > 6) { 
                        highlightColor = 1;
                    }
                        break;
                    case "Wraithling Swarm":
                        if (unit == null && isAdjacentToFriendlyUnit(gameState, x, y)) {
                            highlightColor = 4;
                        }
                        break;
                    case "Dark Terminus":
                    case "True Strike":
                        if (unit != null && gameState.player2Unit.contains(unit)) {
                            highlightColor = 2;
                        }
                        break;
                    case "Sundrop Elixir":
                        if (unit != null && gameState.player1Unit.contains(unit)) {
                            highlightColor = 1;
                        }
                        break;
                    case "Beam Shock":
                        if (unit != null && gameState.player2Unit.contains(unit) && unit != gameState.player2Unit.get(0)) {
                            highlightColor = 3;
                        }
                        break;
                }
                if (highlightColor > 0) {
                    System.out.println("[DEBUG] Highlighting tile at (" + x + "," + y + ") with color " + highlightColor);
                    BasicCommands.drawTile(out, tile, highlightColor);
                    try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}//lu
                    tile.setHighlightState(highlightColor);

                    gameState.getHighlightedTiles().add(tile);
                }
            }
        }
    }
    public static void highlightUnitCardTargets(ActorRef out, GameState gameState, Card unitCard) {
        System.out.println("[DEBUG] Highlighting unit card placement targets (using Wraithling Swarm logic).");

        gameState.clearHighlightedTiles();

        for (int x = 0; x < gameState.tileMap.length; x++) {
            for (int y = 0; y < gameState.tileMap[x].length; y++) {
                Tile tile = gameState.tileMap[x][y];
                Unit unit = gameState.checkmap(x, y);
                int highlightColor = 0;
                if (tile == null) continue;
                if (unit == null && isAdjacentToFriendlyUnit(gameState, x, y)) {
                    highlightColor = 4; 
                }
                if (highlightColor > 0) {
                    System.out.println("[DEBUG - highlightUnitCardTargets] Highlighting tile at (" + x + "," + y + ") with color " + highlightColor + " (Wraithling Swarm logic)");
                    BasicCommands.drawTile(out, tile, highlightColor);
                    try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}//lu
                    tile.setHighlightState(highlightColor);
                    gameState.getHighlightedTiles().add(tile);
                }
            }
        }
    }

    private static boolean isAdjacentToFriendlyUnit(GameState gameState, int x, int y) {
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && nx < gameState.tileMap.length &&
                    ny >= 0 && ny < gameState.tileMap[0].length) {

                Unit adjacentUnit = gameState.checkmap(nx, ny);
                if (adjacentUnit != null && gameState.player1Unit.contains(adjacentUnit)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static void clearHighlights(ActorRef out, GameState gameState, boolean forceClear) {
        if (gameState.isWraithlingSwarmActive) {
            System.out.println("[DEBUG] Wraithling Swarm in progress, skipping highlight clear.");
            return;
        }

        System.out.println("[DEBUG] Clearing all highlights... (forceClear=" + forceClear + ")");

        for (int x = 0; x < gameState.tileMap.length; x++) {
            for (int y = 0; y < gameState.tileMap[x].length; y++) {
                Tile tile = gameState.tileMap[x][y];
                if (tile != null && (forceClear || tile.getHighlightState() != 0)) {
                    BasicCommands.drawTile(out, tile, 0);
                    try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}//lu
                    tile.setHighlightState(0);
                }
            }
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}