package structures.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

public class Rush extends Ability {

	public Rush() {
		super("Rush");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
		// TODO Auto-generated method stub

	}

}
