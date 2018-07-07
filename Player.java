package com.example.dominion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Player implements Serializable{

    String Name;
    int number;
    boolean human;
    ArrayList<Card> deck;
    ArrayList<Card> hand;
    ArrayList<Card> discard;
    ArrayList<Card> inPlay;

    Player(BasicCards cardSet, PlayerInfo playerInfo) {
        this.Name = playerInfo.getName();
        this.number = playerInfo.getNumber();
        this.human = playerInfo.getHuman();
        this.deck = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            deck.add(cardSet.estate); // add 3 estates to the deck
        }
        for (int i = 0; i < 7; i++) {
            deck.add(cardSet.copper); // add 7 coppers to the deck
        }
        this.hand = new ArrayList<>();
        this.discard = new ArrayList<>();
        this.inPlay = new ArrayList<>();
    }

    public void shuffleDeck() {
        Card temp;
        int length = deck.size(); //number of cards in the deck
        Random rand = new Random();
        for (int j = 0; j < length; j++) {
            int n = rand.nextInt(length - 2); //a random index of the deck, not including the last card
            temp = deck.get(n);
            deck.remove(n);
            deck.add(temp);
        }
    }

    public void drawHand() {
        int deckSize = deck.size();
        int discardSize = discard.size();
        int cardsStillNeeded;
        int newDeckSize;
        if (deckSize > 5) {  //more than 5 cards in deck
            for (int i = deckSize - 1; i > deckSize - 6; i--) { // add last 5 cards in deck to hand
                hand.add(deck.get(i));
                deck.remove(i);
            }
        } else if (deckSize > 0) { //less than 5 cards in deck, some in discard
            cardsStillNeeded = 5-deckSize;
            for (int i = deckSize - 1; i >= 0; i--) {
                hand.add(deck.get(i));
                deck.remove(i);
            }
            putDiscardInDeck();
            if (discardSize > cardsStillNeeded){
                for (int i = discardSize - 1; i > discardSize - cardsStillNeeded-1; i--) { // add last needed cards in deck to hand
                    hand.add(deck.get(i));
                    deck.remove(i);
                }
            } else{
                for (int i = discardSize - 1; i >= 0; i--) { // add remaining cards in shuffled deck to hand
                    hand.add(deck.get(i));
                    deck.remove(i);
                }
            }
        } else{                 //no cards in deck, some in discard
            putDiscardInDeck();
            newDeckSize = deck.size();
            if (newDeckSize>5){ // more than 5 cards in shuffled deck
                for (int i = newDeckSize - 1; i > deckSize - 6; i--) { // add last 5 cards in deck to hand
                    hand.add(deck.get(i));
                    deck.remove(i);
                }
            } else{ // less than 5 cards in shuffled deck
                for (int i = newDeckSize - 1; i >=0; i--) { // add remaining cards in deck to hand
                    hand.add(deck.get(i));
                    deck.remove(i);
                }
            }
        }
    }

    void putDiscardInDeck() {
        int discardSize = discard.size();
        for (int i = 0; i<discardSize; i++){
            deck.add(discard.get(i));
        }
        discard.clear();
        shuffleDeck();
    }

    void equals(Player player){
        if(player.deck.size() >= this.deck.size()) {
            for (int i = 0; i < this.deck.size(); i++) this.deck.set(i, player.deck.get(i));
            for (int i = this.deck.size(); i < player.deck.size(); i++) this.deck.add(player.deck.get(i));
        }
        else {
            for (int i = 0; i < player.deck.size(); i++) this.deck.set(i, player.deck.get(i));
            for (int i = this.deck.size()-1; i >= player.deck.size(); i--) this.deck.remove(i);
        }
        if(player.hand.size() >= this.hand.size()) {
            for (int i = 0; i < this.hand.size(); i++) this.hand.set(i, player.hand.get(i));
            for (int i = this.hand.size(); i < player.hand.size(); i++) this.hand.add(player.hand.get(i));
        }
        else {
            for (int i = 0; i < player.hand.size(); i++) this.hand.set(i, player.hand.get(i));
            for (int i = this.hand.size()-1; i >= player.hand.size(); i--) this.hand.remove(i);
        }
        if(player.discard.size() >= this.discard.size()) {
            for (int i = 0; i < this.discard.size(); i++) this.discard.set(i, player.discard.get(i));
            for (int i = this.discard.size(); i < player.discard.size(); i++) this.discard.add(player.discard.get(i));
        }
        else {
            for (int i = 0; i < player.discard.size(); i++) this.discard.set(i, player.discard.get(i));
            for (int i = this.discard.size()-1; i >= player.discard.size(); i--) this.discard.remove(i);
        }
        if(player.inPlay.size() >= this.inPlay.size()) {
            for (int i = 0; i < this.inPlay.size(); i++) this.inPlay.set(i, player.inPlay.get(i));
            for (int i = this.inPlay.size(); i < player.inPlay.size(); i++) this.inPlay.add(player.inPlay.get(i));
        }
        else {
            for (int i = 0; i < player.inPlay.size(); i++) this.inPlay.set(i, player.inPlay.get(i));
            for (int i = this.inPlay.size()-1; i >= player.inPlay.size(); i--) this.inPlay.remove(i);
        }
    }

    public ArrayList<CardMultiTag> getDeckPileList() {
        ArrayList<CardMultiTag> deckPileList = new ArrayList<>();
        for (int i = 0; i < this.deck.size(); i++){
            String cardName = this.deck.get(i).getName();
            deckPileList.add(new CardMultiTag(i,i,cardName, "deck"));
        }
        return deckPileList;
    }
}