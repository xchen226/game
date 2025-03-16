package structures.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

public class Provoke extends Ability {

	public Provoke() {
		super("Provoke");
		
	}

	@Override
	public void onTrigger(Unit owner, ActorRef out, GameState gameState, Object event) {
		// TODO Auto-generated method stub
	}
	
    /**
     * lu
     * 判断单位是否受到 Provoke 能力的限制
     */
    public static boolean isStunned(Unit unit, GameState gameState) {
        int x = unit.getPosition().getTilex();
        int y = unit.getPosition().getTiley();
        
        
        // 遍历周围 8 个方向
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // 跳过本身
                int nx = x + dx;
                int ny = y + dy;
                // 检查坐标是否在棋盘范围内
                if (!gameState.checkmaprange(nx, ny)) {
                    continue;
                }
                // 获取该邻近格子上的 unit
                Unit neighbor = gameState.checkmap(nx, ny);
                if (neighbor != null && isEnemy(unit, neighbor, gameState)) {
                    // 遍历该邻居单位的所有能力
                    if (neighbor.getAbilities() != null) {
                        for (Ability ability : neighbor.getAbilities()) {
                            if ("Provoke".equals(ability.getAbilityName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    private static boolean isEnemy(Unit unit, Unit other, GameState gameState) {
        if (gameState.player1Unit.contains(unit)) {
            return gameState.player2Unit.contains(other);
        } else if (gameState.player2Unit.contains(unit)) {
            return gameState.player1Unit.contains(other);
        }
        return false;
    }

}
