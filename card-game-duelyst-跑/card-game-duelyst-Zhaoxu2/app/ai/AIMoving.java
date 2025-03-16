package ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import play.libs.Json;
import commands.BasicCommands;
// import commands.UICommands;
// import commands.AtkCommands;
// import commands.PlaceCommands;
// import commands.MoveCommands;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
// import structures.basic.Name;
// import structures.basic.Spells;
import structures.basic.UnitAnimation;
import structures.basic.UnitAnimationType;
import structures.GameState;
import utils.BasicObjectBuilders;

 public class AIMoving{

     public static int aiXmove(int x1, int x2){
        if(x1 > x2) return -1;
        else if(x1 == x2) return  0;
        else return  1;
    }

    public static int aiYmove(int y1, int y2){
        if(y1 > y2) return -1;
        else if(y1 == y2) return  0;
        else return  1;
    }
    public static void move(ActorRef out, GameState gameState, Unit Aunit){
        Unit Punit = gameState.player1Unit.get(0);
        int Ax = Punit.getPosition().getTilex();
        int Ay = Punit.getPosition().getTiley();
        int Px = Aunit.getPosition().getTilex();
        int Py = Aunit.getPosition().getTiley();

        int xn = aiXmove(Px, Ax);
        int yn = aiYmove(Py, Ay);

        // for(int i = 2; i >= 0 && !Punit.checkmove(); --i){
        //     for(int j = 2-i; j >= 0 && !Punit.checkmove(); --j){
        //         int x = i * xn;
        //         int y = j * yn;
        //         if(x == 0 && y == 0) continue;
        //         Tile nTile = BasicObjectBuilders.loadTile(Px + x, Py + y);
        //         MoveCommands.moveAiUnit(out, gameState, Punit, nTile);
        //         if (Punit.checkmove()) {
        //             try {
        //                 Thread.sleep(2000);
        //             } catch (InterruptedException e) {
        //                 e.printStackTrace();
        //             }
        //             if(Punit.getType()==17){
        //                 try {
        //                     Thread.sleep(2000);
        //                 } catch (InterruptedException e) {
        //                     e.printStackTrace();
        //                 }
        //             }


        //         }
        //     }
        // }
    }

    public static void Aimove(ActorRef out, GameState gameState){

        List<Unit> ailist = new ArrayList<>(gameState.player2Unit);
        for(Unit unit: ailist){
            move(out,gameState,unit);
        }
    }
 }