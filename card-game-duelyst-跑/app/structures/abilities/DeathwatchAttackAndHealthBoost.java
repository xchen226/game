package structures.abilities;

import structures.GameState;
import structures.basic.Unit;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.UnitDeathEvent;

//Shadow Watcher

public class DeathwatchAttackAndHealthBoost extends Ability {
    // private int attackBoost = 1;
    // private int healthBoost = 1;

    public DeathwatchAttackAndHealthBoost() {
        super("DeathwatchAttackAndHealthBoost");
    }

    @Override
    public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
    	
    	System.out.println("[DEBUG - Shadow Watcher's Ability] DeathwatchAttackAndHealthBoost Alreagy triggered.");
    	    	
    	if (event instanceof JsonNode) {
            JsonNode eventNode = (JsonNode) event;
            if (eventNode.has("messageType") && "unitDeath".equals(eventNode.get("messageType").asText())) {
                // 此处表示传入的事件确实是 "unitSummon" 类型
                System.out.println("[DEBUG - Shadow Watcher's Ability message recieved] This is a unitDeath event.");
                // 在这里添加具体的逻辑代码
                if (owner.getHealth() > 0) {
        			int currentAttack = owner.getAttack();
                    owner.setAttack(currentAttack + 1);
                    int currentHealth = owner.getHealth();
                    owner.setHealth(currentHealth + 1);
                    System.out.println("[DeathwatchAttackAndHealthBoost] Unit " + owner.getId() +
                        " attack increased to " + (currentAttack + 1) +
                        " and health increased to " + (currentHealth + 1));
                    BasicCommands.setUnitAttack(out, owner, owner.getAttack());
                    BasicCommands.setUnitHealth(out, owner, owner.getHealth());
                
        		}
            }
          
        }        
    }
}
