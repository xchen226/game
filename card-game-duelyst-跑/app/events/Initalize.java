package events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;
import utils.StaticConfFiles;

/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to receive commands from the back-end.
 * { messageType = "initalize" }
 */
public class Initalize implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                gameState.map[i][j] = null;
                Tile tile = BasicObjectBuilders.loadTile(i, j);
                gameState.tileMap[i][j] = tile;
                BasicCommands.drawTile(out, tile, 0);
            }
        }
        gameState.gameInitalised = true;
        gameState.humanPlayer = new Player(20, 2);
        gameState.aiPlayer = new Player(20, 2);

        BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
        BasicCommands.setPlayer2Health(out, gameState.aiPlayer);
        BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
        BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);

        gameState.player1Unit = new ArrayList<>();
        gameState.player2Unit = new ArrayList<>();
        BasicCommands.addPlayer1Notification(out, "drawplayerAvatar", 2);
        Unit playerAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, gameState.getNextUnitID(), Unit.class);
        

        playerAvatar.setHealth(20);
        playerAvatar.setAttack(2);
        playerAvatar.setMaxHealth(20);
        playerAvatar.setPositionByTile(gameState.tileMap[1][2]);
        BasicCommands.drawUnit(out, playerAvatar, gameState.tileMap[1][2]);
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

        BasicCommands.addPlayer1Notification(out, "drawaiAvatar", 2);
        Unit aiAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, gameState.getNextUnitID(), Unit.class);
        

        aiAvatar.setHealth(20);
        aiAvatar.setAttack(2);
        aiAvatar.setMaxHealth(20);
        aiAvatar.setPositionByTile(gameState.tileMap[7][2]);
        BasicCommands.drawUnit(out, aiAvatar, gameState.tileMap[7][2]);
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

        BasicCommands.addPlayer1Notification(out, "setUnitAttack", 2);
        BasicCommands.setUnitAttack(out, playerAvatar, 2);
        BasicCommands.setUnitAttack(out, aiAvatar, 2);
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

        BasicCommands.addPlayer1Notification(out, "setUnitHealth", 2);
        BasicCommands.setUnitHealth(out, playerAvatar, 20);
        BasicCommands.setUnitHealth(out, aiAvatar, 20);
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

        gameState.setAiAvatar(aiAvatar);
        gameState.setPlayerAvatar(playerAvatar);
        gameState.setmap(1, 2, playerAvatar);
        gameState.setmap(7, 2, aiAvatar);

        gameState.player1Unit.add(playerAvatar);
        gameState.player2Unit.add(aiAvatar);
        
        //lu：初始化玩家牌库
        gameState.player1Cards = new ArrayList<>(OrderedCardLoader.getPlayer1Cards(1));
        gameState.player2Cards = new ArrayList<>(OrderedCardLoader.getPlayer2Cards(1));

        //lu
        
        //原gameState.player1Cards = new ArrayList<>();
        //原gameState.player2Cards = new ArrayList<>();
        gameState.playerHands = new ArrayList<>();
        gameState.aiHands = new ArrayList<>();

        gameState.player2Cards.addAll(OrderedCardLoader.getPlayer2Cards(2));
        

        List<Card> cards = new ArrayList<Card>();
        cards.addAll(OrderedCardLoader.getPlayer1Cards(1));
        for (int i = 1; i <= 3; i++) {
            Random random = new Random();
            int index = random.nextInt(cards.size());
            Card card = cards.get(index);
            BasicCommands.drawCard(out, card, i, 0);
            try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
            gameState.playerHands.add(card);
        }
    }

    public static void drawCardForPlayer1(ActorRef out, GameState gameState, int targetPosition) {
    	
        // lu:检查手牌是否已满（例如限制为6张）
        if (gameState.playerHands.size() >= 6) {
            System.out.println("[DEBUG - Card Draw] Player 1's hand is full. Cannot draw more cards.");
            return;
        }
        // 直接从 gameState.player1Cards 中抽卡（假设已经在 gameState 中初始化了牌库）
        if (gameState.player1Cards.isEmpty()) {
            System.out.println("[WARN - Card Draw] Player 1's deck is empty. Cannot draw card.");
            return;
        }
    	
        /*原
        List<Card> cards = new ArrayList<Card>();
        cards.addAll(OrderedCardLoader.getPlayer1Cards(1));
        if (cards.isEmpty()) {
            System.out.println("[WARN - Card Draw] Player 1's deck is empty. Cannot draw card.");
            return;
        }
        */
    
        Random random = new Random();
        
        //lu
        int index = random.nextInt(gameState.player1Cards.size());
        Card card = gameState.player1Cards.remove(index); // 从牌库中移除卡牌
        // 注意：这里不再重新赋值给 gameState.player1Cards，因为我们直接操作了该列表
        //lu
        
        /*
         * 原
         * int index = random.nextInt(cards.size());
        Card card = cards.get(index); 
        cards.remove(index);
        
        gameState.player1Cards = cards;
        */
    
        int cardPositionToDraw; 
        if (targetPosition > 0) {
            cardPositionToDraw = targetPosition;
            System.out.println("[DEBUG - Card Draw] Using target position for drawCard: " + targetPosition);
        } else {
            cardPositionToDraw = gameState.playerHands.size() + 1; 
            System.out.println("[DEBUG - Card Draw] Target position invalid or not provided, using default position: " + cardPositionToDraw); 
        }
    
    
        System.out.println("[DEBUG - Card Draw] Preparing to draw card: " + card.getCardname() + ", ID: " + card.getId() + ", at position: " + cardPositionToDraw); 
    
        BasicCommands.drawCard(out, card, cardPositionToDraw, 0); 
    
        System.out.println("[DEBUG - Card Draw] BasicCommands.drawCard command SENT for card: " + card.getCardname() + ", ID: " + card.getId() + ", at position: " + cardPositionToDraw); 
    
        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }
    
        gameState.playerHands.add(card); 
        System.out.println("[DEBUG - Card Draw] Player 1 drew card: " + card.getCardname() + ", ID: " + card.getId() + ", Hand Size: " + gameState.playerHands.size());
    }
}
