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
import structures.basic.UnitAnimation;
import structures.basic.UnitAnimationType;
import structures.GameState;
import structures.abilities.Ability;
import structures.abilities.Provoke;
import utils.BasicObjectBuilders;
public class AIAttack{


    public static void AIatk(ActorRef out, GameState gameState, Unit Aunit){
    	
    	//lu:Provoke对攻击的限制
    	System.out.println("[DEBUG - UnitAction - Attack] Attempting attack: Unit " + Aunit.getId() + " -> enemy unit " + Aunit.getId());
        
        // 首先检查攻击者周围是否存在拥有 "Provoke" 的敌方单位
        if (Provoke.isStunned(Aunit, gameState)) {
            // 如果存在，则要求 defender 也必须拥有 Provoke 能力w
            boolean defenderHasProvoke = false;
            if (gameState.playerAvatar.getAbilities() != null) {
                for (Ability ability : gameState.playerAvatar.getAbilities()) {
                    if ("Provoke".equals(ability.getAbilityName())) {
                        defenderHasProvoke = true;
                        break;
                    }
                }
            }
            if (!defenderHasProvoke) {
                System.err.println("[DEBUG - UnitAction - Attack] Attacker " + Aunit.getId() + " is forced to attack a unit with Provoke!");
                BasicCommands.addPlayer1Notification(out, "You must attack an enemy with Provoke!", 2);
                return;
            }
        }
    	//lu
    	
        int aiX = Aunit.getPosition().getTilex();
        int aiY = Aunit.getPosition().getTiley();
        int enemyX = gameState.playerAvatar.getPosition().getTilex();
        int enemyY = gameState.playerAvatar.getPosition().getTiley();

        // 2. 如果敌人在攻击范围内，攻击
        if (Math.abs(aiX - enemyX) + Math.abs(aiY - enemyY) == 1) {
            TileClicked.doAttack(out, gameState, Aunit, gameState.playerAvatar);
            return;
        }
    }
    public static void Aiatk(ActorRef out, GameState gameState){

        List<Unit> ailist = new ArrayList<>(gameState.player2Unit);
        for(Unit unit: ailist){
        	AIatk(out,gameState,unit);
        	try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
        }
    }
    
}