package events;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.SpellCard;
import structures.basic.Tile;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import events.Initalize;
import events.CardClicked;


public class TileClicked implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        if (gameState == null) {
            System.err.println("[ERROR] GameState is null at the beginning of processEvent. Cannot process event.");
            return;
        }
        if (message == null) {
            System.err.println("[ERROR] Message JsonNode is null. Cannot process tile click event.");
            return;
        }
        int tilex, tiley;
        if (message.has("tilex") && message.has("tiley")) {
            tilex = message.get("tilex").asInt();
            tiley = message.get("tiley").asInt();
        } else {
            System.err.println("[ERROR] Message JsonNode is missing 'tilex' or 'tiley' field. Cannot determine clicked tile coordinates.");
            return;
        }

        if (gameState.player1Unit == null || gameState.player1Unit.isEmpty()) {
            System.err.println("[ERROR] player1Unit is empty or null in GameState. Game cannot proceed.");
            return;
        }
        if (gameState.player2Unit == null || gameState.player2Unit.isEmpty()) {
            System.err.println("[ERROR] player2Unit is empty or null in GameState. Game cannot proceed.");
            return;
        }
        if (!gameState.checkmaprange(tilex, tiley)) {
            System.err.println("[ERROR] Clicked tile coordinates (" + tilex + ", " + tiley + ") are out of map range.");
            return;
        }


        Unit occupant = gameState.checkmap(tilex, tiley);
        Tile tile = gameState.tileMap[tilex][tiley]; 
        if (tile == null) {
            System.err.println("[ERROR] No Tile found at (" + tilex + ", " + tiley + ") in gameState.tileMap.");
            return; 
        } else {
            System.out.println("[DEBUG] Tile at (" + tilex + ", " + tiley + ")");
        }
        tile.setOccupant(occupant);
        System.out.println("[DEBUG] Clicked occupant: " + occupant);

        if (gameState.isCardClicked instanceof SpellCard) {
            Tile clickedTile = gameState.getHighlightedTile(tilex, tiley);
            if (clickedTile == null) {
                System.err.println("[ERROR] gameState.getHighlightedTile(" + tilex + "," + tiley + ") returned null! Cannot proceed with SpellCard.");
                return; 
            }
            handleSpellCardClick(out, gameState, clickedTile, occupant, tilex, tiley);
            return;
        }
        handleUnitControlClick(out, gameState, tile, occupant, tilex, tiley);


    }
private void handleSpellCardClick(ActorRef out, GameState gameState, Tile clickedTile, Unit occupant, int tilex, int tiley) {
    SpellCard spellCard = (SpellCard) gameState.isCardClicked;
    System.out.println("[DEBUG - SpellCard] Selected a spell card: " + spellCard.getCardname());

    if (clickedTile.getHighlightState() == 0) {
        System.out.println("[DEBUG - SpellCard] Clicked on a non-highlighted tile. Ignoring for SpellCard.");
        return;
    }
    int spellCost = spellCard.getManacost();
    int playerMana = gameState.humanPlayer.getMana();
    if (playerMana < spellCost) {
        System.out.println("[WARN - SpellCard] Insufficient mana to cast spell card: " + spellCard.getCardname() + ". Cost: " + spellCost + ", Current Mana: " + playerMana);
        BasicCommands.addPlayer1Notification(out, "Insufficient Mana!", 2);
        return;
    }
if (spellCard.getCardname().equals("Wraithling Swarm")) {
    if (occupant == null) {
        System.out.println("[DEBUG - SpellCard] Executing Wraithling Swarm");
       if (gameState.wraithlingSwarmCount != 0) {
        System.out.println("[DEBUG - SpellCard] Continuing Wraithling Swarm... Ghost " + (gameState.wraithlingSwarmCount + 1) + "/3");

        gameState.wraithlingSwarmCount++;
        spellCard.Wraithling_Swarm_Spell(out, gameState, clickedTile, spellCard);
            if (gameState.wraithlingSwarmCount >= 3) {
                System.out.println("[DEBUG] Wraithling Swarm completed!");
                gameState.isWraithlingSwarmActive = false;
                gameState.wraithlingSwarmCount = 0;
                gameState.activeWraithlingSwarmCard = null;

            System.out.println("[DEBUG - SpellCard] Spell cast completed. Clearing highlights...");
            clearHighlights(out, gameState);

            System.out.println("[DEBUG - SpellCard] Resetting selected card...");
            gameState.isCardClicked = null;
            } else {
                System.out.println("[DEBUG] Waiting for next tile selection...");
                CardClicked.highlightSpellTargets(out, gameState, spellCard);
            }

            
        }
        System.out.println("[DEBUG - SpellCard] Starting Wraithling Swarm!");
        gameState.activeWraithlingSwarmCard = spellCard;
        gameState.wraithlingSwarmCount++;
        spellCard.Wraithling_Swarm_Spell(out, gameState, clickedTile, spellCard);
        System.out.println("[DEBUG] Waiting for next tile selection...");
        CardClicked.highlightSpellTargets(out, gameState, spellCard);

    } else {
        System.out.println("[DEBUG - SpellCard] Cannot execute Wraithling Swarm. Occupant found at (" + tilex + ", " + tiley + ").");
    }
} else {
    if (occupant == null) {
        System.out.println("[DEBUG - SpellCard] No occupant at (" + tilex + ", " + tiley + "). Cannot execute spell: " + spellCard.getCardname());
        return;
    }
    System.out.println("[DEBUG - SpellCard] Occupant found at (" + tilex + ", " + tiley + "): " + occupant);
    System.out.println("[DEBUG - SpellCard] Executing spell: " + spellCard.getCardname() + ", SpellType: " + spellCard.getSpellType());
        switch (spellCard.getSpellType()) {
            case "Horn_of_the_Forsaken":
                System.out.println("[DEBUG - SpellCard] Executing Horn_of_the_Forsaken...");
                spellCard.Horn_of_the_Forsaken(out, gameState, clickedTile, spellCard);
                break;
            case "Wraithling_Swarm_Spell":
                System.out.println("[DEBUG - SpellCard] Executing Wraithling_Swarm_Spell...");
                spellCard.Wraithling_Swarm_Spell(out, gameState, clickedTile, spellCard);
                break;
            case "Dark_Terminus":
                System.out.println("[DEBUG - SpellCard] Executing Dark_Terminus...");
                spellCard.Dark_Terminus(out, gameState, clickedTile, spellCard);
                break;
            case "Sundrop_Elixir":
                System.out.println("[DEBUG - SpellCard] Executing Sundrop_Elixir...");
                spellCard.Sundrop_Elixir(out, gameState, clickedTile, spellCard);
                break;
            case "True_Strike":
                System.out.println("[DEBUG - SpellCard] Executing True_Strike...");
                spellCard.True_Strike(out, gameState, clickedTile, spellCard);
                break;
            case "Beam_Shock":
                System.out.println("[DEBUG - SpellCard] Executing Beam_Shock...");
                spellCard.Beam_Shock(out, gameState, clickedTile, spellCard);
                break;
            default:
                System.out.println("[ERROR - SpellCard] Unknown spell type: " + spellCard.getSpellType());
                break;
        }
    }

    if (spellCard != null && spellCard.getCardname().equals("Wraithling Swarm") && gameState.isWraithlingSwarmActive) {
        System.out.println("[DEBUG - SpellCard] Wraithling Swarm is still active, skipping generic spell completion handling.");
        return;
    }

    System.out.println("[DEBUG - SpellCard] Spell cast completed. Clearing highlights...");
    clearHighlights(out, gameState);
    System.out.println("[DEBUG - SpellCard] Resetting selected card...");
    gameState.isCardClicked = null;
   boolean cardRemoved = false;
   if (gameState.playerHands != null && spellCard != null && !cardRemoved) {
       int cardPositionToDelete = -1;

       System.out.println("[DEBUG - SpellCard - Hand Check] Starting card position lookup in playerHands. SpellCard: " + spellCard.getCardname() + ", ID: " + spellCard.getId() + ", Object: " + spellCard);
       System.out.println("[DEBUG - SpellCard - Hand Check] playerHands content:");
       for (int i = 0; i < gameState.playerHands.size(); i++) {
           Card handCard = gameState.playerHands.get(i);
           boolean isEqual = handCard.equals(spellCard);

           System.out.println("[DEBUG - SpellCard - Hand Check]  - Hand card at position " + (i + 1) + ": " + handCard.getCardname() + ", ID: " + handCard.getId() + ", Object: " + handCard + ", isEqual: " + isEqual);
           if (isEqual) {
               cardPositionToDelete = i + 1;
               System.out.println("[DEBUG - SpellCard - Hand Check]   Found matching card in hand at position " + (i + 1) + ", Card ID: " + handCard.getId() + ", Object: " + handCard);
               break;
           } else {
               System.out.println("[DEBUG - SpellCard - Hand Check]   Card at position " + (i + 1) + " is NOT equal to SpellCard. Hand Card ID: " + handCard.getId() + ", SpellCard ID: " + spellCard.getId());
           }
       }
       System.out.println("[DEBUG - SpellCard - Hand Check] playerHands content END");
       System.out.println("[DEBUG - SpellCard - Hand Check] Card position to delete: " + cardPositionToDelete);

       if (cardPositionToDelete != -1) {
        gameState.playerHands.remove(spellCard);
        System.out.println("[DEBUG - SpellCard] Spell card removed from playerHands: " + spellCard.getCardname() + ", at position: " + cardPositionToDelete + ", ID: " + spellCard.getId());
    
        BasicCommands.deleteCard(out, cardPositionToDelete);
        System.out.println("[DEBUG - SpellCard] Sent command to client to delete card UI at position: " + cardPositionToDelete + ", card: " + spellCard.getCardname() + ", ID: " + spellCard.getId());
    
        cardRemoved = true;
        int removedCardPosition = cardPositionToDelete;
        int newMana = playerMana - spellCost;
        gameState.humanPlayer.setMana(newMana);
        BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
        System.out.println("[DEBUG - SpellCard] Mana cost deducted. Spell Cost: " + spellCost + ", New Mana: " + newMana);
        System.out.println("[DEBUG - SpellCard] Drawing a new card for player 1 after spell card use, aiming for position: " + removedCardPosition);
        Initalize.drawCardForPlayer1(out, gameState, removedCardPosition);
       }}
    }
    private void handleUnitControlClick(ActorRef out, GameState gameState, Tile tile, Unit occupant, int tilex, int tiley) { 
        if (gameState.isTileClicked == null) {
            handleUnitSelection(out, gameState, occupant);
        } else {
            handleUnitActionClick(out, gameState, tile, occupant, tilex, tiley);
        }
    }
    private void handleUnitSelection(ActorRef out, GameState gameState, Unit occupant) {
        if (occupant != null) {
            if (gameState.player1Unit.contains(occupant)) {
                gameState.isTileClicked = occupant;
                System.out.println("[DEBUG - UnitControl] Selected unit with ID=" + occupant.getId());
                highlightMoveTiles(out, gameState, occupant);
                highlightAttackTiles(out, gameState, occupant);
            } else {
                System.out.println("[DEBUG - UnitControl] Cannot select enemy unit or occupant not found in player1Unit.");
            }
        } else {
            System.out.println("[DEBUG - UnitControl] Clicked empty tile, no unit selected.");
        }
    }

    private void handleUnitActionClick(ActorRef out, GameState gameState, Tile tile, Unit occupant, int tilex, int tiley) { 
        clearHighlights(out, gameState);
        Unit selectedUnit = gameState.isTileClicked;
        System.out.println("[DEBUG - UnitAction] Selected unit: " + selectedUnit);

        if (occupant != null) {
            if (!isEnemy(gameState, selectedUnit, occupant)) {
                System.out.println("[DEBUG - UnitAction] Target unit " + occupant.getId() + " is not an enemy. Cannot attack.");
                gameState.isTileClicked = null;
                return;
            }

            performUnitAttack(out, gameState, selectedUnit, occupant);

        } else {
            performUnitMove(out, gameState, selectedUnit, tile, tilex, tiley);
        }
        gameState.isTileClicked = null;
    }
    private void performUnitAttack(ActorRef out, GameState gameState, Unit attacker, Unit defender) {
        System.out.println("[DEBUG - UnitAction - Attack] Attempting attack: Unit " + attacker.getId() + " -> enemy unit " + defender.getId());
        int attackRange = attacker.hasMoved() ? 1 : 3;
        int unitX = attacker.getPosition().getTilex();
        int unitY = attacker.getPosition().getTiley();
        int enemyX = defender.getPosition().getTilex();
        int enemyY = defender.getPosition().getTiley();
        int distance = Math.abs(unitX - enemyX) + Math.abs(unitY - enemyY);

        if (distance <= attackRange) {
            doAttack(out, gameState, attacker, defender);
        } else {
            System.out.println("[DEBUG - UnitAction - Attack] Enemy out of range, moving closer...");
            Tile approachTile = findApproachTile(gameState, attacker, defender);
            if (approachTile != null) {
                BasicCommands.moveUnitToTile(out, attacker, approachTile);
                BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.move);
                int oldX = attacker.getPosition().getTilex();
                int oldY = attacker.getPosition().getTiley();
                gameState.clearmap(oldX, oldY);
                attacker.setPositionByTile(approachTile);
                gameState.setmap(approachTile.getTilex(), approachTile.getTiley(), attacker);
                doAttack(out, gameState, attacker, defender);
            } else {
                System.out.println("[DEBUG - UnitAction - Attack] No valid approach tile found. Cannot move closer to attack.");
            }
        }
    }

    private void performUnitMove(ActorRef out, GameState gameState, Unit selectedUnit, Tile targetTile, int tilex, int tiley) { 
        System.out.println("[DEBUG - UnitAction - Move] Move unit ID=" + selectedUnit.getId() + " to (" + tilex + ", " + tiley + ")");
        if (!isInMoveRange(selectedUnit, tilex, tiley)) {
            System.out.println("[DEBUG - UnitAction - Move] Target tile is out of allowed move range.");
        } else {
            BasicCommands.moveUnitToTile(out, selectedUnit, targetTile);
            BasicCommands.playUnitAnimation(out, selectedUnit, UnitAnimationType.move);
            int oldX = selectedUnit.getPosition().getTilex();
            int oldY = selectedUnit.getPosition().getTiley();
            gameState.clearmap(oldX, oldY);
            selectedUnit.setPositionByTile(targetTile);
            gameState.setmap(tilex, tiley, selectedUnit);
            selectedUnit.setHasMoved(true);
            System.out.println("[DEBUG - UnitAction - Move] Unit " + selectedUnit.getId() + " moved to (" + tilex + ", " + tiley + ") successfully.");
        }
    }


    private void doAttack(ActorRef out, GameState gameState, Unit attacker, Unit defender) {
        System.out.println("[DEBUG - Combat] Unit " + attacker.getId() + " attacks unit " + defender.getId());
        BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
        int attackDamage = attacker.getAttack();
        int newDefenderHealth = defender.getHealth() - attackDamage;
        if (newDefenderHealth < 0) {
            newDefenderHealth = 0;
        }
        defender.setHealth(newDefenderHealth);
        BasicCommands.setUnitHealth(out, defender, newDefenderHealth);
        System.out.println("[DEBUG - Combat] Attacker " + attacker.getId() + " deals " + attackDamage + " damage.");
        if (defender == gameState.playerAvatar) {
            gameState.humanPlayer.setHealth(newDefenderHealth);
            BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
        } else if (defender == gameState.aiAvatar) {
            gameState.aiPlayer.setHealth(newDefenderHealth);
            BasicCommands.setPlayer2Health(out, gameState.aiPlayer);
        }
        if (newDefenderHealth <= 0) {
            BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.death);
            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
            BasicCommands.deleteUnit(out, defender);
            int dx = defender.getPosition().getTilex();
            int dy = defender.getPosition().getTiley();
            gameState.clearmap(dx, dy);
            System.out.println("[DEBUG - Combat] Defender " + defender.getId() + " is dead!");
        } else {
            BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.attack);
            int counterDamage = defender.getAttack();
            int newAttackerHealth = attacker.getHealth() - counterDamage;
            if (newAttackerHealth < 0) {
                newAttackerHealth = 0;
            }
            attacker.setHealth(newAttackerHealth);
            BasicCommands.setUnitHealth(out, attacker, newAttackerHealth);
            System.out.println("[DEBUG - Combat] Defender " + defender.getId() + " counterattacks for " + counterDamage + " damage.");
            if (attacker == gameState.playerAvatar) {
                gameState.humanPlayer.setHealth(newAttackerHealth);
                BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
            } else if (attacker == gameState.aiAvatar) {
                gameState.aiPlayer.setHealth(newAttackerHealth);
            }
            if (newAttackerHealth <= 0) {
                BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.death);
                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                BasicCommands.deleteUnit(out, attacker);
                int ax = attacker.getPosition().getTilex();
                int ay = attacker.getPosition().getTiley();
                gameState.clearmap(ax, ay);
                System.out.println("[DEBUG - Combat] Attacker " + attacker.getId() + " died in counterattack!");
            }
        }
    }

    private boolean isInMoveRange(Unit unit, int tilex, int tiley) {
        int oldX = unit.getPosition().getTilex();
        int oldY = unit.getPosition().getTiley();
        int dx = Math.abs(tilex - oldX);
        int dy = Math.abs(tiley - oldY);

        if (dx <= 2 && dy == 0) return true;
        if (dy <= 2 && dx == 0) return true;
        if (dx == 1 && dy == 1) return true;
        return false;
    }

    private Tile findApproachTile(GameState gameState, Unit attacker, Unit enemy) {
        int ex = enemy.getPosition().getTilex();
        int ey = enemy.getPosition().getTiley();
        int[][] dirs = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
        for (int[] d : dirs) {
            int nx = ex + d[0];
            int ny = ey + d[1];
            if (gameState.checkmaprange(nx, ny) && gameState.checkmap(nx, ny) == null) {
                return gameState.tileMap[nx][ny];
            }
        }
        return null;
    }

    private void highlightMoveTiles(ActorRef out, GameState gameState, Unit unit) {
        for (int i = 0; i < gameState.tileMap.length; i++) {
            for (int j = 0; j < gameState.tileMap[i].length; j++) {
                Tile t = gameState.tileMap[i][j];
                if (t != null
                    && gameState.checkmap(i, j) == null
                    && isInMoveRange(unit, i, j))
                {
                    if (t.getHighlightState() != 1) {
                        BasicCommands.drawTile(out, t, 1);
                        t.setHighlightState(1);
                    }
                }
            }
        }
    }

    private void highlightAttackTiles(ActorRef out, GameState gameState, Unit unit) {
        int attackRange = unit.hasMoved() ? 1 : 3;
        int unitX = unit.getPosition().getTilex();
        int unitY = unit.getPosition().getTiley();

        for (int i = 0; i < gameState.tileMap.length; i++) {
            for (int j = 0; j < gameState.tileMap[i].length; j++) {
                Tile t = gameState.tileMap[i][j];
                Unit occupant = gameState.checkmap(i, j);
                if (t != null
                    && occupant != null
                    && isEnemy(gameState, unit, occupant))
                {
                    int distance = Math.abs(unitX - i) + Math.abs(unitY - j);
                    if (distance <= attackRange) {
                        if (t.getHighlightState() != 2) {
                            BasicCommands.drawTile(out, t, 2);
                            t.setHighlightState(2);
                        }
                    }
                }
            }
        }
    }

    private void clearHighlights(ActorRef out, GameState gameState) {
        for (int i = 0; i < gameState.tileMap.length; i++) {
            for (int j = 0; j < gameState.tileMap[i].length; j++) {
                Tile t = gameState.tileMap[i][j];
                if (t != null && t.getHighlightState() != 0) {
                    BasicCommands.drawTile(out, t, 0);
                    t.setHighlightState(0);
                }
            }
        }
    }

    private boolean isEnemy(GameState gameState, Unit selectedUnit, Unit other) {
        if (gameState.player1Unit.contains(selectedUnit)) {
            return gameState.player2Unit.contains(other);
        } else if (gameState.player2Unit.contains(selectedUnit)) {
            return gameState.player1Unit.contains(other);
        }
        return false;
    }
}