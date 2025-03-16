package structures.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import events.UnitDeathEvent;

/**
 * Bloodmoon Priestess
 * DeathwatchSummonWraithling 能力：
 * 当其他单位死亡时触发此能力，使拥有该能力的单位在其周围随机一个空闲格上召唤一个 Wraithling。
 */
public class DeathwatchSummonWraithling extends Ability {

    public DeathwatchSummonWraithling() {
        super("DeathwatchSummonWraithling");
    }

    /**
     * 当事件触发时调用此方法。
     * @param owner 拥有该能力的单位
     * @param event 事件对象，预期为 UnitDeathEvent，且该事件包含当前 GameState 与 ActorRef 信息
     */
    @Override
    public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
    	
    	System.out.println("[DEBUG - Bloodmoon Priestess's Ability] DeathwatchSummonWraithling Alreagy triggered.");
    	
    	if (event instanceof JsonNode) {
            JsonNode eventNode = (JsonNode) event;
            if (eventNode.has("messageType") && "unitDeath".equals(eventNode.get("messageType").asText())) {
                // 此处表示传入的事件确实是 "unitSummon" 类型
                System.out.println("[DEBUG - Bloodmoon Priestess's Ability message recieved] This is a unitDeath event.");
                // 在这里添加具体的逻辑代码
                if (owner.getHealth() > 0) {
                    int x = owner.getPosition().getTilex();
                    int y = owner.getPosition().getTiley();
                    int[][] offsets = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
                    List<Tile> candidateTiles = new ArrayList<>();
                    for (int[] offset : offsets) {
                        int newX = x + offset[0];
                        int newY = y + offset[1];
                        if (gameState.checkmaprange(newX, newY) && gameState.checkmap(newX, newY) == null) {
                            Tile candidate = gameState.tileMap[newX][newY];
                            if (candidate != null) {
                                candidateTiles.add(candidate);
                            }
                        }
                    }
                    if (candidateTiles.isEmpty()) {
                        System.out.println("[DeathwatchSummonWraithling] No adjacent empty tile found. Ability has no effect.");
                        return;
                    }
                    Random rand = new Random();
                    Tile summonTile = candidateTiles.get(rand.nextInt(candidateTiles.size()));
                    System.out.println("[DeathwatchSummonWraithling] Unit " + owner.getId() +
                        " triggers ability. Summoning Wraithling at (" + summonTile.getTilex() + ", " + summonTile.getTiley() + ").");
                    placeWraithling(out, gameState, summonTile, owner.getOwner());
                    
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
