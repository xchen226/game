package structures.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

/**
 * Ability 抽象类定义了所有能力的基本接口
 */
public abstract class Ability {
    private String abilityName;

    public Ability(String name) {
        this.abilityName = name;
    }

    public String getAbilityName() {
        return abilityName;
    }

    /**
     * 当事件触发时调用该方法
     * @param owner 拥有该能力的单位
     * @param event 事件对象（通常为 JSON 或自定义事件对象）
     */
    public abstract void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event);

}
