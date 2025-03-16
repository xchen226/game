// package ai;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;
// import java.util.ArrayList;
// import java.util.List;

// import akka.actor.ActorRef;
// import play.libs.Json;
// import commands.BasicCommands;
// import commands.UICommands;
// import commands.AtkCommands;
// import commands.PlaceCommands;
// import commands.MoveCommands;
// import structures.basic.Card;
// import structures.basic.EffectAnimation;
// import structures.basic.Player;
// import structures.basic.Tile;
// import structures.basic.Unit;
// import structures.basic.Name;
// import structures.basic.Spells;
// import structures.basic.UnitAnimation;
// import structures.basic.UnitAnimationType;
// import structures.GameState;
// import utils.BasicObjectBuilders;
// public class AIAttack{


//     public static void Aiatk(ActorRef out, GameState gameState){

//         List<Unit> ailist = new ArrayList<>(gameState.player2Unit);
//         for(Unit unit: ailist){
//             move_and_atk(out,gameState,unit);
//             if(gameState.gameFinished == true) return;
//         }
//     }


//     public static void move_and_atk(ActorRef out, GameState gameState, Unit Punit){
//         List<Unit> playerlist = new ArrayList<>(gameState.player1Unit);
//         for(Unit unit:playerlist){
//             int tilex = unit.getPosition().getTilex();
//             int tiley = unit.getPosition().getTiley();
//             Tile thisTile = BasicObjectBuilders.loadTile(tilex, tiley);
//             if(Punit.getAtk()>0)AtkCommands.attack(out,gameState,Punit,thisTile);


//             if((!Punit.checkatk()) && (!Punit.checkmove())) {

//                // if(Punit.getAtk()>0)AtkCommands.attack(out, gameState, Punit, thisTile);

//                 if (gameState.checkmap(tilex, tiley) != null) {
//                     for (int i = -1; (i <= 1) && (!Punit.checkmove()); ++i) {
//                         for (int j = -1; (j <= 1) && (!Punit.checkmove()); ++j) {
//                             if (i != 0 || j != 0) {


//                                 Tile nTile = BasicObjectBuilders.loadTile(tilex + i, tiley + j);
//                                 if(AtkCommands.attackable(gameState,Punit,nTile,thisTile)){
//                                     //	if (gameState.checkmap(tilex, tiley).getOwner() != Punit.getOwner()) {
//                                     MoveCommands.moveAiUnit(out, gameState, Punit, nTile);
//                                     if (Punit.checkmove()) {
//                                         try {
//                                             Thread.sleep(2000);
//                                         } catch (InterruptedException e) {
//                                             e.printStackTrace();
//                                         }
//                                         if(Punit.getType()==17){
//                                             try {
//                                                 Thread.sleep(2000);
//                                             } catch (InterruptedException e) {
//                                                 e.printStackTrace();
//                                             }
//                                         }

//                                         if(Punit.getAtk()>0)AtkCommands.attack(out, gameState, Punit, thisTile);

//                                     }
//                                 }
//                             }
//                         }
//                     }
//                 }


//             }
//         }

//     }
// }