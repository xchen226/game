package events;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

public class UnitDeathEvent implements EventProcessor {

	@Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		
		System.out.println("[DEBUG - UnitDeath.processEvent] Unit death event successfully created.");
		
		int unitId = message.get("id").asInt();
	    System.out.println("[UnitDeathEvent] Unit died with id: " + unitId);
	    
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
	    
	    // 对于每个单位，如果它是 CreatureUnit 的实例，则调用 handleEvent
	    for (Unit unit : allUnits) {
	    	boolean isCreature = unit.isCreature();
	        if (isCreature) {
	            unit.handleEvent(out, gameState, message);
	        }
	    }
    }
}
