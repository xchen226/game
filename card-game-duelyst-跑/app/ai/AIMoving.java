package ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import play.libs.Json;
import commands.BasicCommands;
import events.TileClicked;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.SpellCard;
import structures.basic.UnitAnimation;
import structures.basic.UnitAnimationType;
import structures.GameState;
import structures.abilities.Flying;
import structures.abilities.Provoke;
import utils.BasicObjectBuilders;

 public class AIMoving{


    
    public static void AImove(ActorRef out, GameState gameState, Unit Aunit){

        if (gameState == null) {
            System.err.println("[DEBUG - AIMoving] gameState is null!");
            return;
        }
        if (gameState.aiAvatar == null) {
            System.err.println("[DEBUG - AIMoving] gameState.aiAvatar is null!");
            return;
        }
        if (Aunit == null) {
            System.err.println("[DEBUG - AIMoving] Aunit (passed argument) is null!");
            return;
        }
        if (Aunit.getPosition() == null) {
            System.err.println("[DEBUG - AIMoving] Aunit's position is null!");
            return;
        }
        if (gameState.tileMap == null) {
            System.err.println("[DEBUG - AIMoving] gameState.tileMap is null!");
            return;
        }
        
        // lu：如果单位受到 Provoke 限制（即被敌方的 Provoke 能力影响），则不能移动
        if (Provoke.isStunned(Aunit, gameState)) {
            System.out.println("[DEBUG - AIMoving] Unit " + Aunit.getId() + " is provoked and cannot move.");
            BasicCommands.addPlayer1Notification(out, "Unit " + Aunit.getId() + " is provoked and cannot move!", 2);
            return;
        }
        
        Unit Punit = gameState.playerAvatar;
        int Px = Punit.getPosition().getTilex();
        int Py = Punit.getPosition().getTiley();
        int Ax = Aunit.getPosition().getTilex();
        int Ay = Aunit.getPosition().getTiley();

        // 计算朝向玩家的移动方向
        int dx = Integer.compare(Px, Ax); // x方向 (-1=左, 1=右, 0=不变)
        int dy = Integer.compare(Py, Ay); // y方向 (-1=上, 1=下, 0=不变)

        Tile oldtile = gameState.tileMap[Ax][Ay];
        Tile newtile = gameState.tileMap[Ax + dx][Ay + dy];


        for(Unit unit : gameState.player2Unit){
            // 优先尝试移动
            if (canMoveTo(gameState, Ax + dx, Ay + dy)) {
                moveUnit(out, gameState, unit, oldtile, newtile);
                return;
            }
        }
    
        System.out.println("[AI] No valid move found.");
        return;
    }

        
    
        // 检查 AI 是否可以移动到目标位置
        private static boolean canMoveTo(GameState gameState, int x, int y) {
            return gameState.checkmaprange(x, y) && gameState.checkmap(x, y) == null;
        }
        
        
        // 移动单位
        private static void moveUnit(ActorRef out, GameState gameState, Unit unit, Tile oldtile, Tile newtile) {

            int oldX = oldtile.getTilex();
			int oldY = oldtile.getTiley();
            int newX = newtile.getTilex();
			int newY = newtile.getTiley();

			unit.setPositionByTile(newtile);
            BasicCommands.moveUnitToTile(out, unit, newtile);
            BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.move);//lu

            gameState.clearmap(oldX, oldY);
            unit.setPositionByTile(newtile);//lu
			gameState.setmap(newX, newY, unit);
            System.out.println("[AI] Moved to (" + newX + ", " + newY + ")");
        }

    public static void Aimove(ActorRef out, GameState gameState){

        List<Unit> ailist = new ArrayList<>(gameState.player2Unit);
        for(Unit unit: ailist){
            AImove(out,gameState,unit);
            try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}//lu
        }
    }
}