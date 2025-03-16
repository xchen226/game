package structures.abilities;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

public class OpeningGambittAlliesBoost extends Ability {

	public OpeningGambittAlliesBoost() {
		super("OpeningGambittAlliesBoost");
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
	    System.out.println("[DEBUG - Silverguard Squire's Ability] OpeningGambittAlliesBoost already triggered.");
	    
    	
    	if (event instanceof JsonNode) {
            JsonNode eventNode = (JsonNode) event;
            if (eventNode.has("messageType") && "unitSummon".equals(eventNode.get("messageType").asText())) {
                // 此处表示传入的事件确实是 "unitSummon" 类型
                System.out.println("[DEBUG - Silverguard Squire's Ability message recieved] This is a unitSummon event.");
                // 在这里添加具体的逻辑代码
                // 假设该能力隶属于 owning player's avatar，
        	    // 通常 owner 参数就是拥有该能力的单位，但这里我们直接使用 gameState.playerAvatar 作为参照。
        	    Unit avatar = gameState.aiAvatar;
        	    if (avatar == null) {
        	        System.err.println("[DEBUG - Silverguard Squire's Ability] Ai avatar is null.");
        	        return;
        	    }
        	    
        	    // 获取头像所在的棋盘坐标
        	    int avatarX = avatar.getPosition().getTilex();
        	    int avatarY = avatar.getPosition().getTiley();
        	    
        	    // 定义要检查的左右两个方向
        	    int[] dx = {-1, 1};
        	    for (int d : dx) {
        	        int targetX = avatarX + d;
        	        int targetY = avatarY; // 同一行
        	        if (!gameState.checkmaprange(targetX, targetY)) continue;  // 超出棋盘范围跳过
        	        
        	        // 获取该相邻格子的单位
        	        Unit adjacent = gameState.checkmap(targetX, targetY);
        	        if (adjacent != null) {
        	            // 判断是否为同盟单位。假设：
        	            // 如果头像在 player1Unit 列表中，则相邻单位也必须在 player1Unit 中；
        	            // 否则，若头像在 player2Unit 中，则相邻单位必须在 player2Unit 中。
        	            if (gameState.player2Unit.contains(adjacent)) {
        	                // 给该单位增加永久 +1 攻击和 +1 生命	            	
        	                int newAttack = adjacent.getAttack() + 1;
        	                int newHealth = adjacent.getHealth() + 1;
        	                int newMaxHealth = adjacent.getMaxHealth() + 1;
        	                adjacent.setAttack(newAttack);
        	                adjacent.setHealth(newHealth);
        	                adjacent.setMaxHealth(newMaxHealth);
        	                
        	                // 调用 UI 更新命令
        	                BasicCommands.setUnitAttack(out, adjacent, newAttack);
        	                BasicCommands.setUnitHealth(out, adjacent, newHealth);	                
        	                BasicCommands.setMaxHealth(out, adjacent, newMaxHealth);
        	                
        	                System.out.println("[DEBUG - Opening Gambit] Buffed unit " + adjacent.getId() +
        	                        " at (" + targetX + "," + targetY + ") to attack " + newAttack +
        	                        " and health " + newHealth);
        	            }
        	        }
        	    }
        	}
    	}    
	}
}
