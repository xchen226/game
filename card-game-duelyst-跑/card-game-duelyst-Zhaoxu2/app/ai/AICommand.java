package ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import play.libs.Json;
import commands.BasicCommands;
// import commands.UICommands;
// import commands.AtkCommands;
// import commands.PlaceCommands;
// import commands.MoveCommands;
// import structures.ai.AiCommand;
// import structures.ai.AiMoving;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
// import structures.basic.Spells;
import structures.basic.UnitAnimation;
import structures.basic.UnitAnimationType;
import structures.GameState;

import utils.BasicObjectBuilders;
public class AICommand{
    // public static void AiPlace(ActorRef out, GameState gameState){
    //     boolean done = false;
    //     while(done == false) {
    //         for (Card card : gameState.aiHands) {
    //             if (card.getIsCreature()) {
    //                 if (card.getManacost() <= gameState.aiPlayer.getMana()) {

    //                     Tile tile = PlaceCommands.getaiPlacement(out, gameState);
    //                     if(tile == null){done = true; break;}
    //                     PlaceCommands.useAiCard(out, gameState, card, tile);
    //                     try {
    //                         Thread.sleep(2000);
    //                     } catch (InterruptedException e) {
    //                         e.printStackTrace();
    //                     }

    //                     break;
    //                 }
    //             }


    //         }
    //         done = true;
    //     }
    // }
    // public static void AiSpell(ActorRef out, GameState gameState){
    //     boolean done = false;
    //     while(done == false) {
    //         for (Card card : gameState.aiHands) {
    //             if (!card.getIsCreature()) {
    //                 if (card.getManacost() <= gameState.aiPlayer.getMana()) {

    //                     if(Spells.AiSpell(out,gameState,card)){
    //                         gameState.aiHands.remove(card);
    //                         gameState.aiPlayer.setMana(gameState.aiPlayer.getMana() - card.getManacost());
    //                         BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
    //                         try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}




    //                         break;
    //                     }

    //                 }
    //             }


    //         }
    //         done = true;
    //     }
    // }
    public static void AiBehave(ActorRef out, GameState gameState){

        BasicCommands.addPlayer1Notification(out,"Ai Turn"+gameState.Turns, 2);
        // if(gameState.player1CardUsed<20){
        //     gameState.player2CardUsed++;
        //     gameState.aiHands.add(gameState.player2Cards.get(gameState.player2CardUsed));
        // }
        gameState.aiPlayer.setMana(Math.min(gameState.Turns+1,9));
        BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        // AiSpell(out,gameState);
        // AiPlace(out,gameState);
        // AIAttack.Aiatk(out,gameState);
        if(gameState.gameFinished == true) return;
        // AIMoving.Aimove(out,gameState);
        BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
    }

}