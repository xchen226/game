package events;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.List;

public class UnitSummonEvent implements EventProcessor {

    /**
     * 处理单位召唤事件：
     * 遍历所有玩家单位，将事件传递给每个单位的 handleEvent 方法
     */
    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
    	
    	System.out.println("[DEBUG - SummonUnit.processEvent] Unit summon event successfully created."); 
    	
		int unitId = message.get("id").asInt();
	    System.out.println("[UnitSummonEvent] Unit summoned with id: " + unitId);
	    
	    // 将双方单位都加入一个列表中
	    List<Unit> allUnits = new ArrayList<>();
	    allUnits.addAll(gameState.getPlayer1Unit());
	    allUnits.addAll(gameState.getPlayer2Unit());
	    
	    //打印场上所有unit
	    List<Integer> allUnitsIDs = new ArrayList<>();
	    for (Unit unit : allUnits) {
	    	allUnitsIDs.add(unit.getId());
	    }
	    System.out.println(allUnitsIDs);
	    
	    
	    // 将人类单位都加入一个列表中
	    List<Unit> allplayerUnits = new ArrayList<>();
	    allplayerUnits.addAll(gameState.getPlayer1Unit());	    
	    //打印场上所有unit
	    List<Integer> allplayerUnitsIDs = new ArrayList<>();
	    for (Unit unit : allplayerUnits) {
	    	allplayerUnitsIDs.add(unit.getId());
	    }
	    System.out.println(allplayerUnitsIDs);	    
	    // 将ai单位都加入一个列表中
	    List<Unit> allaiUnits = new ArrayList<>();
	    allaiUnits.addAll(gameState.getPlayer2Unit());
	    	    //打印场上所有unit
	    List<Integer> allaiUnitsIDs = new ArrayList<>();
	    for (Unit unit : allaiUnits) {
	    	allaiUnitsIDs.add(unit.getId());
	    }
	    System.out.println(allaiUnitsIDs);
	    
	    
	    // 对于每个单位，如果它是 CreatureUnit 的实例，则调用 handleEvent
	    for (Unit unit : allUnits) {
	    	boolean isCreature = unit.isCreature();
	        if (isCreature) {
	            unit.handleEvent(out, gameState, message);
	        }
	    }
    }
}
