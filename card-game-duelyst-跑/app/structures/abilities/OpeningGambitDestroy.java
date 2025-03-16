package structures.abilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.UnitDeathEvent;
import events.UnitSummonEvent;
import play.libs.Json;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.Tile;
import structures.basic.Player;
import structures.basic.UnitAnimationType;
import java.util.ArrayList;
import java.util.List;

//Nightsorrow Assassin

public class OpeningGambitDestroy extends Ability {

    public OpeningGambitDestroy() {
        super("OpeningGambitDestroy");
    }

    @Override
    public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
    	
    	System.out.println("[DEBUG - Nightsorrow Assassin's Ability] OpeningGambitDestroy Alreagy triggered.");
    	
    	if (event instanceof JsonNode) {
            JsonNode eventNode = (JsonNode) event;
            if (eventNode.has("messageType") && "unitSummon".equals(eventNode.get("messageType").asText())) {
                // 此处表示传入的事件确实是 "unitSummon" 类型
                System.out.println("[DEBUG - Nightsorrow Assassin's Ability message recieved] This is a unitSummon event.");
                // 在这里添加具体的逻辑代码
                System.out.println("OpeningGambitDestroy triggered for unit " + owner.getId());
                // 获取拥有该能力的单位在棋盘上的索引位置
                int ownerX = owner.getPosition().getTilex();
                int ownerY = owner.getPosition().getTiley();
                Unit targetToDestroy = null;
                
                // 遍历周围8个方向
                outerLoop:
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        // 跳过自身位置
                        if (dx == 0 && dy == 0) continue;
                        int newX = ownerX + dx;
                        int newY = ownerY + dy;
                        // 检查是否在棋盘范围内
                        if (!gameState.checkmaprange(newX, newY)) continue;
                        // 获取该格上的单位（如果有的话）
                        Unit neighbor = gameState.checkmap(newX, newY);
                        if (neighbor != null) {
                            // 判断该 neighbor 是否为敌方单位
                            boolean isEnemy = false;
                           // 假设如果 owner 属于 humanPlayer，则敌方单位在 player2Unit 中，反之亦然
                            if (gameState.player1Unit.contains(neighbor)) { // neighbor属于humanPlayer
                                isEnemy = false;
                            } else {
                                isEnemy = true;// neighbor不属于humanPlayer
                            }
                            if (isEnemy) {
                                // 通过 maxHealth 判断是否低于满血状态
                                if (neighbor.getHealth() < neighbor.getMaxHealth()) {
                                    targetToDestroy = neighbor;
                                    break outerLoop;
                                }
                            }
                        }
                    }
                }
                if (targetToDestroy != null) {
                    System.out.println("[OpeningGambitDestroy] Destroying enemy unit with id: " + targetToDestroy.getId());
                    // 播放死亡动画
                    BasicCommands.playUnitAnimation(out, targetToDestroy, UnitAnimationType.death);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 从 UI 上删除单位
                    BasicCommands.deleteUnit(out, targetToDestroy);
                    int ax = targetToDestroy.getPosition().getTilex();
                    int ay = targetToDestroy.getPosition().getTiley();
                    gameState.clearmap(ax, ay);
                    // 更新 GameState，将该单位从棋盘和单位列表中移除
                    gameState.delPlay1Unit(targetToDestroy);
                    gameState.removeUnit(targetToDestroy);
                    
                    if(targetToDestroy.equals(gameState.aiAvatar)) {
                    	System.out.println("[OpeningGambitDestroy] aiAvatar destroyed. Human Wins. " );
                    }else {
                    	// 构造死亡事件的 JSON 消息
                        ObjectNode deathMsg = Json.newObject();
                        deathMsg.put("messageType", "unitDeath"); // 指定事件类型为单位死亡
                        deathMsg.put("id", targetToDestroy.getId());     // 将死亡单位的ID加入消息中
                        // 创建 UnitDeathEvent 事件处理器实例，并调用 processEvent 方法
                        UnitDeathEvent deathEvent = new UnitDeathEvent();
                        deathEvent.processEvent(out, gameState, deathMsg);
                    }                   
                    
                } else {
                    System.out.println("[OpeningGambitDestroy] No adjacent damaged enemy unit found to destroy.");
                }
            }
    	}
            
    }
}
    


