package com.example.dominion;

import java.util.ArrayList;

public class Game {
    ArrayList<Player> playerList= new ArrayList<>();
    ArrayList<String> gameCardList = new ArrayList<>();

    Game(ArrayList<Player> playerList, ArrayList<String> gameCardsList){
        this.playerList = playerList;
        this.gameCardList = gameCardsList;
    }

}
