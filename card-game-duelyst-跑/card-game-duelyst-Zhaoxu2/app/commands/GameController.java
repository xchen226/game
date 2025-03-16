package commands;

import commands.BasicCommands;
import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import utils.BasicObjectBuilders;







public class GameController {
    public static void Initiallization(ActorRef out, GameState gameState){
        Tile tile;
        for (int i = 0; i < 9; ++i)
            for (int j = 0; j < 5; ++j) {
                tile = BasicObjectBuilders.loadTile(i, j);
                BasicCommands.drawTile(out, tile, 0);
            }
        }
    }
 