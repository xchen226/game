package events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.abilities.Ability;
import structures.abilities.Flying;
import structures.abilities.Provoke;
/*
import structures.abilities.Ability;
import structures.abilities.DeathwatchAttackAndHealthBoost;
import structures.abilities.DeathwatchAttackBoost;
import structures.abilities.DeathwatchDamageAndHeal;
import structures.abilities.DeathwatchSummonWraithling;
import structures.abilities.OpeningGambit;
import structures.abilities.OpeningGambitDestroy;
import structures.basic.BetterUnit;
import structures.basic.BigCard;
*/
import structures.basic.Card;
import structures.basic.SpellCard;
import structures.basic.Tile;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import utils.SummonUnitOnBoard;
import events.Initalize;
import play.libs.Json;
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

        if (gameState.isCardClicked != null){
        if (gameState.isCardClicked instanceof SpellCard) {
            Tile clickedTile = gameState.getHighlightedTile(tilex, tiley);
            if (clickedTile == null) {
                System.err.println("[ERROR] gameState.getHighlightedTile(" + tilex + "," + tiley + ") returned null! Cannot proceed with SpellCard.");
                return; 
            }
            handleSpellCardClick(out, gameState, clickedTile, occupant, tilex, tiley);
            return;
        }else if(!(gameState.isCardClicked instanceof SpellCard)) { //lu: CreatureCard Click
            Tile clickedTile = gameState.getHighlightedTile(tilex, tiley);
            // 判断点击的 tile 是否为高亮的合法区域
            if (clickedTile == null){ 
            return;
            }
            handleUnitCardClick(out, gameState, clickedTile, tilex, tiley);
            return;
        }
     }
    handleUnitControlClick(out, gameState, tile, occupant, tilex, tiley);
        
        
    }
    
    //lu:如果点击CreatureCard
    public static void handleUnitCardClick(ActorRef out, GameState gameState, Tile clickedTile, int tilex, int tiley) { //  ✅ 修改方法签名： 去掉 occupant 参数， 但保留 tilex, tiley

        // 2. 获取当前选中的卡牌（CreatureCard）
        Card creatureCard = gameState.isCardClicked; //  ✅  使用 creatureCard 变量名更清晰

        System.out.println("[DEBUG - CreatureCard] Selected a creature card: " + creatureCard.getCardname()); //  ✅  修改 Debug 信息为 Creature Card
        if (clickedTile.getHighlightState() == 0) {
            System.out.println("[DEBUG - CreatureCard] Clicked on a non-highlighted tile. Ignoring for CreatureCard."); //  ✅  修改 Debug 信息为 Creature Card
            return;
        }

        int creatureCost = creatureCard.getManacost();
        int playerMana = gameState.humanPlayer.getMana();

        if (playerMana < creatureCost) { //  ✅  添加 mana 不足的判断
            System.out.println("[DEBUG - CreatureCard] Not enough mana to summon " + creatureCard.getCardname() + ". Required: " + creatureCost + ", Available: " + playerMana);
            BasicCommands.addPlayer1Notification(out, "Not enough mana!", 2); //  ✅  添加客户端提示
            gameState.isCardClicked = null; //  ✅  取消卡牌选中状态
            clearHighlights(out, gameState); 
            return; //  ✅  mana 不足， 直接返回
        }


        Unit summonedUnit = SummonUnitOnBoard.summonUnit(out, gameState, clickedTile, tilex, tiley, creatureCard); //  ✅  调用 CreatureUnit.summonUnit 方法， 传递 creatureCard
        gameState.player1Unit.add(summonedUnit);
        gameState.setmap(tilex, tiley, summonedUnit);

        //更新手牌 (手牌更新逻辑保持不变)
        System.out.println("[DEBUG - CreatureCard - Hand Update] Unit Summon completed for " + creatureCard.getCardname() + ". Updating hand..."); //  ✅  修改 Debug 信息为 Creature Card
        clearHighlights(out, gameState);
        gameState.isCardClicked = null;

        boolean cardRemoved = false;
        if (gameState.playerHands != null && creatureCard != null && !cardRemoved) {
            int cardPositionToDelete = -1;

            System.out.println("[DEBUG - CreatureCard - Hand Check] Starting card position lookup in playerHands. CreatureCard: " + creatureCard.getCardname() + ", ID: " + creatureCard.getId() + ", Object: " + creatureCard); //  ✅  修改 Debug 信息为 Creature Card
            System.out.println("[DEBUG - CreatureCard - Hand Check] playerHands content:"); //  ✅  修改 Debug 信息为 Creature Card
            for (int i = 0; i < gameState.playerHands.size(); i++) {
                Card handCard = gameState.playerHands.get(i);
                boolean isEqual = handCard.equals(creatureCard);

                System.out.println("[DEBUG - CreatureCard - Hand Check]  - Hand card at position " + (i + 1) + ": " + handCard.getCardname() + ", ID: " + handCard.getId() + ", Object: " + handCard + ", isEqual: " + isEqual); //  ✅  修改 Debug 信息为 Creature Card
                if (isEqual) {
                    cardPositionToDelete = i + 1;
                    System.out.println("[DEBUG - CreatureCard - Hand Check]   Found matching card in hand at position " + (i + 1) + ", Card ID: " + handCard.getId() + ", Object: " + handCard); //  ✅  修改 Debug 信息为 Creature Card
                    break;
                } else {
                    System.out.println("[DEBUG - CreatureCard - Hand Check]   Card at position " + (i + 1) + " is NOT equal to CreatureCard. Hand Card ID: " + handCard.getId() + ", SpellCard ID: " + creatureCard.getId()); //  ✅  修改 Debug 信息为 Creature Card
                }
            }
            System.out.println("[DEBUG - CreatureCard - Hand Check] playerHands content END"); //  ✅  修改 Debug 信息为 Creature Card
            System.out.println("[DEBUG - CreatureCard - Hand Check] Card position to delete: " + cardPositionToDelete); //  ✅  修改 Debug 信息为 Creature Card

            if (cardPositionToDelete != -1) {
                int handSizeBeforeDelete = gameState.playerHands.size();
                for (int i = 1; i <= handSizeBeforeDelete; i++) {
                    BasicCommands.deleteCard(out, i);
                    System.out.println("[DEBUG - CreatureCard - Hand Update] Deleting ALL hand card UI at position: " + i); //  ✅  修改 Debug 信息为 Creature Card
                }

                gameState.playerHands.remove(creatureCard);
                System.out.println("[DEBUG - CreatureCard - Hand Update] Creature card removed from playerHands: " + creatureCard.getCardname() + ", at position: " + cardPositionToDelete + ", ID: " + creatureCard.getId()); //  ✅  修改 Debug 信息为 Creature Card

                BasicCommands.deleteCard(out, cardPositionToDelete);
                System.out.println("[DEBUG - CreatureCard - Hand Update] Sent command to client to delete card UI at position: " + cardPositionToDelete + ", card: " + creatureCard.getCardname() + ", ID: " + creatureCard.getId()); //  ✅  修改 Debug 信息为 Creature Card
                for (int i = 0; i < gameState.playerHands.size(); i++) {
                    Card handCard = gameState.playerHands.get(i);
                    BasicCommands.drawCard(out, handCard, i + 1, 0);
                    System.out.println("[DEBUG - CreatureCard - Hand Update] Redrawing card: " + handCard.getCardname() + ", at position: " + (i + 1)); //  ✅  修改 Debug 信息为 Creature Card
                }
                cardRemoved = true;
                int removedCardPosition = cardPositionToDelete;
                //扣除mana
                int newMana = playerMana - creatureCost;
                gameState.humanPlayer.setMana(newMana);
                gameState.aiPlayer.setMana(newMana);
                BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
                BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
                System.out.println("[DEBUG - CreatureCard - Hand Update] Mana cost deducted. Spell Cost: " + creatureCost + ", New Mana: " + newMana); //  ✅  修改 Debug 信息为 Creature Card
            }
        }
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
}     else if(spellCard.getCardname().equals("Dark Terminus")){

         if (occupant.getHealth() > 8) { 
        System.out.println("[DEBUG - Dark Terminus] Target's health is greater than 8. Spell cannot be cast on target with health > 8."); 
        return;
    }

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
        int handSizeBeforeDelete = gameState.playerHands.size();
        for (int i = 1; i <= handSizeBeforeDelete; i++) {
            BasicCommands.deleteCard(out, i);
            System.out.println("[DEBUG - SpellCard] Deleting ALL hand card UI at position: " + i);
        }


        gameState.playerHands.remove(spellCard);
        System.out.println("[DEBUG - SpellCard] Spell card removed from playerHands: " + spellCard.getCardname() + ", at position: " + cardPositionToDelete + ", ID: " + spellCard.getId());

        BasicCommands.deleteCard(out, cardPositionToDelete);
        System.out.println("[DEBUG - SpellCard] Sent command to client to delete card UI at position: " + cardPositionToDelete + ", card: " + spellCard.getCardname() + ", ID: " + spellCard.getId());
        for (int i = 0; i < gameState.playerHands.size(); i++) {
            Card handCard = gameState.playerHands.get(i);
            BasicCommands.drawCard(out, handCard, i + 1, 0);
            System.out.println("[DEBUG - SpellCard] Redrawing card: " + handCard.getCardname() + ", at position: " + (i + 1));
        }
        cardRemoved = true;
        int removedCardPosition = cardPositionToDelete;
        int newMana = playerMana - spellCost;
        gameState.humanPlayer.setMana(newMana);
        gameState.aiPlayer.setMana(newMana);
        BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
        BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
        System.out.println("[DEBUG - SpellCard] Mana cost deducted. Spell Cost: " + spellCost + ", New Mana: " + newMana);
        gameState.SpellUsing = null;
    }
      }
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

            if (!selectedUnit.isFinishAttack()) {
                performUnitAttack(out, gameState, selectedUnit, occupant);
                selectedUnit.setFinishAttack(true);
               if (selectedUnit.getRobustness() != 0){
                    SpellCard.Random_Wraithling(out, gameState, selectedUnit);
                }
            } else {
                return;
            }

        } else {
            if (!selectedUnit.isFinishMove()) {
            performUnitMove(out, gameState, selectedUnit, tile, tilex, tiley);
            selectedUnit.setFinishMove(true);
            }else {
                return;
            }
        }

        gameState.isTileClicked = null;
    }
    private void performUnitAttack(ActorRef out, GameState gameState, Unit attacker, Unit defender) {
        
    	//lu:Provoke对攻击的限制
    	System.out.println("[DEBUG - UnitAction - Attack] Attempting attack: Unit " + attacker.getId() + " -> enemy unit " + defender.getId());
        
        // 首先检查攻击者周围是否存在拥有 "Provoke" 的敌方单位
        if (Provoke.isStunned(attacker, gameState)) {
            // 如果存在，则要求 defender 也必须拥有 Provoke 能力w
            boolean defenderHasProvoke = false;
            if (defender.getAbilities() != null) {
                for (Ability ability : defender.getAbilities()) {
                    if ("Provoke".equals(ability.getAbilityName())) {
                        defenderHasProvoke = true;
                        break;
                    }
                }
            }
            if (!defenderHasProvoke) {
                System.err.println("[DEBUG - UnitAction - Attack] Attacker " + attacker.getId() + " is forced to attack a unit with Provoke!");
                BasicCommands.addPlayer1Notification(out, "You must attack an enemy with Provoke!", 2);
                return;
            }
        }
    	//lu
    	
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


    public static void doAttack(ActorRef out, GameState gameState, Unit attacker, Unit defender) {
        System.out.println("[DEBUG - Combat] Unit " + attacker.getId() + " attacks unit " + defender.getId());
        BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
    
        int defenderRobustness = defender.getRobustness();
        System.out.println("[DEBUG - Combat - Defender Robustness Check (Defender - Attack Phase)] Defender " + defender.getId() + " Robustness Value: " + defenderRobustness + " (Attack Phase - Before Damage)");
        if (defenderRobustness > 0) {
            System.out.println("[DEBUG - Combat - Defender Robustness Active (Defender - Attack Phase)] Defender " + defender.getId() + " HAS ROBUSTNESS, ABSORBING Attack Damage!");
            defenderRobustness--;
            defender.setRobustness(defenderRobustness);
            System.out.println("[DEBUG - Combat - Defender Robustness Reduced (Defender - Attack Phase)] Defender " + defender.getId() + " Robustness reduced to: " + defenderRobustness + " (Attack Phase)");
            System.out.println("[DEBUG - Combat - Defender Robustness Absorbed Attack Damage (Attack Phase)] Defender's Robustness ABSORBED Attack Damage, defender takes NO damage.");
        } else {
            System.out.println("[DEBUG - Combat - Defender No Robustness (Defender - Attack Phase)] Defender " + defender.getId() + " NO ROBUSTNESS or Robustness is 0. Defender WILL take Attack Damage.");
            int attackDamage = attacker.getAttack();
            int newDefenderHealth = defender.getHealth() - attackDamage;
            if (newDefenderHealth < 0) {
                newDefenderHealth = 0;
            }
            defender.setHealth(newDefenderHealth);
            BasicCommands.setUnitHealth(out, defender, newDefenderHealth);
            System.out.println("[DEBUG - Combat - Health Damage] Attacker " + attacker.getId() + " deals " + attackDamage + " damage. Defender " + defender.getId() + " new health: " + newDefenderHealth);
    
            if (defender.getId() == 0) { 
                gameState.humanPlayer.setHealth(newDefenderHealth);
                BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
            } else if (defender.getId() == 100) {
                gameState.aiPlayer.setHealth(newDefenderHealth);
                BasicCommands.setPlayer2Health(out, gameState.aiPlayer);

            if (newDefenderHealth <= 0) {
                BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.death);
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
                BasicCommands.deleteUnit(out, defender);
                int dx = defender.getPosition().getTilex();
                int dy = defender.getPosition().getTiley();
                gameState.clearmap(dx, dy);
                
                //lu
                gameState.delPlay1Unit(defender);
                gameState.delPlay2Unit(defender);
                gameState.removeUnit(defender);
                // 构造死亡事件的 JSON 消息
                ObjectNode deathMsg = Json.newObject();
                deathMsg.put("messageType", "unitDeath"); // 指定事件类型为单位死亡
                deathMsg.put("id", attacker.getId());     // 将死亡单位的ID加入消息中
                // 创建 UnitDeathEvent 事件处理器实例，并调用 processEvent 方法
                UnitDeathEvent deathEvent = new UnitDeathEvent();
                deathEvent.processEvent(out, gameState, deathMsg);
                
                System.out.println("[DEBUG - Combat - Defender Dead] Defender " + defender.getId() + " is dead!");
                return;
                }
            }
        }
    
        BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.attack);
        int counterDamage = defender.getAttack();
    
        int attackerRobustness = attacker.getRobustness();
        System.out.println("[DEBUG - Combat - Attacker Robustness Check (Attacker - Counterattack Phase)] Attacker " + attacker.getId() + " Robustness Value: " + attackerRobustness + " (Counterattack Phase - Before Damage)");
        if (attackerRobustness > 0) {
            System.out.println("[DEBUG - Combat - Attacker Robustness Active (Attacker - Counterattack Phase)] Attacker " + attacker.getId() + " HAS ROBUSTNESS, ABSORBING Counterattack Damage!");
            attackerRobustness--;
            attacker.setRobustness(attackerRobustness);
            System.out.println("[DEBUG - Combat - Attacker Robustness Reduced (Attacker - Counterattack Phase)] Attacker " + attacker.getId() + " Robustness reduced to: " + attackerRobustness + " (Counterattack Phase)");
            System.out.println("[DEBUG - Combat - Attacker Robustness Absorbed Counterattack Damage (Counterattack Phase)] Attacker's Robustness ABSORBED Counterattack Damage, attacker takes NO damage from counterattack.");
        } else {
            System.out.println("[DEBUG - Combat - Attacker No Robustness (Attacker - Counterattack Phase)] Attacker " + attacker.getId() + " NO ROBUSTNESS or Robustness is 0 during Counterattack. Attacker WILL take Counterattack Damage.");
            int newAttackerHealth = attacker.getHealth() - counterDamage;
            if (newAttackerHealth < 0) {
                newAttackerHealth = 0;
            }
            attacker.setHealth(newAttackerHealth);
            BasicCommands.setUnitHealth(out, attacker, newAttackerHealth);
            System.out.println("[DEBUG - Combat - Counterattack Damage] Defender " + defender.getId() + " counterattacks for " + counterDamage + " damage. Attacker " + attacker.getId() + " new health: " + newAttackerHealth);
            if (attacker.getId() == 0) {
                gameState.humanPlayer.setHealth(newAttackerHealth);
                BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
            } else if (attacker.getId() == 100) {
                gameState.aiPlayer.setHealth(newAttackerHealth);
            }
            if (newAttackerHealth <= 0) {
                BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.death);
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
                BasicCommands.deleteUnit(out, attacker);
                int ax = attacker.getPosition().getTilex();
                int ay = attacker.getPosition().getTiley();
                gameState.clearmap(ax, ay);
                            
                //lu
                gameState.delPlay1Unit(attacker);
                gameState.delPlay2Unit(attacker);
                gameState.removeUnit(attacker);
                // 构造死亡事件的 JSON 消息
                ObjectNode deathMsg = Json.newObject();
                deathMsg.put("messageType", "unitDeath"); // 指定事件类型为单位死亡
                deathMsg.put("id", attacker.getId());     // 将死亡单位的ID加入消息中
                // 创建 UnitDeathEvent 事件处理器实例，并调用 processEvent 方法
                UnitDeathEvent deathEvent = new UnitDeathEvent();
                deathEvent.processEvent(out, gameState, deathMsg);

                
                System.out.println("[DEBUG - Combat - Attacker Died in Counterattack] Attacker " + attacker.getId() + " died in counterattack!");
            }
        }
    }

    private boolean isInMoveRange(Unit unit, int tilex, int tiley) {
    	
    	// lu:Flying能力移动范围判定
    	boolean canFly = Flying.canFly(unit);
    	if(canFly) {
    		return true;
    	} else {
    		int oldX = unit.getPosition().getTilex();
            int oldY = unit.getPosition().getTiley();
            int dx = Math.abs(tilex - oldX);
            int dy = Math.abs(tiley - oldY);

            if (dx <= 2 && dy == 0) return true;
            if (dy <= 2 && dx == 0) return true;
            if (dx == 1 && dy == 1) return true;
            return false;
    	}      
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
                        try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}//lu
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
                            try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}//lu
                            t.setHighlightState(2);
                        }
                    }
                }
            }
        }
    }

    public static void clearHighlights(ActorRef out, GameState gameState) {
        for (int i = 0; i < gameState.tileMap.length; i++) {
            for (int j = 0; j < gameState.tileMap[i].length; j++) {
                Tile t = gameState.tileMap[i][j];
                if (t != null && t.getHighlightState() != 0) {
                    BasicCommands.drawTile(out, t, 0);
                    try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}//lu
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

