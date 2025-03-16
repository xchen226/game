package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.BigCard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.UnitSummonEvent;
import structures.GameState;
import structures.abilities.Ability;
import structures.abilities.DeathwatchAttackAndHealthBoost;
import structures.abilities.DeathwatchAttackBoost;
import structures.abilities.DeathwatchDamageAndHeal;
import structures.abilities.DeathwatchSummonWraithling;
import structures.abilities.Flying;
import structures.abilities.OpeningGambit;
import structures.abilities.OpeningGambitDestroy;
import structures.abilities.OpeningGambittAlliesBoost;
import structures.abilities.Provoke;
import structures.abilities.Rush;
import structures.abilities.Zeal;
import utils.BasicObjectBuilders;


public class SummonUnitOnBoard {
	
    public static Unit summonUnit(ActorRef out, GameState gameState, Tile clickedTile, int tilex, int tiley, Card card) {
        //  ✅  Step 1: 修改 unit 变量类型为 Unit - 确保这里是 Unit unit = null;
        Unit unit = null;
        
        unit = BasicObjectBuilders.loadUnit(card.getUnitConfig(), gameState.getNextUnitID(), Unit.class);

        // 将 Unit 放置到棋盘上，并调用 BasicCommands.drawUnit 绘制
        unit.setPositionByTile(clickedTile);
        BasicCommands.drawUnit(out, unit, clickedTile);
        // 更新游戏状态：将 Unit 添加到玩家单位列表，并更新棋盘 map 数据
        /*
        if ((card.getCardname().equals("Bad Omen"))||(card.getCardname().equals("Gloom Chaser"))||(card.getCardname().equals("Rock Pulveriser"))||(card.getCardname().equals("Shadow Watcher"))||(card.getCardname().equals("Nightsorrow Assassin"))||(card.getCardname().equals("Bloodmoon Priestess"))||(card.getCardname().equals("Shadowdancer"))) {
        	gameState.player1Unit.add(unit);
        }else {
        	gameState.player2Unit.add(unit);
        }
        */
        
        
        // gameState.setmap(tilex, tiley, unit);


        unit.setAttack(card.getBigCard().getAttack());
        BasicCommands.setUnitAttack(out, unit, card.getBigCard().getAttack());

        unit.setHealth(card.getBigCard().getHealth());
        BasicCommands.setUnitHealth(out, unit, card.getBigCard().getHealth());

        unit.setMaxHealth(card.getBigCard().getHealth());
        BasicCommands.setMaxHealth(out, unit, card.getBigCard().getHealth());

        // 7. 创建单位召唤事件的 JSON 消息，并调用 UnitSummonEvent 处理器广播该事件
        ObjectNode summonMsg = Json.newObject();
        summonMsg.put("messageType", "unitSummon");
        summonMsg.put("id", unit.getId());
        summonMsg.put("tilex", tilex);
        summonMsg.put("tiley", tiley);
        UnitSummonEvent summonEvent = new UnitSummonEvent();
        summonEvent.processEvent(out, gameState, summonMsg);
        // 4. 为 Unit 赋予对应的 Abilities
        List<Ability> abilities = null;
        if (card.getCardname().equals("Bad Omen")) {
            abilities = Arrays.asList(new DeathwatchAttackBoost());
        } else if (card.getCardname().equals("Gloom Chaser")) {
            abilities = Arrays.asList(new OpeningGambit());
        } else if ((card.getCardname().equals("Rock Pulveriser"))||(card.getCardname().equals("Swamp Entangler"))||(card.getCardname().equals("Ironcliffe Guardian"))) {        	
            abilities = Arrays.asList(new Provoke());    
        } else if (card.getCardname().equals("Shadow Watcher")) {
            abilities = Arrays.asList(new DeathwatchAttackAndHealthBoost());
        } else if (card.getCardname().equals("Nightsorrow Assassin")) {
            abilities = Arrays.asList(new OpeningGambitDestroy());
        } else if (card.getCardname().equals("Bloodmoon Priestess")) {
            abilities = Arrays.asList(new DeathwatchSummonWraithling());
        } else if (card.getCardname().equals("Shadowdancer")) {
            abilities = Arrays.asList(new DeathwatchDamageAndHeal());        
        } else if (card.getCardname().equals("Silverguard Squire")) {
            abilities = Arrays.asList(new OpeningGambittAlliesBoost());
        } else if (card.getCardname().equals("Saberspine Tiger")) {
            abilities = Arrays.asList(new Rush());
        } else if (card.getCardname().equals("Silverguard Knight")) {
            abilities = Arrays.asList(new Provoke(),new Zeal());
        } else if (card.getCardname().equals("SYoung Flamewing")) {
            abilities = Arrays.asList(new Flying());
        }
        if (abilities != null) {
            //  ✅  Step 3: 注释掉 unit.setAbilities(abilities); 代码 - 确保这行被注释或删除
            // unit.setAbilities(abilities);
        }
        unit.setIsCreature(true);
        System.out.println("[DEBUG - UnitType] Unit is creature:" + unit.isCreature());
        
        unit.setAbilities(abilities);
        
        //lu:unit id 计数
        //unit.setId(++ GameState.unitCounter);
        System.out.println("[DEBUG - UnitID] Unit Id:" + unit.getId());
        //lu
        

        //  ✅  Step 4: 修改方法返回类型为 Unit - 确保方法签名是 public static Unit summonUnit(...)
        return unit; //  👈  Step 2: 移除 (CreatureUnit) 强制类型转换 - 确保这里没有 (CreatureUnit)
    }
    
    


}
	


