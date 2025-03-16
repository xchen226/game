package structures.basic;

import structures.GameState;
import structures.abilities.Ability;
import structures.basic.Position;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.ActorRef;

/**
 * Represents a unit on the game board (including heroes).
 * Contains position, animation state, health, attack, and move status.
 */
public class Unit {

    @JsonIgnore
    protected static ObjectMapper mapper = new ObjectMapper();

    private int id;
    private UnitAnimationType animation;
    private Position position;
    private UnitAnimationSet animations;
    private ImageCorrection correction;
    private int robustness;
    private Player owner;
    private boolean finishMove = false;
    private boolean finishAttack = false;

    private int health;
    private int attack;
    private int maxHealth;

    private boolean hasMoved = false;
    private boolean stunned = false; 

    private int highlightState;

      //  finishMove 属性的 Getter 方法 ( boolean 类型的 getter 通常以 "is" 开头， 也可以用 "get" )
    public boolean isFinishMove() {     //  👈  Getter 方法名： isFinishMove
        return finishMove;
    }

    //  finishMove 属性的 Setter 方法
    public void setFinishMove(boolean finishMove) {   //  👈 Setter 方法名： setFinishMove
        this.finishMove = finishMove;
    }

    //  finishAttack 属性的 Getter 方法 ( boolean 类型的 getter 通常以 "is" 开头， 也可以用 "get" )
    public boolean isFinishAttack() {    //  👈 Getter 方法名： isFinishAttack
        return finishAttack;
    }

    //  finishAttack 属性的 Setter 方法
    public void setFinishAttack(boolean finishAttack) {  //  👈 Setter 方法名： setFinishAttack
        this.finishAttack = finishAttack;
    }

    public int getHighlightState() {
        return highlightState;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }
    
    public boolean isStunned() {
        return this.stunned;
    }

    public Unit() {
    }

    public void addRobustness(int value) {
        this.robustness += value;
    }

    public int getRobustness() {
        return robustness;
    }

    public void setRobustness(int robustness) {
        this.robustness = robustness;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
    public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
        this.id = id;
        this.animation = UnitAnimationType.idle;
        this.position = new Position(0, 0, 0, 0);
        this.animations = animations;
        this.correction = correction;
        this.finishMove = false;
        this.finishAttack = false;
    }

    public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
        this.id = id;
        this.animation = UnitAnimationType.idle;
        this.position = new Position(currentTile.getXpos(), currentTile.getYpos(),
                                     currentTile.getTilex(), currentTile.getTiley());
        this.animations = animations;
        this.correction = correction;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public UnitAnimationType getAnimation() {
        return animation;
    }
    public void setAnimation(UnitAnimationType animation) {
        this.animation = animation;
    }
    public Position getPosition() {
        return position;
    }
    public void setPosition(Position position) {
        this.position = position;
    }
    public UnitAnimationSet getAnimations() {
        return animations;
    }
    public void setAnimations(UnitAnimationSet animations) {
        this.animations = animations;
    }
    public ImageCorrection getCorrection() {
        return correction;
    }
    public void setCorrection(ImageCorrection correction) {
        this.correction = correction;
    }


    @JsonIgnore
    public void setPositionByTile(Tile tile) {
        this.position = new Position(tile.getXpos(), tile.getYpos(),
                                     tile.getTilex(), tile.getTiley());
    }

    public int getHealth() {
        return this.health;
    }
    public void setHealth(int health) {
        this.health = health;
        if (this.health < 0) {
            this.health = 0;
        }
    }
    public void reduceHealth(int amount) {
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
    }
    public int getAttack() {
        return this.attack;
    }
    public void setAttack(int attack) {
        this.attack = attack;
    }

    public boolean hasMoved() {
        return hasMoved;
    }
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public String toString() {
        return "Attack: " + this.attack + ", Health: " + this.health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    
    
    //lu:是不是卡牌召唤单位
    private boolean isCreature = false;
    
    public void setIsCreature(boolean isCreature) {
    	this.isCreature = isCreature;
    }
    public boolean isCreature() {
    	return this.isCreature;
    }
    
    private List<Ability> abilities;
    
    // 能力的 getter 方法
    public List<Ability> getAbilities() {
        return abilities;
    }

    // 能力的 setter 方法
    public void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
    }
    
    public void handleEvent(ActorRef out, GameState gameState, Object event) {
    	
    	System.out.println("[DEBUG - CreatureUnit.handleEvent] Ability trigger event now starts to be handled.");
    	
        if (abilities != null) {
            for (Ability ability : abilities) {
                ability.onTrigger(this, out, gameState, event);
                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }
    
    
}



