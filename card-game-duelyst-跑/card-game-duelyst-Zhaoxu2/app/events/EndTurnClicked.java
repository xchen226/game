package events;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;
import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Player;
import commands.BasicCommands;
// import commands.UICommands;
// import commands.AtkCommands;
// import commands.PlaceCommands;
// import commands.MoveCommands;
// import utils.BasicObjectBuilders;
// import structures.basic.Card;
// import structures.basic.Unit;
import ai.AICommand;
/**
 * Indicates that the user has clicked an object on the game canvas, in this case
 * the end-turn button.
 * 
 * { 
 *   messageType = “endTurnClicked”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class EndTurnClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		if(gameState.SpellUsing != null)return;
		if(gameState.gameFinished)return;
		if(gameState.AnimationPlaying)return;
		// UICommands.resetTile(out,gameState);
		// UICommands.resetHand(out,gameState);
		// gameState.humanPlayer.setMana(0);
		// BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
		AICommand.AiBehave(out,gameState);
		if(gameState.gameFinished == true) return;
		gameState.Turns += 1;
		BasicCommands.addPlayer1Notification(out,"Your Turn"+gameState.Turns, 2);
		gameState.humanPlayer.setMana(Math.min(gameState.Turns+1,9));
		BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);

		// if(gameState.playerHands.size()<6&&gameState.player1CardUsed<20) {//Check if card in player hands out of range
		// 	gameState.player1CardUsed++;
		// 	gameState.playerHands.add(gameState.player1Cards.get(gameState.player1CardUsed));
		// 	BasicCommands.drawCard(out, gameState.player1Cards.get(gameState.player1CardUsed), gameState.playerHands.size(), 0);
		// }

		// for(Unit unit: gameState.player1Unit) {
		// 	unit.resetmove();
		// 	unit.resetatk();
		// 	unit.decstun();
		// }
		// for(Unit unit: gameState.player2Unit) {
		// 	unit.resetmove();
		// 	unit.resetatk();
		// 	unit.decstun();

		// }
		// gameState.isTileClicked = null;

	}

}
