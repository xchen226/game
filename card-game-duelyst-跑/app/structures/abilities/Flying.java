package structures.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

public class Flying extends Ability {

	public Flying() {
		super("Flying");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
		// TODO Auto-generated method stub

	}
	
	public static boolean canFly(Unit unit) {
		boolean fly = false;
		
    	// 遍历该邻居单位的所有能力
        if (unit.getAbilities() != null) {
            for (Ability ability : unit.getAbilities()) {
                if ("Flying".equals(ability.getAbilityName())) {
                    fly = true;
                }
            }
        }
        return fly;
	}

}
