package structures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.SpellCard;
import structures.basic.Unit;
import structures.basic.Tile;
import utils.StaticConfFiles;
import utils.BasicObjectBuilders;

/**
 * This class can be used to hold information about the on-going game.
 * It's created with the GameActor.
 */
public class GameState {


    public boolean AnimationPlaying = false; 
    public boolean gameInitalised = false; 
    public boolean gameFinished = false;    
    public boolean win = false; 
    public boolean lost = false;          

    public boolean isWraithlingSwarmActive = false; 
    public int wraithlingSwarmCount = 0; 
    public SpellCard activeWraithlingSwarmCard = null; 

    public Unit isTileClicked = null;
    public Card isCardClicked = null;
    public Card SpellUsing = null;
    public Tile clickedTile = null;
    private List<Tile> highlightedTiles = new ArrayList<>();

    public List<Unit> TempUnitList; 
    public List<Tile> TempTileList;  

    public int Turns; 


    public List<Card> player1Cards;   
    public List<Card> player2Cards;  
    public List<Card> playerHands; 
    public List<Card> aiHands;  

    public Player humanPlayer; 
    public Player aiPlayer; 
    private int unitIDCounter = 1;

    public List<Unit> player1Unit;
    public List<Unit> player2Unit; 

    public Unit map[][] = new Unit[9][5];
    public Tile tileMap[][] = new Tile[9][5];

    public int handPosition;

    public Unit playerAvatar; 
    public Unit aiAvatar; 
    private Tile selectedTile;

    public GameState() {
        Turns = 1;
    }
    
    //lu:Unit id 计数器
    public static int unitCounter = 0;

    public void updateTileOccupant(int tilex, int tiley, Unit newOccupant) {
        if (tileMap[tilex][tiley] == null) {
            tileMap[tilex][tiley] = new Tile("Default", tilex, tiley, 0, 100, 0, 0); 
            System.out.println("[DEBUG] Tile at (" + tilex + ", " + tiley + ") was null and initialized.");
        }
        if (tilex >= 0 && tilex < tileMap.length && tiley >= 0 && tiley < tileMap[tilex].length) {
            Tile tile = tileMap[tilex][tiley];
            tile.setOccupant(newOccupant);
            System.out.println("[DEBUG] Occupant at (" + tilex + ", " + tiley + ") updated to: " + newOccupant);
        } else {
            System.out.println("[ERROR] Invalid tile coordinates: (" + tilex + ", " + tiley + ")");
        }
    }


    public Tile getHighlightedTile(int x, int y) {
        for (Tile tile : highlightedTiles) {
            if (tile.getTilex() == x && tile.getTiley() == y) {
                return tile;
            }
        }
        return null;
    }
    public Tile getHighlightedTile() {
        if (highlightedTiles.isEmpty()) {
            System.out.println("[ERROR] highlightedTiles is EMPTY! No highlighted tiles exist.");
            return null;
        }
        for (Tile tile : highlightedTiles) {
            if (tile != null) return tile;
        }
        return null;
    }
    public List<Tile> getHighlightedTiles() {
        return highlightedTiles;
    }
    public void clearHighlightedTiles() {
        highlightedTiles.clear();
    }

    public Unit getUnitOnTile(Tile tile) {
        if (tile != null) {
            return tile.getUnit(); 
        }
        return null;
    }
    public void setmap(int x, int y, Unit unit) {
        this.map[x][y] = unit;
    }

    public void clearmap(int x, int y) {
        this.map[x][y] = null;
    }

    public Unit checkmap(int x, int y) {
        if (x < 0 || x >= 9 || y < 0 || y >= 5) return null;
        return this.map[x][y];
    }
    public int getNextUnitID() {
        return unitIDCounter++;
    }

    public boolean checkmaprange(int x, int y) {
        return (x >= 0 && x < 9 && y >= 0 && y < 5);
    }

    public void delPlay1Unit(Unit unit) {
        int Id = unit.getId();
        for (int i = 0; i < this.player1Unit.size(); ++i) {
            Unit U = this.player1Unit.get(i);
            if (U.getId() == Id) {
                this.player1Unit.remove(U);
                break;
            }
        }
    }
	public void changeUnit(ActorRef out, Unit unit){
		if(unit == this.player1Unit.get(0)){
			this.humanPlayer.setHealth(unit.getHealth());
			BasicCommands.setPlayer1Health(out,humanPlayer);
		}
		if(unit == this.player2Unit.get(0)){
			this.aiPlayer.setHealth(unit.getHealth());
			BasicCommands.setPlayer2Health(out,aiPlayer);
		}
		if(unit.getOwner()==this.humanPlayer)changePlayer1Unit(unit);
		else changePlayer2Unit(unit);
	}
    public void changePlayer1Unit(Unit unit){
        int Id = unit.getId();
        boolean unitFound = false;
        
        for(int i = 0; i < this.player1Unit.size(); ++i){
            Unit U = this.player1Unit.get(i);
            
            if(U.getId() == Id) {
                this.player1Unit.set(i, unit); 
                unitFound = true;
                break;
            }
        }
        
        if (!unitFound) {
            System.out.println("[ERROR] Unit with ID " + Id + " not found in player1Unit.");
        }
    }
	public void changePlayer2Unit(Unit unit){
		int Id = unit.getId();
		for(int i = 0; i < this.player2Unit.size();++i){
			Unit U=this.player2Unit.get(i);
			if(U.getId() == Id){
				this.player2Unit.set(i,unit);

			}
			break;
		}
	}
    public void removeUnit(Unit unit) {
        int x = unit.getPosition().getTilex();
        int y = unit.getPosition().getTiley();
        setmap(x, y, null); 
        player1Unit.remove(unit);
        player2Unit.remove(unit);
    }

    public void delPlay2Unit(Unit unit){
        int Id = unit.getId();
        for(int i = 0; i < this.player2Unit.size(); ++i){
            Unit U = this.player2Unit.get(i);
            if(U.getId() == Id) {
                this.player2Unit.remove(U);
                break;
            }
        }
    }
    
    //lu
    public List<Unit> getPlayer1Unit(){
    	return player1Unit;
    }
    public List<Unit> getPlayer2Unit(){
    	return player2Unit;
    }
    public Unit getAiUnit() {
        if (aiAvatar == null) {
            System.err.println("[ERROR] AI Avatar is null in getAiUnit().");
        }
        return aiAvatar;
    }
    
    public Unit getPlayerAvatar() {
        if (playerAvatar == null) {
            System.err.println("[ERROR] Player Avatar is null in getPlayerAvatar().");
        }
        return playerAvatar;
    }
    
    public void setAiAvatar(Unit ai) {
        this.aiAvatar = ai;
    }
    
    public void setPlayerAvatar(Unit player) {
        this.playerAvatar = player;
    }
    
}
