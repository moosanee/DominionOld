package com.example.dominion;

import android.graphics.Color;

public final class MyConstants {
    public final static int screenWidth = 1200; //pixels (nexus 7) = 600 dps
    public final static int screenHeight = 1920; //pixels (nexus 7) == 960 dps
    public final static int cardWidth = 200; // width of a card in pixels
    public final static int cardHeight = 308; // (cardWidth*1.542);
    public final static int smallCardWidth = 120; //width of a small card
    public final static int smallCardHeight = 185; // (smallCardWidth*1.542f);
    public final static int minOverlap = 32; // minimum overlap of cards
    public final static int buffer = 16; // minimum generic spacer
    public final static int handBuffer = 32; // extra space to separate the hand from the deck and discard

    public static final int OPPONENT_LISTENERS = 470;
    public static final int TRASH_LISTENER = 480;
    public static final int PLAYER_HAND_VIEW_ID = 232323;
    public static final int PLAY_AREA_VIEWS_ID = 454545;
    public static final int DISCARD_BROWSE_LISTENER = 492;
    public static final int DECK_BROWSE_LISTENER = 496;
    public static final int HAND_BROWSE_LISTENER = 498;
    public static final int INPLAY_BROWSE_LISTENER = 500;
    public static final int INPLAY_VIEW_ID = 502;
    public static final int HAND_AREA_VIEW_ID = 504;
    private static final int CARD_CLOSEUP_ACTIVITY_CODE = 1003;
    private static final int BASIC_CARDS_ACTIVITY_CODE = 6;
    private static final int INTRIGUE_CARDS_ACTIVITY_CODE = 7;
    private static final int GAME_BOARD_ACTIVITY_CODE = 8;
    private static final int TESTER_BUTTON_ID = 474747;
    private static final int CHOOSE_CARDS_ACTIVITY_CODE = 0;
    private static final int START_GAME_ACTIVITY_CODE = 1;
    public final static int BACKGROUND_COLOR_DARK = Color.parseColor("#363c61");
    public final static int BACKGROUND_COLOR = Color.parseColor("#45508b");
    public final static int ACCENT_COLOR = Color.parseColor("#ffe9be");
}
