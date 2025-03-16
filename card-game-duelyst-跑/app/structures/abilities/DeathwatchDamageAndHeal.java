package structures.abilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.basic.Player;
import events.UnitDeathEvent;
import play.libs.Json;


/**
 * Shadowdancer
 * 当其他单位死亡时，触发此能力，使拥有该能力的单位对敌方 Avatar（即敌方玩家）造成 1 点伤害，
 * 同时自身恢复 1 点生命值。
 */
public class DeathwatchDamageAndHeal extends Ability {

    public DeathwatchDamageAndHeal() {
        super("DeathwatchDamageAndHeal");
    }
    
    /**
     * 当事件触发时调用此方法。
     * @param owner 拥有此能力的单位
     * @param event 事件对象（预期为 UnitDeathEvent）
     */
    @Override
    public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
    	
    	System.out.println("[DEBUG - Shadowdancer's Ability] DeathwatchDamageAndHeal Alreagy triggered.");
    	
    	if (event instanceof JsonNode) {
            JsonNode eventNode = (JsonNode) event;
            if (eventNode.has("messageType") && "unitDeath".equals(eventNode.get("messageType").asText())) {
                // 此处表示传入的事件确实是 "unitSummon" 类型
                System.out.println("[DEBUG - Shadowdancer's Ability message recieved] This is a unitDeath event.");
                // 在这里添加具体的逻辑代码
                if (owner.getHealth() > 0) { 
                    Player ownerPlayer = gameState.humanPlayer; 
                    Player enemyPlayer = gameState.aiPlayer; 
 
                    
                    // 我方英雄+1血
                    if(ownerPlayer.getHealth() < 20) {
                    	ownerPlayer.setHealth(ownerPlayer.getHealth() + 1);
                    	gameState.playerAvatar.setHealth(gameState.playerAvatar.getHealth()+ 1);
                    	BasicCommands.setPlayer1Health(out, ownerPlayer);
                    }else {
                    	System.out.println("[Shadowdancer's Ability] Player is at maxHealth. Ability has no effect.");
                    }
                    
                    // 敌Avatar-1血
                    int newAiAvatarHealth = gameState.aiAvatar.getHealth() - 1;
                    if (newAiAvatarHealth < 0) {
                    	newAiAvatarHealth = 0;
                    }
                    
                    enemyPlayer.setHealth(newAiAvatarHealth);//ai玩家-1血
                    gameState.aiAvatar.setHealth(newAiAvatarHealth);//ai英雄-1血
                    BasicCommands.setUnitHealth(out, gameState.aiAvatar, newAiAvatarHealth);
                    BasicCommands.setPlayer2Health(out, enemyPlayer);
                    
                                       
                    
                    System.out.println("[DEBUG - Shadowdancer's Ability] aiAvatar " + gameState.aiAvatar.getId() + " Health decreased by 1." + " New Health: " + newAiAvatarHealth);
                    
                    
                    if (newAiAvatarHealth <= 0) {
                        BasicCommands.playUnitAnimation(out, gameState.aiAvatar, UnitAnimationType.death);
                        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                        BasicCommands.deleteUnit(out, gameState.aiAvatar);
                        int ax = gameState.aiAvatar.getPosition().getTilex();
                        int ay = gameState.aiAvatar.getPosition().getTiley();
                        gameState.clearmap(ax, ay);
                                    
                        //lu
                        gameState.delPlay1Unit(gameState.aiAvatar);
                        gameState.delPlay2Unit(gameState.aiAvatar);
                        gameState.removeUnit(gameState.aiAvatar);
                    
                    /*
                    //这里还要加敌方avatar死亡的语句
                    if (gameState.aiAvatar.getHealth() <= 0) {
                        BasicCommands.playUnitAnimation(out, gameState.aiAvatar, UnitAnimationType.death);
                        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                        BasicCommands.deleteUnit(out, gameState.aiAvatar);
                        int dx = gameState.aiAvatar.getPosition().getTilex();
                        int dy = gameState.aiAvatar.getPosition().getTiley();
                        gameState.clearmap(dx, dy);
                        gameState.delPlay1Unit(gameState.aiAvatar);
                        gameState.removeUnit(gameState.aiAvatar);
                        
                        //缺游戏胜利的语句
                     */
                        
                        System.out.println("[DEBUG - Combat - Defender Dead] Defender " + gameState.aiAvatar.getId() + " is dead!");
                        return;
                    }

                    
                    System.out.println("[DeathwatchDamageAndHeal] Unit " + owner.getId() +
                        " triggers ability: enemy health decreased by 1, owner healed by 1.");
                           
                }
            }
        }
    }
    
}
