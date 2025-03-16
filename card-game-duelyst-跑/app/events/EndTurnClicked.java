package events;



import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;
import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Player;
import structures.basic.Unit;
import commands.BasicCommands;
import ai.AICommand;
import events.Initalize;
import events.CardClicked;



/**

* Indicates that the user has clicked an object on the game canvas, in this case

* the end-turn button.

*

* {

* messageType = “endTurnClicked”

* }

*

* @author Dr. Richard McCreadie

*

*/

public class EndTurnClicked implements EventProcessor{



@Override

public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

System.out.println("[DEBUG - EndTurnClicked] processEvent CALLED ...");

if(gameState.SpellUsing != null)return;

if(gameState.gameFinished)return;

if(gameState.AnimationPlaying)return;

AICommand.AiBehave(out,gameState);

if(gameState.gameFinished == true) return;

gameState.Turns += 1;

BasicCommands.addPlayer1Notification(out,"Your Turn"+gameState.Turns, 2);

Initalize.drawCardForPlayer1(out, gameState, 0); 



gameState.humanPlayer.setMana(Math.min(gameState.Turns+1,9));

BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);

for (Unit unit : gameState.player1Unit) { //  遍历当前玩家所有单位
    unit.setFinishMove(false); //  👈  *重置单位的 hasMovedThisTurn 属性为 false，  允许新回合移动* **
    unit.setFinishAttack(false);
}
CardClicked.clearHighlights(out, gameState, true);

}

}