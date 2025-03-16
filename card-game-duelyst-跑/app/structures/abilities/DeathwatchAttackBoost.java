package structures.abilities;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.UnitDeathEvent;
import structures.GameState;
import structures.basic.Unit;

/**
 * Bad Omen
 * DeathwatchAttackBoost 能力：当其他单位死亡时，触发此能力，
 * 使拥有该能力的单位获得永久性 +1 攻击力。
 */
public class DeathwatchAttackBoost extends Ability {
	// private int boost = 1;
    
    public DeathwatchAttackBoost() {
        super("DeathwatchAttackBoost");
    }
    
    @Override
    public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
    	
    	System.out.println("[DEBUG - Bad Omen's Ability] DeathwatchAttackBoost Alreagy triggered.");
    	
    	if (event instanceof JsonNode) {
            JsonNode eventNode = (JsonNode) event;
            if (eventNode.has("messageType") && "unitDeath".equals(eventNode.get("messageType").asText())) {
                // 此处表示传入的事件确实是 "unitSummon" 类型
                System.out.println("[DEBUG - Bad Omen's Ability message recieved] This is a unitDeath event.");
                // 在这里添加具体的逻辑代码
                if (owner.getHealth() > 0) {
            	    int currentAttack = owner.getAttack();
                    owner.setAttack(currentAttack + 1);
                    System.out.println("[DeathwatchAttackBoost] Unit " + owner.getId() + " attack increased to " + (currentAttack + 1));
                    BasicCommands.setUnitAttack(out, owner, owner.getAttack());
                }
            }    
        }        
    }
}

