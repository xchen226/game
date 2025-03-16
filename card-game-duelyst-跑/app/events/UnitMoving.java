package events;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.abilities.Ability;
import structures.abilities.Flying;
import structures.abilities.Provoke;

public class UnitMoving implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        System.out.println("UnitMoving event triggered: " + message);

        if (!message.has("unitID") || !message.has("tilex") || !message.has("tiley")) {
            System.err.println("unitMoving event missing required fields: " + message);
            return;
        }

        int unitID = message.get("unitID").asInt();
        int tilex  = message.get("tilex").asInt();
        int tiley  = message.get("tiley").asInt();

        Unit selectedUnit = findUnitById(gameState, unitID);
        if (selectedUnit == null) {
            System.err.println("No unit found with ID = " + unitID);
            return;
        }

        if (!gameState.checkmaprange(tilex, tiley)) {
            System.err.println("Target tile out of range: (" + tilex + ", " + tiley + ")");
            return;
        }

        Tile targetTile = gameState.tileMap[tilex][tiley];
        if (targetTile == null) {
            System.err.println("No Tile object found in tileMap at (" + tilex + ", " + tiley + ")");
            return;
        }

        Unit occupant = gameState.checkmap(tilex, tiley);
        if (occupant != null) {
            System.err.println("Tile occupied by unit ID=" + occupant.getId());
            return;
        }

        if ((!isInMoveRange(selectedUnit, tilex, tiley))||Provoke.isStunned(selectedUnit, gameState)) { //lu:加了stunned判定
            System.err.println("Unit ID=" + unitID + " can't move to (" + tilex + ", " + tiley + ") - out of range");
            return;
        }

        BasicCommands.moveUnitToTile(out, selectedUnit, targetTile);
        BasicCommands.playUnitAnimation(out, selectedUnit, UnitAnimationType.move);

        int oldX = selectedUnit.getPosition().getTilex();
        int oldY = selectedUnit.getPosition().getTiley();
        gameState.clearmap(oldX, oldY);

        selectedUnit.setPositionByTile(targetTile);
        gameState.setmap(tilex, tiley, selectedUnit);

        selectedUnit.setHasMoved(true);

        System.out.println("Unit " + unitID + " moved to (" + tilex + ", " + tiley + ").");
    }

    private Unit findUnitById(GameState gameState, int unitID) {
        for (Unit u : gameState.player1Unit) {
            if (u.getId() == unitID) return u;
        }
        for (Unit u : gameState.player2Unit) {
            if (u.getId() == unitID) return u;
        }
        return null;
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
    
}
