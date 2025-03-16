package structures.abilities;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import events.UnitSummonEvent;

//Gloom Chaser

public class OpeningGambit extends Ability {

    public OpeningGambit() {
        super("OpeningGambit");
    }

    @Override
    public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
    	
    	System.out.println("[DEBUG - Gloom Chaser's Ability] OpeningGambit Already triggered.");
    	
    	if (event instanceof JsonNode) {
            JsonNode eventNode = (JsonNode) event;
            if (eventNode.has("messageType") && "unitSummon".equals(eventNode.get("messageType").asText())) {
                // 此处表示传入的事件确实是 "unitSummon" 类型
                System.out.println("[DEBUG - Gloom Chaser's Ability message recieved] This is a unitSummon event.");
                // 在这里添加具体的逻辑代码
                int ownerX = owner.getPosition().getTilex();
                int ownerY = owner.getPosition().getTiley();
                // 对于人类玩家，假设召唤在左侧
                int targetX = ownerX - 1;
                int targetY = ownerY;
                if (gameState.checkmaprange(targetX, targetY) && gameState.checkmap(targetX, targetY) == null) {
                    Tile summonTile = gameState.tileMap[targetX][targetY];
                    System.out.println("[OpeningGambit] Unit " + owner.getId() +
                        " triggers ability. Attempting to summon Wraithling at (" + targetX + ", " + targetY + ").");
                    
                    placeWraithling(out, gameState, summonTile, owner.getOwner());
                } else {
                    System.out.println("[OpeningGambit] Target tile (" + targetX + ", " + targetY + ") is not available.");
                }
            }
    	}            

    }
    


    
    private static void placeWraithling(ActorRef out, GameState gameState, Tile tile, Player player) { 
        if (gameState.checkmap(tile.getTilex(), tile.getTiley()) != null || !gameState.checkmaprange(tile.getTilex(), tile.getTiley())) return;

        playEffect(out, gameState, tile, StaticConfFiles.f1_summon);

        Unit unit = BasicObjectBuilders.loadUnit(StaticConfFiles.wraithling, gameState.getNextUnitID(), Unit.class);
        unit.setPositionByTile(tile);
        gameState.setmap(tile.getTilex(), tile.getTiley(), unit);
        BasicCommands.drawUnit(out, unit, tile);
        gameState.player1Unit.add(unit);

        unit.setHealth(1);
        BasicCommands.setUnitHealth(out, unit, 1);
        unit.setAttack(1);
        BasicCommands.setUnitAttack(out, unit, 1);
        
        unit.setId(++ GameState.unitCounter);
        System.out.println("[DEBUG - placeWraithling] Unit ID: " + unit.getId() + ", Health set to: " + unit.getHealth() + " (Server-side)");
        System.out.println("[DEBUG - placeWraithling] Unit ID: " + unit.getId() + ", Attack set to: " + unit.getAttack() + " (Server-side)");
    }


    private static void playEffect(ActorRef out, GameState gameState, Tile tile, String effectFile) { 
        gameState.AnimationPlaying = true;
        EffectAnimation effect = BasicObjectBuilders.loadEffect(effectFile);
        BasicCommands.playEffectAnimation(out, effect, tile);
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        gameState.AnimationPlaying = false;
    }

}
