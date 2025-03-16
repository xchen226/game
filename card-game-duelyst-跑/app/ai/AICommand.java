package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actors.*;
import akka.actor.ActorRef;
import play.libs.Json;
import commands.BasicCommands;
import events.TileClicked;
// import commands.UICommands;
// import commands.AtkCommands;
// import commands.PlaceCommands;
// import commands.MoveCommands;
// import structures.ai.AiCommand;
// import structures.ai.AiMoving;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import structures.basic.SpellCard;
import structures.basic.Tile;
import structures.basic.Unit;
// import structures.basic.Spells;
import structures.basic.UnitAnimation;
import structures.basic.UnitAnimationType;
import structures.GameState;
import utils.SummonUnitOnBoard;
import utils.BasicObjectBuilders;
public class AICommand{


    public static void aiDrawCard(GameState gameState) {
    	
    	System.out.println("[DEBUG] aiDrawCard start.");
    	
        // AI 手牌最多 6 张
        if (gameState.aiHands.size() >= 6) {
            System.out.println("[DEBUG] AI hand is full, cannot draw more cards.");
            return;
        }
    
        // 抽卡逻辑
        if (gameState.Turns == 1) {
            // 第一回合抽 3 张
            for (int i = 0; i < 3; i++) {
                drawCardForAI(gameState);
            }
        } else {
            // 其他回合每回合抽 1 张
            drawCardForAI(gameState);
        }
    }
                            
    // 从 AI 牌库里抽卡
    private static void drawCardForAI(GameState gameState) {
    	Random random = new Random();
    	int index = random.nextInt(gameState.player2Cards.size()); // 随机索引
    	Card drawnCard = gameState.player2Cards.remove(index); // 随机抽一张并移出牌库
    	gameState.aiHands.add(drawnCard); // 加入 AI 手牌
    	System.out.println("[DEBUG] AI drew card: " + drawnCard.getCardname());
    }
        
    
    public static void aiPlayCards(GameState gameState, ActorRef out) {
        System.out.println("[DEBUG] AI is playing cards...");
    
        List<Card> playableCards = new ArrayList<>();
        
        // 筛选 AI 可以打出的卡（法力足够的）
        for (Card card : gameState.aiHands) {
            if (card.getManacost() <= gameState.aiPlayer.getMana()) {
                playableCards.add(card);
            }
        }
    
        // 按法力消耗从高到低排序（先使用最贵的卡）
        playableCards.sort(Comparator.comparingInt(Card::getManacost).reversed());

    
        for (Card card : playableCards) {
            if ((card.getIsCreature()==true)&&(gameState.aiPlayer.getMana() > 0)) {
                playUnitCard(out, gameState, card);
            } else if (card instanceof SpellCard) {
                // playSpellCard(out, gameState, (SpellCard) card);
            }
        }
    }
                                
    // 处理单位卡（如果场上有空位）
    private static void playUnitCard(ActorRef out, GameState gameState, Card card) {
        int tilex, tiley;
        
        if(gameState.Turns % 2 == 0){
            tilex = gameState.aiAvatar.getPosition().getTilex();
            tiley = gameState.aiAvatar.getPosition().getTiley()+1;
        }
        else{
            tilex = gameState.aiAvatar.getPosition().getTilex();
            tiley = gameState.aiAvatar.getPosition().getTiley()-1;
        }
        
        //lu
        // 循环检查预定 tile 及其右侧空闲格子
        Tile tile = null;
        while (gameState.checkmaprange(tilex, tiley)) {
            tile = gameState.tileMap[tilex][tiley];
            if (tile != null && gameState.checkmap(tilex, tiley) == null) {
                // 找到一个空 tile
                break;
            }
            // 如果当前 tile 不适合，则向右移动
            tilex++;
        }
        
        // 如果跳出循环后，tile 仍然为 null，表示没有找到合适位置
        if (tile == null || !gameState.checkmaprange(tilex, tiley)) {
            System.err.println("[DEBUG] No valid tile found for summoning unit card: " + card.getCardname());
            return;
        }
        
        System.out.println("[DEBUG] AI plays unit card: " + card.getCardname() + " at (" + tilex + ", " + tiley + ")");
        Unit summonedUnit = SummonUnitOnBoard.summonUnit(out, gameState, tile, tilex, tiley, card);
        gameState.player2Unit.add(summonedUnit);
        System.err.println("[DEBUG] player2Unit: " + gameState.player2Unit);
        gameState.setmap(tilex, tiley, summonedUnit);
        gameState.player2Cards.remove(card);        
        gameState.aiHands.remove(card);
        //lu
        
        /*
         * 原        
        Tile tile = gameState.tileMap[tilex][tiley];
        if (tile != null) {
            System.out.println("[DEBUG] AI plays unit card: " + card.getCardname());
            SummonUnitOnBoard.summonUnit(out, gameState, tile, tilex, tiley, card);
            gameState.player2Cards.remove(card); // 用完就移除
        }
        */

        gameState.aiPlayer.setMana(gameState.aiPlayer.getMana()-card.getManacost());
        BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);//lu
		//原BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);

    }
            
            // // 处理法术卡（如果有合适的目标）
            // private void playSpellCard(ActorRef out, GameState gameState, SpellCard card) {
            //     Unit target = gameState.getBestSpellTarget();
            //     if (target != null) {
            //         System.out.println("[DEBUG] AI casts spell: " + card.getName() + " on " + target.getName());
            //         gameState.aiCastSpell(out, target, card);
            //         gameState.aiCards.remove(card); // 用完就移除
            //     }
            // }
            
            
    public static void AiBehave(ActorRef out, GameState gameState){  	

    	System.out.println("[DEBUG] AiBehave");
        BasicCommands.addPlayer1Notification(out,"Ai Turn"+gameState.Turns, 2);
        // if(gameState.player1CardUsed<20){
        //     gameState.player2CardUsed++;
        //     gameState.aiHands.add(gameState.player2Cards.get(gameState.player2CardUsed));
        // }

        // GameActor.images.add("assets/game/extra/ui/button_end_turn_enemy.png"); // 灰色按钮（敌方回合）
        gameState.aiPlayer.setMana(Math.min(gameState.Turns+1,9));
        BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

        if(gameState.gameFinished == true) return;

        aiDrawCard(gameState);
        aiPlayCards(gameState, out);

        //AIMoving.AImove(out,gameState,gameState.aiAvatar);
        AIMoving.Aimove(out,gameState);//lu
        // AiSpell(out,gameState);
        // AiPlace(out,gameState);
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        AIAttack.Aiatk(out,gameState);
        BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
    }

}
    
