package structures.basic;

import java.util.Random;
import structures.basic.Position;
import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import commands.BasicCommands;
import play.libs.Json;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.GameState;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * SpellCard class manages spell card effects in the game.
 */
public class SpellCard extends Card {

    private String spellType;
    private int effectValue;

    public String getSpellType() {
        return spellType;
    }

    public int getEffectValue() {
        return this.effectValue;
    }

    public void setEffectValue(int effectValue) {
        this.effectValue = effectValue;
    }

    public void Horn_of_the_Forsaken(ActorRef out, GameState gameState, Tile tile, Card card) {
        Unit unit = gameState.player1Unit.get(0);
        int unitX = unit.getPosition().getTilex();
        int unitY = unit.getPosition().getTiley();
    
    
        int tileX = tile.getTilex();
        int tileY = tile.getTiley();
    
        if (tileX == unitX && tileY == unitY) {
                if (card.getManacost() <= gameState.humanPlayer.getMana()) {
                    gameState.AnimationPlaying = true;
                    EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
                    BasicCommands.playEffectAnimation(out, effect, tile);
                    gameState.AnimationPlaying = false;
                    unit.addRobustness(3);
                    gameState.setmap(unitX, unitY, unit);
                    gameState.changeUnit(out, unit);
                    gameState.humanPlayer.setMana(gameState.humanPlayer.getMana() - card.getManacost());
                    BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
                } else {
                    System.out.println("[ERROR] Not enough mana to play Horn of the Forsaken.");
                }
            } 
         else {
            System.out.println("[ERROR] Selected tile does not match unit's position.");
        }
    }
    public static void Wraithling_Swarm_Spell(ActorRef out, GameState gameState, Tile tile, Card card) {
        int tileX = tile.getTilex();
        int tileY = tile.getTiley();
    
        if (gameState.checkmap(tileX, tileY) != null) {
            System.out.println("[WARN - SpellCard] Tile (" + tileX + "," + tileY + ") is occupied. Cannot summon Wraithling here.");
            return;
        }

        if (!gameState.isWraithlingSwarmActive) { 
            int spellCost = card.getManacost();
            int playerMana = gameState.humanPlayer.getMana();
    
            if (playerMana >= spellCost) {
                int newMana = playerMana - spellCost;
                gameState.humanPlayer.setMana(newMana);
                BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
                System.out.println("[DEBUG - SpellCard] Mana cost deducted (spell completion - based on state). Spell Cost: " + spellCost + ", New Mana: " + newMana); //  ✅  修改 Debug 日志，表明是基于状态判定的法术结束时扣除
            } else {
                System.out.println("[WARN - SpellCard] Insufficient mana at spell completion (based on state)! Spell Card: " + card.getCardname() + ". Cost: " + spellCost + ", Current Mana: " + playerMana + ". Mana NOT deducted (this should not happen)."); //  ✅  警告日志
                BasicCommands.addPlayer1Notification(out, "Insufficient Mana (at completion)!", 2);
             
                gameState.isWraithlingSwarmActive = false; 
                gameState.wraithlingSwarmCount = 0;
                gameState.activeWraithlingSwarmCard = null;
                gameState.isCardClicked = null;
                return;
            }
        } else {
            System.out.println("[DEBUG - SpellCard] Mana cost NOT deducted (spell active - based on state). Waiting for spell completion to deduct mana."); //  ✅  新增 Debug 日志，表明法术激活状态时不扣除法力值
        }
        SpellCard.placeWraithling(out, gameState, tile, gameState.humanPlayer);
      
    
        System.out.println("[DEBUG] Summoned Wraithling " + gameState.wraithlingSwarmCount + "/3");
    if (gameState.wraithlingSwarmCount >= 3) {
        System.out.println("[DEBUG - SpellCard] --- IF BLOCK ENTERED: gameState.wraithlingSwarmCount = " + gameState.wraithlingSwarmCount); //  ⭐  新增 Debug 日志 - IF 代码块入口
        System.out.println("[DEBUG] Wraithling Swarm finished!");
        gameState.isWraithlingSwarmActive = false;
        gameState.wraithlingSwarmCount = 0;
        gameState.activeWraithlingSwarmCard = null;
        System.out.println("[DEBUG - SpellCard] --- IF BLOCK EXIT: gameState.isWraithlingSwarmActive = " + gameState.isWraithlingSwarmActive + ", gameState.wraithlingSwarmCount = " + gameState.wraithlingSwarmCount); 
        } else {
            System.out.println("[DEBUG] Waiting for next tile selection...");
        }
    }

   public static void Dark_Terminus(ActorRef out, GameState gameState, Tile tile, Card card){
    int tilex = tile.getTilex();
    int tiley = tile.getTiley();
    Unit unit = gameState.checkmap(tilex,tiley);
    if(unit!=null){
        if(unit.getOwner()==gameState.aiPlayer){
           
            if(card.getManacost()<=gameState.humanPlayer.getMana()) {
                if (unit != gameState.player2Unit.get(0)) { 
                    gameState.humanPlayer.setMana(gameState.humanPlayer.getMana() - gameState.isCardClicked.getManacost());
                    BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
                    gameState.AnimationPlaying = true;
                    EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_inmolation);
                    BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.death);
                    BasicCommands.playEffectAnimation(out, effect, tile);
                    try {
                        Thread.sleep(BasicCommands.playEffectAnimation(out, effect, tile));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SpellCard.placeWraithling(out, gameState,tile, gameState.humanPlayer);
                    gameState.AnimationPlaying = false;

                }
            }

        }
    }
}

public void Sundrop_Elixir(ActorRef out, GameState gameState, Tile tile, Card card) {
    Unit target = gameState.checkmap(tile.getTilex(), tile.getTiley());

    if (target != null && gameState.player2Unit.contains(target)) { 
        playEffect(out, gameState, tile, StaticConfFiles.f1_buff);
        int healAmount = 4;
        int newHealth = target.getHealth() + healAmount;
        int maxHealth = target.getMaxHealth(); 
        if (newHealth > maxHealth) {
            newHealth = maxHealth;
        }
        target.setHealth(newHealth);
        gameState.changeUnit(out, target);
    }
}

public void True_Strike(ActorRef out, GameState gameState, Tile tile, Card card) {
    Unit target = gameState.checkmap(tile.getTilex(), tile.getTiley());

    if (target != null && gameState.player1Unit.contains(target)) {
        playEffect(out, gameState, tile, StaticConfFiles.f1_inmolation);
        target.setHealth(target.getHealth() - 2);
        if (target.getHealth() <= 0) gameState.removeUnit(target);
        gameState.changeUnit(out, target);
    }
}
    public void Beam_Shock(ActorRef out, GameState gameState, Tile tile, Card card) {
        Unit target = gameState.checkmap(tile.getTilex(), tile.getTiley());

        if (target != null && gameState.player1Unit.contains(target) && target != gameState.player1Unit.get(0)) {
            playEffect(out, gameState, tile, StaticConfFiles.f1_buff);
            target.setStunned(true);
            gameState.changeUnit(out, target);
        }
    }
    private static void placeWraithling(ActorRef out, GameState gameState, Tile tile, Player player) { 
        if (gameState.checkmap(tile.getTilex(), tile.getTiley()) != null || !gameState.checkmaprange(tile.getTilex(), tile.getTiley())) return;

        playEffect(out, gameState, tile, StaticConfFiles.f1_summon);

        Unit unit = BasicObjectBuilders.loadUnit(StaticConfFiles.wraithling, gameState.getNextUnitID(), Unit.class);
        unit.setPositionByTile(tile);
        gameState.setmap(tile.getTilex(), tile.getTiley(), unit);
        BasicCommands.drawUnit(out, unit, tile);
        gameState.player1Unit.add(unit);

        unit.setHealth(1);
        BasicCommands.setUnitHealth(out, unit, 1);
        unit.setAttack(1);
        BasicCommands.setUnitAttack(out, unit, 1);
        System.out.println("[DEBUG - placeWraithling] Unit ID: " + unit.getId() + ", Health set to: " + unit.getHealth() + " (Server-side)");
        System.out.println("[DEBUG - placeWraithling] Unit ID: " + unit.getId() + ", Attack set to: " + unit.getAttack() + " (Server-side)");
    }


    private static void playEffect(ActorRef out, GameState gameState, Tile tile, String effectFile) { 
        gameState.AnimationPlaying = true;
        EffectAnimation effect = BasicObjectBuilders.loadEffect(effectFile);
        BasicCommands.playEffectAnimation(out, effect, tile);
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        gameState.AnimationPlaying = false;
    }
    public static void Random_Wraithling(ActorRef out, GameState gameState, Unit unit){
        Random random = new Random();
        List<Tile> place = new ArrayList<>();
        Player player = unit.getOwner();
        Tile tile;
        int unitx = unit.getPosition().getTilex();
        int unity = unit.getPosition().getTiley();

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if ((gameState.checkmap(unitx + i, unity + j) == null)) {
                    tile = gameState.tileMap[unitx + i][unity + j]; 
                    place.add(tile);
                }
            }
        }
        if(place.isEmpty())return;
        tile = place.get(random.nextInt(place.size()));
        int x = tile.getTilex();
        int y = tile.getTiley();
        SpellCard.placeWraithling(out,gameState,tile,gameState.humanPlayer);
    }


    private static Tile getRandomEmptyTile(GameState gameState) {
        Random rand = new Random();
        List<Tile> emptyTiles = new ArrayList<>();

        for (int i = 0; i < gameState.tileMap.length; i++) {
            for (int j = 0; j < gameState.tileMap[i].length; j++) {
                if (gameState.checkmap(i, j) == null) {
                    emptyTiles.add(gameState.tileMap[i][j]);
                }
            }
        }

        if (!emptyTiles.isEmpty()) {
            return emptyTiles.get(rand.nextInt(emptyTiles.size()));
        }
        return null;
    }
}