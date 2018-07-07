package com.example.dominion;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class GameBoardActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "Gestures";

    final static int screenWidth = 1200; //pixels (nexus 7) = 600 dps
    final static int screenHeight = 1920; //pixels (nexus 7) == 960 dps
    final static int cardWidth = 200; // width of a card in pixels
    final static int cardHeight = 308; // (cardWidth*1.542);
    final static int smallCardWidth = 120; //width of a small card
    final static int smallCardHeight = 185; // (smallCardWidth*1.542f);
    final static int minOverlap = 32; // minimum overlap of cards
    final static int buffer = 16; // minimum generic spacer
    final static int handBuffer = 32; // extra space to separate the hand from the deck and discard

    private static final int OPPONENT_LISTENERS = 470;
    private static final int TRASH_LISTENER = 480;
    private static final int PLAYER_HAND_VIEW_ID = 232323;
    private static final int PLAY_AREA_VIEWS_ID = 454545;
    private static final int DISCARD_BROWSE_LISTENER = 492;
    private static final int DECK_BROWSE_LISTENER = 496;
    private static final int HAND_BROWSE_LISTENER = 498;
    private static final int INPLAY_BROWSE_LISTENER = 500;
    private static final int INPLAY_VIEW_ID = 502;
    private static final int HAND_AREA_VIEW_ID = 504;
    private final static int BACKGROUND_COLOR_DARK = Color.parseColor("#363c61");
    private final static int BACKGROUND_COLOR = Color.parseColor("#45508b");
    final static int ACCENT_COLOR = Color.parseColor("#ffe9be");

    ArrayList<CardMultiTag> trashPileList = new ArrayList<>();
    int trashPileListCatalogue = 0;
    ArrayList<CardMultiTag> discardPileList = new ArrayList<>();
    int discardPileListCatalogue = 0;
    ArrayList<CardMultiTag> deckPileList = new ArrayList<>();
    int deckPileListCatalogue = 0;
    ArrayList<ImageView> bankImageViews = new ArrayList<>();
    ArrayList<TextView> bankPileCounterViews = new ArrayList<>();
    ArrayList<ImageView> handImageViews = new ArrayList<>();
    ArrayList<CardMultiTag> handPileList = new ArrayList<>();
    ArrayList<ImageView> playAreaViews = new ArrayList<>();
    ArrayList<CardMultiTag> playAreaList = new ArrayList<>();
    int handImageViewCatalogue = 0;
    int playAreaViewCatalogue = 0;
    ArrayList<ImageView> opponentImageViews = new ArrayList<>();
    ArrayList<Card> gameCards = new ArrayList<>();
    ArrayList<String> gameCardList = new ArrayList<>();
    ArrayList<PlayerInfo> playerInfoList = new ArrayList<>();
    ArrayList<Player> playerList = new ArrayList<>();
    boolean doubleTap = false;
    boolean handCardRemoved = false;
    boolean inPlayCardRemoved = false;
    //int deckCountId;

    private GestureDetectorCompat detector;
    //Player playerA;
    ConstraintSet constraintSet;
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        detector = new GestureDetectorCompat(this, new GestureListener());
        this.gameCardList = getIntent().getStringArrayListExtra("gameCardListKey");
        this.playerInfoList = (ArrayList<PlayerInfo>) getIntent().getSerializableExtra("playerListKey");
        layout = findViewById(R.id.activity_game_board);
        constraintSet = new ConstraintSet();

        for (int i = 0; i < playerInfoList.size(); i++){
            playerList.add( new Player(ChooseGameCardsActivity.basicCardSet, playerInfoList.get(i)));
            playerList.get(i).shuffleDeck();
            playerList.get(i).drawHand();
        }
        //Game game = new Game(playerList, gameCardList);
        deckPileList = playerList.get(0).getDeckPileList();
        deckPileListCatalogue = 5;
        int deckViewId = getResources().getIdentifier
                ("player_deck", "id", getPackageName());
        ImageView deckView = (ImageView) layout.getViewById(deckViewId);
        CardMultiTag cmt = new CardMultiTag
                (0, 0, "playerDeck", "deck");
        deckView.setTag(cmt);

        int discardViewId = getResources().getIdentifier
                ("player_discard", "id", getPackageName());
        ImageView discardView = (ImageView) layout.getViewById(discardViewId);
        cmt = new CardMultiTag
                (0, 0, "playerDiscard", "discard");
        discardView.setTag(cmt);

        pullOutCards(gameCardList);
        reorderByCost(gameCards);
        boolean curse = checkCurse(gameCardList);
        if (curse) {
            int viewId = getResources().getIdentifier("curse", "id", getPackageName());
            ImageView curseView = (ImageView) layout.getViewById(viewId);
            CardMultiTag curseTag = new CardMultiTag
                    (0, 0, "curse", "bank");
            curseView.setTag(curseTag);
            String pileName = String.valueOf(bankImageViews.size());
            bankImageViews.add(curseView);
            TextView counterView = makePileSizeCounter(192, 96, pileName, "30");
            layout.addView(counterView);
            int drawableId = getResources().getIdentifier("curse60",
                    "drawable", getPackageName());
            curseView.setImageResource(drawableId);
        }
        layoutHandAndInPlayAreas();
        layoutActionCards(layout, gameCards);
        displayHand(playerList.get(0), constraintSet, layout);
        completeImageViewList(layout);
        setImageListeners();
        //Game game = new Game(numberOfPlayers,gameCardList);
    }

    public boolean onTouch(MotionEvent motionEvent) {
        if (this.detector.onTouchEvent(motionEvent)) {
            return true;
        }
        return super.onTouchEvent(motionEvent);
    }


    private void setImageListeners() {
//bank card views
        for (int i = 0; i < bankImageViews.size(); i++) {
            ImageView view = bankImageViews.get(i);
            view.setOnTouchListener(bankListener);
        }
//hand card views
        for (int i = 0; i < handImageViews.size(); i++) {
            ImageView view = handImageViews.get(i);
            view.setOnTouchListener(handListener);
            view.setOnDragListener(new MyDragListener());
        }
//deck view
        int viewId = getResources().getIdentifier("player_deck", "id", getPackageName());
        View view = layout.getViewById(viewId);
        view.setOnTouchListener(deckListener);
        view.setOnDragListener(new MyDragListener());
//discard view
        viewId = getResources().getIdentifier("player_discard", "id", getPackageName());
        view = layout.getViewById(viewId);
        view.setOnTouchListener(discardListener);
        view.setOnDragListener(new MyDragListener());
//trash view
        viewId = getResources().getIdentifier("trash", "id", getPackageName());
        view = findViewById(viewId);
        CardMultiTag cmt = new CardMultiTag
                (0, 0, "trash", "trash");
        view.setTag(cmt);
        view.setOnTouchListener(trashListener);
        view.setOnDragListener(new MyDragListener());
//opponents views
        for (int i = 0; i < opponentImageViews.size(); i++) {
            final int FINALI = i;
            view = opponentImageViews.get(i);
            view.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View view) {
                    String opponent = String.valueOf(opponentImageViews.get(FINALI).getTag());
                    Intent intent = new Intent(view.getContext(), OpponentAreaDialogActivity.class);
                    intent.putExtra("opponentKey", opponent);
                    intent.putExtra("viewIdKey", view.getId());
                    startActivityForResult(intent, OPPONENT_LISTENERS + FINALI);
                }
            });
        }
//in play area view
        ImageView handView = findViewById(HAND_AREA_VIEW_ID);
        handView.setOnDragListener(new MyDragListener());

//in play area view
        ImageView inPlayView = findViewById(INPLAY_VIEW_ID);
        inPlayView.setOnDragListener(new MyDragListener());

    }//setImageListeners

    private void addHandCardListener(View view) {
        view.setOnTouchListener(handListener);
        view.setOnDragListener(new MyDragListener());
    }//addHandCardListener

    private void addInPlayCardListener(View view) {
        view.setOnTouchListener(inPlayListener);
        view.setOnDragListener(new MyDragListener());
    }//addInPlayCardListener




    //bank onTouch listener
    View.OnTouchListener bankListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean detected = detector.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (!doubleTap) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view);
                    view.startDrag(data, shadowBuilder, view, 0);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (doubleTap) {
                    doubleTap = false;
                    CardMultiTag cmt = (CardMultiTag) view.getTag();
                    String cardName = cmt.getCardName();
                    int viewId = view.getId();
                    String[] actions = {"","","exit",""};
                    boolean[] buttonVisibility = {false, false, true, false};
                    Intent intent = new Intent(view.getContext(), CardCloseUpActivity.class);
                    intent.putExtra("cardNameKey", cardName);
                    intent.putExtra("viewIdKey", viewId);
                    intent.putExtra("actionKey", actions);
                    intent.putExtra("buttonVisibilityKey", buttonVisibility);
                    startActivity(intent);
                }
            }
            return detected;
        }
    }; // bank onTouch listener

    //hand onTouch listener
    View.OnTouchListener handListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean detected = detector.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (!doubleTap) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.setVisibility(View.INVISIBLE);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (doubleTap) {
                    doubleTap = false;
                    Intent intent = new Intent(view.getContext(), HandBrowseActivity.class);
                    intent.putExtra("handPileListKey", handPileList);
                    startActivityForResult(intent, HAND_BROWSE_LISTENER);
                }
            }
            return detected;
        }
    }; // hand onTouch listener

    //in play onTouch listener
    View.OnTouchListener inPlayListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean detected = detector.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (!doubleTap) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.setVisibility(View.INVISIBLE);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (doubleTap) {
                    doubleTap = false;
                    Intent intent = new Intent(view.getContext(), HandBrowseActivity.class);
                    intent.putExtra("handPileListKey", playAreaList);
                    startActivityForResult(intent, INPLAY_BROWSE_LISTENER);
                }
            }
            return detected;
        }
    }; // in play onTouch listener

    //deck onTouch listener
    View.OnTouchListener deckListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean detected = detector.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (!doubleTap) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view);
                    view.startDrag(data, shadowBuilder, view, 0);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (doubleTap) {
                    doubleTap = false;
                    Intent intent = new Intent(view.getContext(), DeckBrowseActivity.class);
                    intent.putExtra("deckPileListKey", deckPileList);
                    startActivityForResult(intent, DECK_BROWSE_LISTENER);
                }
            }
            return detected;
        }
    }; // deck onTouch listener

    //discard onTouch listener
    View.OnTouchListener discardListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean detected = detector.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (!doubleTap) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view);
                    view.startDrag(data, shadowBuilder, view, 0);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (doubleTap) {
                    doubleTap = false;
                    Intent intent = new Intent(view.getContext(), DiscardBrowseActivity.class);
                    intent.putExtra("discardPileListKey", discardPileList);
                    startActivityForResult(intent, DISCARD_BROWSE_LISTENER);
                }
            }
            return detected;
        }
    }; // discard onTouch listener

    //trash onTouch listener
    View.OnTouchListener trashListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean detected = detector.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (!doubleTap) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view);
                    view.startDrag(data, shadowBuilder, view, 0);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (doubleTap) {
                    doubleTap = false;
                    Intent intent = new Intent(view.getContext(), TrashPileActivity.class);
                    intent.putExtra("trashPileListKey", trashPileList);
                    startActivityForResult(intent, TRASH_LISTENER);
                }
            }
            return detected;
        }
    }; // trash onTouch listener



    class MyDragListener implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int targetId = 0;
            final ImageView movingView = (ImageView) event.getLocalState();
            int viewId = movingView.getId();
            CardMultiTag cmt = (CardMultiTag) movingView.getTag();
            String movingViewName = cmt.getCardName();
            String movingViewType = cmt.getCardType();
            Card card = ChooseGameCardsActivity.basicCardSet.getCard(movingViewName);
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    cmt = (CardMultiTag) v.getTag();
                    String targetType = cmt.getCardType();
                    v.setAlpha(0.5f);
                    if (targetType.equals("hand")) {
                        for (int i = 0; i < handImageViews.size(); i++) {
                            handImageViews.get(i).setAlpha(0.5f);
                        }
                    }
                    if (targetType.equals("inPlay")) v.setBackgroundColor(BACKGROUND_COLOR);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    cmt = (CardMultiTag) v.getTag();
                    targetType = cmt.getCardType();
                    v.setAlpha(1f);
                    if (targetType.equals("hand")) {
                        for (int i = 0; i < handImageViews.size(); i++) {
                            handImageViews.get(i).setAlpha(1f);
                        }
                    }
                    if (targetType.equals("inPlay")) v.setBackgroundColor(BACKGROUND_COLOR_DARK);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    targetId = v.getId();
                    cmt = (CardMultiTag) v.getTag();
                    targetType = cmt.getCardType();
                    if (movingViewType.equals("bank")) {
                        int trashId = getResources().getIdentifier
                                ("trash", "id", getPackageName());
                        int discardId = getResources().getIdentifier
                                ("player_discard", "id", getPackageName());
                        int deckId = getResources().getIdentifier
                                ("player_deck", "id", getPackageName());
                        if (targetId == trashId) {
                            removeCardFromPile(movingViewName);
                            addCardToTrash(movingViewName);
                        } else if (targetId == discardId) {
                            removeCardFromPile(movingViewName);
                            addCardToDiscard(playerList.get(0), movingViewName);
                        } else if (targetId == deckId) {
                            removeCardFromPile(movingViewName);
                            addCardToDeck(playerList.get(0), movingViewName);
                        } else if (targetType.equals("hand")) {
                            removeCardFromPile(movingViewName);
                            addCardToHand(playerList.get(0), movingViewName);
                        } else if (targetType.equals("inPlay")) {
                            removeCardFromPile(movingViewName);
                            addCardToPlayArea(movingViewName);
                        } else { // do nothing
                        }
                    }
                    if (movingViewType.equals("hand")) {
                        int trashId = getResources().getIdentifier
                                ("trash", "id", getPackageName());
                        int discardId = getResources().getIdentifier
                                ("player_discard", "id", getPackageName());
                        int deckId = getResources().getIdentifier
                                ("player_deck", "id", getPackageName());
                        if (targetId == trashId) {
                            removeCardFromHand(playerList.get(0), viewId);
                            addCardToTrash(movingViewName);
                        } else if (targetId == discardId) {
                            removeCardFromHand(playerList.get(0), viewId);
                            addCardToDiscard(playerList.get(0), movingViewName);
                        } else if (targetId == deckId) {
                            removeCardFromHand(playerList.get(0), viewId);
                            addCardToDeck(playerList.get(0), movingViewName);
                        } else if (targetType.equals("inPlay")) {
                            removeCardFromHand(playerList.get(0), viewId);
                            addCardToPlayArea(movingViewName);
                        } else if (targetType.equals("hand")) {
                            movingView.setAlpha(1f);
                            return false;
                        } else { // do nothing
                        }
                    }
                    if (movingViewType.equals("inPlay")) {
                        int trashId = getResources().getIdentifier
                                ("trash", "id", getPackageName());
                        int discardId = getResources().getIdentifier
                                ("player_discard", "id", getPackageName());
                        int deckId = getResources().getIdentifier
                                ("player_deck", "id", getPackageName());
                        if (targetId == trashId) {
                            removeCardFromInPlay(viewId);
                            addCardToTrash(movingViewName);
                        } else if (targetId == discardId) {
                            removeCardFromInPlay(viewId);
                            addCardToDiscard(playerList.get(0), movingViewName);
                        } else if (targetId == deckId) {
                            removeCardFromInPlay(viewId);
                            addCardToDeck(playerList.get(0), movingViewName);
                        } else if (targetType.equals("hand")) {
                            removeCardFromInPlay(viewId);
                            addCardToHand(playerList.get(0), movingViewName);
                        } else if (targetType.equals("inPlay")) {
                            movingView.setAlpha(1f);
                            return false;
                        } else { // do nothing
                        }
                    }
                    if (movingViewType.equals("deck")) {
                        int top = deckPileList.size() - 1;
                        if (top == -1) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "shuffling discard", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, buffer, cardHeight / 2 + 2 * buffer);
                            toast.show();
                            putDiscardInDeck(playerList.get(0));
                            top = deckPileList.size() - 1;
                            if (top <= 0) {
                                toast = Toast.makeText(getApplicationContext(),
                                        "deck and\n discard empty", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, buffer, cardHeight / 2 + 2 * buffer);
                                toast.show();
                                return false;
                            }
                        }
                        movingViewName = deckPileList.get(top).getCardName();
                        int trashId = getResources().getIdentifier
                                ("trash", "id", getPackageName());
                        int discardId = getResources().getIdentifier
                                ("player_discard", "id", getPackageName());
                        int deckId = getResources().getIdentifier
                                ("player_deck", "id", getPackageName());
                        if (targetId == trashId) {
                            removeCardFromDeck(playerList.get(0), top);
                            addCardToTrash(movingViewName);
                        } else if (targetId == discardId) {
                            removeCardFromDeck(playerList.get(0), top);
                            addCardToDiscard(playerList.get(0), movingViewName);
                        } else if (targetType.equals("hand")) {
                            removeCardFromDeck(playerList.get(0), top);
                            addCardToHand(playerList.get(0), movingViewName);
                        } else if (targetType.equals("inPlay")) {
                            removeCardFromDeck(playerList.get(0), top);
                            addCardToPlayArea(movingViewName);
                        } else if (targetId == deckId) {
                            movingView.setAlpha(1f);
                            return false;
                        } else { // do nothing
                        }
                    }
                    if (movingViewType.equals("discard")) {
                        int top = discardPileList.size() - 1;
                        if (top == -1) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "discard is empty", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, buffer, cardHeight / 2 + 2 * buffer);
                            toast.show();
                            return true;
                        } else {
                            movingViewName = discardPileList.get(top).getCardName();
                            int trashId = getResources().getIdentifier
                                    ("trash", "id", getPackageName());
                            int discardId = getResources().getIdentifier
                                    ("player_discard", "id", getPackageName());
                            int deckId = getResources().getIdentifier
                                    ("player_deck", "id", getPackageName());
                            if (targetId == trashId) {
                                removeCardFromDiscard(playerList.get(0), top);
                                addCardToTrash(movingViewName);
                            } else if (targetId == deckId) {
                                removeCardFromDiscard(playerList.get(0), top);
                                addCardToDeck(playerList.get(0), movingViewName);
                            } else if (targetType.equals("hand")) {
                                removeCardFromDiscard(playerList.get(0), top);
                                addCardToHand(playerList.get(0), movingViewName);
                            } else if (targetType.equals("inPlay")) {
                                removeCardFromDiscard(playerList.get(0), top);
                                addCardToPlayArea(movingViewName);
                            } else if (targetId == discardId) {
                                movingView.setAlpha(1f);
                                return false;
                            } else { // do nothing
                            }
                        }
                    }
                    if (movingViewType.equals("trash")) {
                        int top = trashPileList.size() - 1;
                        if (top == -1) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "trash is empty", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP | Gravity.LEFT, buffer, cardHeight / 2 + 2 * buffer);
                            toast.show();
                            return true;
                        } else {
                            movingViewName = trashPileList.get(top).getCardName();
                            int trashId = getResources().getIdentifier
                                    ("trash", "id", getPackageName());
                            int discardId = getResources().getIdentifier
                                    ("player_discard", "id", getPackageName());
                            int deckId = getResources().getIdentifier
                                    ("player_deck", "id", getPackageName());
                            if (targetId == discardId) {
                                removeCardFromTrash(top);
                                addCardToDiscard(playerList.get(0), movingViewName);
                            } else if (targetId == deckId) {
                                removeCardFromTrash(top);
                                addCardToDeck(playerList.get(0), movingViewName);
                            } else if (targetType.equals("hand")) {
                                removeCardFromTrash(top);
                                addCardToHand(playerList.get(0), movingViewName);
                            } else if (targetType.equals("inPlay")) {
                                removeCardFromTrash(top);
                                addCardToPlayArea(movingViewName);
                            } else if (targetId == trashId) {
                                movingView.setAlpha(1f);
                                return false;
                            } else { // do nothing
                            }
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1f);
                    cmt = (CardMultiTag) v.getTag();
                    targetType = cmt.getCardType();
                    if (targetType.equals("inPlay")) v.setBackgroundColor(BACKGROUND_COLOR_DARK);
                    if (!event.getResult()) {
                        movingView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (movingView.getVisibility() != View.VISIBLE) {
                                    movingView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                default:
                    break;
            }
            return true;
        }
    }//MyDragListener




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (HAND_BROWSE_LISTENER == requestCode) {
            int listSize = 0;
            int[] chosenIndexList;
            if (data == null) {
                Toast.makeText(this, "Intent is Null", Toast.LENGTH_SHORT).show();
            } else {
                listSize = data.getIntExtra("listSizeKey", listSize);
                chosenIndexList = data.getIntArrayExtra("indexListKey");
                for (int i = 0; i < listSize; i++) {
                    int index = chosenIndexList[i];
                    ImageView handCardView = handImageViews.get(index);
                    int viewId = handCardView.getId();
                    CardMultiTag cmt = (CardMultiTag) handCardView.getTag();
                    String cardName = cmt.getCardName();
                    removeCardFromHand(playerList.get(0), viewId);
                    addCardToPlayArea(cardName);
                }
            }
        }
        if (requestCode == DISCARD_BROWSE_LISTENER) {
            boolean cardChosen = false;
            int chosenCardIndex;
            if (data == null) {
                Toast.makeText(this, "Intent is Null", Toast.LENGTH_SHORT).show();
            } else {
                cardChosen = data.getBooleanExtra("cardChosenKey", cardChosen);
                chosenCardIndex = data.getIntExtra("chosenCardIndexKey", -1);
                if (cardChosen) {
                    CardMultiTag cmt = discardPileList.get(chosenCardIndex);
                    String cardName = cmt.getCardName();
                    removeCardFromDiscard(playerList.get(0), chosenCardIndex);
                    addCardToPlayArea(cardName);
                }
            }
        }

        if (requestCode == DECK_BROWSE_LISTENER) {
            boolean cardChosen = false;
            int chosenCardIndex;
            if (data == null) {
                Toast.makeText(this, "Intent is Null", Toast.LENGTH_SHORT).show();
            } else {
                cardChosen = data.getBooleanExtra("cardChosenKey", cardChosen);
                chosenCardIndex = data.getIntExtra("chosenCardIndexKey", -1);
                if (cardChosen) {
                    CardMultiTag cmt = deckPileList.get(chosenCardIndex);
                    String cardName = cmt.getCardName();
                    removeCardFromDeck(playerList.get(0), chosenCardIndex);
                    addCardToPlayArea(cardName);
                }
            }
        }
        if (requestCode == TRASH_LISTENER) {
            boolean cardChosen = false;
            int chosenCardIndex;
            if (data == null) {
                Toast.makeText(this, "Intent is Null", Toast.LENGTH_SHORT).show();
            } else {
                cardChosen = data.getBooleanExtra("cardChosenKey", cardChosen);
                chosenCardIndex = data.getIntExtra("chosenCardIndexKey", -1);
                if (cardChosen) {
                    CardMultiTag cmt = trashPileList.get(chosenCardIndex);
                    String cardName = cmt.getCardName();
                    removeCardFromTrash(chosenCardIndex);
                    addCardToPlayArea(cardName);
                }
            }
        }
    }// onActivityResult




    private void addCardToPlayArea(String cardName) {

        int margin = buffer + handBuffer;
        int bottomMargin = cardHeight + buffer * 4;
        int areaZoneWidth = screenWidth - 2 * margin - 2 * buffer; // adjustment to screen width for unknown reason
        int numberOfCards = playAreaViews.size() + 1;
        int pileWidth = ((cardWidth - minOverlap) * (numberOfCards - 1) + cardWidth);
        int overlap;
        int shift;
        if (areaZoneWidth < pileWidth) {
            overlap = minOverlap + (pileWidth - areaZoneWidth) / (numberOfCards - 1);
            shift = 0;
        } else {
            overlap = minOverlap;
            shift = (areaZoneWidth - pileWidth) / 2;
        }
        //shift existing area cards
        for (int i = 0; i < playAreaViews.size(); i++) {
            ImageView existingView = playAreaViews.get(i);
            int leftMargin = margin + shift + (cardWidth - overlap) * i;
            ConstraintLayout.LayoutParams params = new ConstraintLayout
                    .LayoutParams(cardWidth, cardHeight);
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(leftMargin, 0, 0, bottomMargin);
            existingView.setLayoutParams(params);
        }
        //add new card to area
        int i = playAreaViews.size();
        final int FINAL_ID = playAreaViewCatalogue;
        ImageView newView = new ImageView(this);
        newView.setId(PLAY_AREA_VIEWS_ID + FINAL_ID);
        playAreaViewCatalogue += 1;
        int nTag = playAreaViews.size();
        CardMultiTag cmt = new CardMultiTag(nTag, nTag, cardName, "inPlay");
        newView.setTag(cmt);
        Drawable drawable = getImageDps(this, cardName, cardWidth / 2);
        newView.setImageDrawable(drawable);
        playAreaList.add(new CardMultiTag(nTag, nTag, cardName, "inPlay"));
        playAreaViews.add(newView);
        int leftMargin = margin + shift + (cardWidth - overlap) * i;
        ConstraintLayout.LayoutParams params = new ConstraintLayout
                .LayoutParams(cardWidth, cardHeight);
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(leftMargin, 0, 0, bottomMargin);
        newView.setLayoutParams(params);
        layout.addView(newView);
        newView.bringToFront();
        newView.setVisibility(View.VISIBLE);
        addInPlayCardListener(newView);
    }

    private void addCardToHand(Player player, String cardName) {
        player.hand.add(ChooseGameCardsActivity.basicCardSet.getCard(cardName));
        int margin = cardWidth + 2 * buffer + handBuffer;
        int handZoneWidth = screenWidth - 2 * margin - 2 * buffer; // adjustment to screen width for unknown reason
        int numberOfCards = player.hand.size();
        int handWidth = ((cardWidth - minOverlap) * (numberOfCards - 1) + cardWidth);
        int overlap;
        int shift;
        if (handZoneWidth < handWidth) {
            overlap = minOverlap + (handWidth - handZoneWidth) / (numberOfCards - 1);
            shift = 0;
        } else {
            overlap = minOverlap;
            shift = (handZoneWidth - handWidth) / 2;
        }
        //shift existing hand cards
        for (int i = 0; i < player.hand.size() - 1; i++) {
            ImageView existingView = handImageViews.get(i);
            int leftMargin = margin + shift + (cardWidth - overlap) * i;
            ConstraintLayout.LayoutParams params = new ConstraintLayout
                    .LayoutParams(cardWidth, cardHeight);
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(leftMargin, 0, 0, (buffer * 3));
            existingView.setLayoutParams(params);
        }
        //add new card to hand
        int i = player.hand.size() - 1;
        final int FINAL_ID = handImageViewCatalogue;
        ImageView newView = new ImageView(this);
        newView.setId(PLAYER_HAND_VIEW_ID + FINAL_ID);
        handImageViewCatalogue += 1;
        newView.setImageDrawable(getImageDps(this, cardName, (cardWidth / 2)));
        int inTag = handImageViews.size();
        CardMultiTag multiTag = new CardMultiTag(inTag, inTag, cardName, "hand");
        newView.setTag(multiTag);
        handImageViews.add(newView);
        handPileList.add(new CardMultiTag(inTag, inTag, cardName, "hand"));
        int leftMargin = margin + shift + (cardWidth - overlap) * i;
        ConstraintLayout.LayoutParams params = new ConstraintLayout
                .LayoutParams(cardWidth, cardHeight);
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(leftMargin, 0, 0, (buffer * 3));
        newView.setLayoutParams(params);
        newView.bringToFront();
        layout.addView(newView);
        addHandCardListener(newView);
    }

    private void addCardToDiscard(Player player, String cardName) {
        player.discard.add(ChooseGameCardsActivity.basicCardSet.getCard(cardName));
        int discardViewId = getResources().getIdentifier
                ("player_discard", "id", getPackageName());
        ImageView discardView = findViewById(discardViewId);
        discardView.setImageDrawable(getImageDps(this, cardName, (cardWidth / 2)));
        CardMultiTag cardInfo = new CardMultiTag
                (discardPileListCatalogue, discardPileList.size(), cardName, "discard");
        discardPileList.add(cardInfo);
        discardPileListCatalogue += 1;
    }

    private void addCardToDeck(Player player, String cardName) {
        player.deck.add(ChooseGameCardsActivity.basicCardSet.getCard(cardName));
        TextView deckView = (TextView) layout.getViewById(R.id.deck_size);
        CardMultiTag cardInfo = new CardMultiTag
                (deckPileListCatalogue, deckPileList.size(), cardName, "deck");
        deckPileList.add(cardInfo);
        deckPileListCatalogue += 1;
        deckView.setText(String.valueOf(deckPileList.size()));
    }

    private void addCardToTrash(String cardName) {
        CardMultiTag cardInfo = new CardMultiTag
                (trashPileListCatalogue, trashPileList.size(), cardName, "trash");
        trashPileList.add(cardInfo);
        trashPileListCatalogue +=1;
        int viewId = getResources().getIdentifier("trash_size", "id", getPackageName());
        TextView trashSizeView = findViewById(viewId);
        trashSizeView.setText(String.valueOf(trashPileList.size()));
    }

    private void removeCardFromPile(String cardName) {
        int index = 0;
        for (int i = 0; i < bankImageViews.size(); i++) {
            CardMultiTag viewTag = (CardMultiTag) bankImageViews.get(i).getTag();
            String viewName = viewTag.getCardName();
            if (viewName.equals(cardName)) index = i;
        }
        TextView counterView = bankPileCounterViews.get(index);
        String text = (String) counterView.getText();
        int counter = Integer.parseInt(text) - 1;
        if (counter == 0) {
            bankImageViews.get(index).setVisibility(View.INVISIBLE);
            bankImageViews.get(index).setClickable(false);
        }
        text = String.valueOf(counter);
        counterView.setText(text);
    }//removeCardFromBankPile

    private void removeCardFromHand(Player player, int viewId) {
        ImageView view = findViewById(viewId);
        CardMultiTag multiTag = (CardMultiTag) view.getTag();
        int index = multiTag.getCardPosition();
        layout.removeView(handImageViews.get(index));
        handCardRemoved = true;
        handImageViews.remove(index);
        handPileList.remove(index);
        player.hand.remove(index);
        for (int i = index; i < handImageViews.size(); i++) {
            ImageView tailingView = handImageViews.get(i);
            CardMultiTag cmt = handPileList.get(i);
            multiTag = (CardMultiTag) tailingView.getTag();
            int was = multiTag.getCardPosition();
            multiTag.setCardPosition(was - 1);
            cmt.setCardPosition(was - 1);
        }
        int margin = cardWidth + 2 * buffer + handBuffer;
        int handZoneWidth = screenWidth - 2 * margin - 2 * buffer; // adjustment to screen width for unknown reason
        int numberOfCards = player.hand.size();
        int handWidth = ((cardWidth - minOverlap) * (numberOfCards - 1) + cardWidth);
        int overlap;
        int shift;
        if (handZoneWidth < handWidth) {
            overlap = minOverlap + (handWidth - handZoneWidth) / (numberOfCards - 1);
            shift = 0;
        } else {
            overlap = minOverlap;
            shift = (handZoneWidth - handWidth) / 2;
        }
        //shift remaining hand cards
        for (int i = 0; i < player.hand.size(); i++) {
            ImageView existingView = handImageViews.get(i);
            int leftMargin = margin + shift + (cardWidth - overlap) * i;
            ConstraintLayout.LayoutParams params = new ConstraintLayout
                    .LayoutParams(cardWidth, cardHeight);
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(leftMargin, 0, 0, (buffer * 3));
            existingView.setLayoutParams(params);
        }
    }// RemoveCaredFromHand

    private void removeCardFromInPlay(int viewId) {
        ImageView view = findViewById(viewId);
        CardMultiTag multiTag = (CardMultiTag) view.getTag();
        int index = multiTag.getCardPosition();
        layout.removeView(playAreaViews.get(index));
        inPlayCardRemoved = true;
        playAreaViews.remove(index);
        playAreaList.remove(index);
        //player.hand.remove(index);
        for (int i = index; i < playAreaViews.size(); i++) {
            ImageView tailingView = playAreaViews.get(i);
            CardMultiTag cmt = playAreaList.get(i);
            multiTag = (CardMultiTag) tailingView.getTag();
            int was = multiTag.getCardPosition();
            multiTag.setCardPosition(was - 1);
            cmt.setCardPosition(was - 1);
        }
        int margin = buffer + handBuffer;
        int bottomMargin = cardHeight + buffer * 4;
        int handZoneWidth = screenWidth - 2 * margin - 2 * buffer; // adjustment to screen width for unknown reason
        int numberOfCards = playAreaViews.size();
        int handWidth = ((cardWidth - minOverlap) * (numberOfCards - 1) + cardWidth);
        int overlap;
        int shift;
        if (handZoneWidth < handWidth) {
            overlap = minOverlap + (handWidth - handZoneWidth) / (numberOfCards - 1);
            shift = 0;
        } else {
            overlap = minOverlap;
            shift = (handZoneWidth - handWidth) / 2;
        }
        //shift remaining hand cards
        for (int i = 0; i < playAreaViews.size(); i++) {
            ImageView existingView = playAreaViews.get(i);
            int leftMargin = margin + shift + (cardWidth - overlap) * i;
            ConstraintLayout.LayoutParams params = new ConstraintLayout
                    .LayoutParams(cardWidth, cardHeight);
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(leftMargin, 0, 0, bottomMargin);
            existingView.setLayoutParams(params);
        }
    }// RemoveCardFromInPlay


    void removeCardFromDeck(Player player, int index) {
        deckPileList.remove(index);
        player.deck.remove(index);
        int deckCountId = getResources().getIdentifier("deck_size", "id", getPackageName());
        TextView counterView = findViewById(deckCountId);
        counterView.setText(String.valueOf(player.deck.size()));
    }

    void removeCardFromDiscard(Player player, int index) {
        discardPileList.remove(index);
        player.discard.remove(index);
        Drawable drawable;
        if (index <= 0) {
            drawable = getImageDps(this, "back", cardWidth / 2);
        } else {
            String topCardName = discardPileList.get(index - 1).getCardName();
            drawable = getImageDps(this, topCardName, cardWidth / 2);
        }
        int discardId = getResources().getIdentifier("player_discard", "id", getPackageName());
        ImageView imageview = findViewById(discardId);
        imageview.setImageDrawable(drawable);
    }

    void removeCardFromTrash(int index){
        trashPileList.remove(index);
        int trashCountId = getResources().getIdentifier("trash_size", "id", getPackageName());
        TextView counterView = findViewById(trashCountId);
        counterView.setText(String.valueOf(trashPileList.size()));
    }




    private void completeImageViewList(ConstraintLayout layout) {
        int viewId = getResources().getIdentifier("copper", "id", getPackageName());
        ImageView view = (ImageView) layout.getViewById(viewId);
        CardMultiTag viewTag = new CardMultiTag
                (0, 0, "copper", "bank");
        view.setTag(viewTag);
        String pileName = String.valueOf(bankImageViews.size());
        bankImageViews.add(view);
        TextView counterView = makePileSizeCounter(344, 288, pileName, "60");
        layout.addView(counterView);

        viewId = getResources().getIdentifier("silver", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        viewTag = new CardMultiTag
                (0, 0, "silver", "bank");
        view.setTag(viewTag);
        pileName = String.valueOf(bankImageViews.size());
        bankImageViews.add(view);
        counterView = makePileSizeCounter(344, 480, pileName, "40");
        layout.addView(counterView);

        viewId = getResources().getIdentifier("gold", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        viewTag = new CardMultiTag
                (0, 0, "gold", "bank");
        view.setTag(viewTag);
        bankImageViews.add(view);
        counterView = makePileSizeCounter(344, 672, pileName, "30");
        layout.addView(counterView);

        viewId = getResources().getIdentifier("estate", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        viewTag = new CardMultiTag
                (0, 0, "estate", "bank");
        view.setTag(viewTag);
        pileName = String.valueOf(bankImageViews.size());
        bankImageViews.add(view);
        counterView = makePileSizeCounter(192, 288, pileName, "12");
        layout.addView(counterView);

        viewId = getResources().getIdentifier("duchy", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        viewTag = new CardMultiTag
                (0, 0, "duchy", "bank");
        view.setTag(viewTag);
        pileName = String.valueOf(bankImageViews.size());
        bankImageViews.add(view);
        counterView = makePileSizeCounter(192, 480, pileName, "12");
        layout.addView(counterView);

        viewId = getResources().getIdentifier("province", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        viewTag = new CardMultiTag
                (0, 0, "province", "bank");
        view.setTag(viewTag);
        pileName = String.valueOf(bankImageViews.size());
        bankImageViews.add(view);
        counterView = makePileSizeCounter(192, 672, pileName, "12");
        layout.addView(counterView);

        viewId = getResources().getIdentifier("player_deck", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        viewTag = new CardMultiTag
                (0, 0, "deck", "deck");
        view.setTag(viewTag);
        ImageView deckView = view;

        viewId = getResources().getIdentifier("playerB_hand", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        opponentImageViews.add(view);
        viewId = getResources().getIdentifier("playerC_hand", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        opponentImageViews.add(view);
        viewId = getResources().getIdentifier("playerD_hand", "id", getPackageName());
        view = (ImageView) layout.getViewById(viewId);
        opponentImageViews.add(view);

    }




    private void layoutHandAndInPlayAreas() {

        //create camouflage hand view
        ImageView handView = new ImageView(this);
        handView.setId(HAND_AREA_VIEW_ID);
        CardMultiTag cmt =new CardMultiTag(0,0,"handZone","hand");
        handView.setTag(cmt);
        handView.setBackgroundColor(BACKGROUND_COLOR_DARK);
        int margin = cardWidth + 2 * buffer + handBuffer;
        int bottomMargin = 3 * buffer;
        int width = screenWidth - 2 * margin;
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, cardHeight);
        params.leftToLeft =ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom =ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(margin,0,0,bottomMargin);
        handView.setLayoutParams(params);
        layout.addView(handView);

        //create camouflage In Play view
        ImageView inPlayView = new ImageView(this);
        inPlayView.setId(INPLAY_VIEW_ID);
        cmt = new CardMultiTag(0, 0, "inPlayZone", "inPlay");
        inPlayView.setTag(cmt);
        inPlayView.setBackgroundColor(BACKGROUND_COLOR_DARK);
        margin = buffer + handBuffer;
        bottomMargin = cardHeight + 4 * buffer;
        width = screenWidth - 2 * margin;
        params = new ConstraintLayout.LayoutParams(width, cardHeight);
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(margin, 0, 0, bottomMargin);
        inPlayView.setLayoutParams(params);
        layout.addView(inPlayView);
}




    private void displayHand(Player player, ConstraintSet constraintSet,
                                      ConstraintLayout layout) {

        //re-calculate placement to center group of overlapped cards with added card
        int margin = cardWidth + 2*buffer + handBuffer;
        int handZoneWidth = screenWidth - 2*margin - 2*buffer; // adjustment to screen width for unknown reason
        int numberOfCards = player.hand.size();
        int handWidth = ((cardWidth-minOverlap)*(numberOfCards-1)+cardWidth);
        int overlap;
        int shift;
        if (handZoneWidth<handWidth){
            overlap = minOverlap + (handWidth-handZoneWidth)/(numberOfCards-1);
            shift = 0;
        }
        else {
            overlap = minOverlap;
            shift = (handZoneWidth-handWidth)/2;
        }

        for (int i = 0; i < player.hand.size(); i++){
            final int FINALI = handImageViewCatalogue;
            Card card = player.hand.get(i);
            ImageView newView = new ImageView(this);
            newView.setId(PLAYER_HAND_VIEW_ID + FINALI);
            handImageViewCatalogue +=1;
            newView.setImageDrawable(getImageDps(this, card.getName(), (cardWidth/2)));
            String sTag = card.getName();
            int iTag = handImageViews.size();
            CardMultiTag multiTag = new CardMultiTag(iTag, iTag, sTag, "hand");
            newView.setTag(multiTag);
            handImageViews.add(newView);
            handPileList.add(new CardMultiTag(iTag, iTag, sTag, "hand"));
            int leftMargin = margin+shift+(cardWidth-overlap)*i;
            ConstraintLayout.LayoutParams params = new ConstraintLayout
                    .LayoutParams(cardWidth, cardHeight);
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(leftMargin, 0, 0,(buffer*3));
            newView.setLayoutParams(params);
            layout.addView(newView);
        }
        TextView deck = (TextView) layout.getViewById(R.id.deck_size);
        deck.setText(String.valueOf(player.deck.size()));
       // deckCountId = deck.getId();

    }



    private void layoutActionCards(ConstraintLayout layout, ArrayList<Card> gameCards) {
        GameCardStats gameCardStats = new GameCardStats();
        gameCardStats.calculateGameCardStats(gameCards);
        int viewId;
        ImageView view;
        String cardName = "";
        CardMultiTag viewTag;
        TextView counterView;
        String pileName = "";
        int displacement;
        int gameCardIndex = 0;
        int cost12 = gameCardStats.getCardCostTally()[0] + gameCardStats.getCardCostTally()[1]; //number of action cards that cost 1 or 2
        if (cost12 > 0) {
            viewId = getResources().getIdentifier("cost2", "id", getPackageName());
            view = (ImageView) layout.getViewById(viewId);
            pileName = String.valueOf(bankImageViews.size());
            bankImageViews.add(view);
            int cost2CardLocStart = 248*2; //pixels
            int cost2CardLocTop = 144*2; //pixels
            counterView = makePileSizeCounter(cost2CardLocStart, cost2CardLocTop, pileName, "10");
            layout.addView(counterView);
            if (cost12 <= 3) displacement = smallCardWidth + 2 * buffer;
            else {
                int availSpace = 3 * smallCardWidth + 120;
                int neededSpace = cost12 * smallCardWidth + 2 * buffer * (cost12 - 1);
                int overlap = (neededSpace - availSpace) / (cost12 - 1);
                displacement = smallCardWidth + 2 * buffer - overlap;
            }

            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < gameCardStats.getCardCostTally()[j]; i++) { //for cards costing 1 or 2
                    gameCardIndex += 1;
                    if (gameCardIndex == 1) {
                        Drawable drawable = getImageDps(this,
                                gameCards.get(0).getName(), smallCardWidth/2 );
                        view.setImageDrawable(drawable);
                        cardName = gameCards.get(gameCardIndex-1).getName();
                        viewTag = new CardMultiTag(0,0,cardName, "bank");
                        view.setTag(viewTag);
                    } else {
                        int totalDispX = cost2CardLocStart + (displacement * (i));
                        cardName = gameCards.get(gameCardIndex - 1).getName();
                        viewTag = new CardMultiTag(0,0,cardName, "bank");
                        Drawable drawable = getImageDps(this,
                                gameCards.get(gameCardIndex - 1).getName(), smallCardWidth/2);
                        ImageView newView = makeNewImageView(totalDispX, cost2CardLocTop, viewTag, drawable);
                        pileName = String.valueOf(bankImageViews.size());
                        bankImageViews.add(newView);
                        layout.addView(newView);
                        counterView = makePileSizeCounter(totalDispX, cost2CardLocTop, pileName, "10");
                        layout.addView(counterView);
                    }
                }
            }
        }
        int cost3 = gameCardStats.getCardCostTally()[2]; //number of action cards that cost 3
        if (cost3 > 0) {
            viewId = getResources().getIdentifier("cost3", "id", getPackageName());
            view = (ImageView) layout.getViewById(viewId);
            pileName = String.valueOf(bankImageViews.size());
            bankImageViews.add(view);                                                                  //first of cost 3
            int cost3CardLocStart = 248*2; //pixels
            int cost3CardLocTop = 240*2; //pixels
            counterView = makePileSizeCounter(cost3CardLocStart, cost3CardLocTop, pileName, "10");
            layout.addView(counterView);
            if (cost3 <= 3) displacement = smallCardWidth + 2 * buffer;
            else {
                int availSpace = 3 * smallCardWidth + 120;
                int neededSpace = cost3 * smallCardWidth + 2 * buffer * (cost3 - 1);
                int overlap = (neededSpace - availSpace) / (cost3 - 1);
                displacement = smallCardWidth + 2 * buffer - overlap;
            }
            int cost3Index = 0;
            for (int i = 0; i < gameCardStats.getCardCostTally()[2]; i++) { //for cards costing 3
                gameCardIndex += 1;
                cost3Index += 1;
                if (cost3Index == 1) {
                    Drawable drawable = getImageDps(this,
                            gameCards.get(gameCardIndex - 1).getName(), smallCardWidth/2);
                    view.setImageDrawable(drawable);
                    cardName = gameCards.get(gameCardIndex-1).getName();
                    viewTag = new CardMultiTag(0,0,cardName, "bank");
                    view.setTag(viewTag);
                } else {
                    int totalDispX = cost3CardLocStart + (displacement * (i));
                    cardName = gameCards.get(gameCardIndex - 1).getName();
                    viewTag = new CardMultiTag(0,0, cardName, "bank");
                    Drawable drawable = getImageDps(this,
                            gameCards.get(gameCardIndex - 1).getName(), smallCardWidth/2);
                    ImageView newView =  makeNewImageView(totalDispX, cost3CardLocTop, viewTag, drawable);
                    pileName = String.valueOf(bankImageViews.size());
                    bankImageViews.add(newView);
                    layout.addView(newView);
                    counterView = makePileSizeCounter(totalDispX, cost3CardLocTop, pileName, "10");
                    layout.addView(counterView);

                }
            }
        }
        int cost4 = gameCardStats.getCardCostTally()[3]; //number of action cards that cost 3
        if (cost4 > 0) {
            viewId = getResources().getIdentifier("cost4", "id", getPackageName());
            view = (ImageView) layout.getViewById(viewId);
            pileName = String.valueOf(bankImageViews.size());
            bankImageViews.add(view);                                                                  // first of cost 4
            int cost4CardLocStart = 248*2; //pixels
            int cost4CardLocTop = 336*2; //pixels
            counterView = makePileSizeCounter(cost4CardLocStart, cost4CardLocTop, pileName, "10");
            layout.addView(counterView);
            if (cost4 <= 3) displacement = smallCardWidth + 2 * buffer;
            else {
                int availSpace = 3 * smallCardWidth + 120;
                int neededSpace = cost4 * smallCardWidth + 2 * buffer * (cost4 - 1);
                int overlap = (neededSpace - availSpace) / (cost4 - 1);
                displacement = smallCardWidth + 2 * buffer - overlap;
            }
            int cost4Index = 0;
            for (int i = 0; i < gameCardStats.getCardCostTally()[3]; i++) { //for cards costing 3
                gameCardIndex += 1;
                cost4Index += 1;
                if (cost4Index == 1) {
                    Drawable drawable = getImageDps(this,
                            gameCards.get(gameCardIndex - 1).getName(), smallCardWidth/2);
                    view.setImageDrawable(drawable);
                    cardName = gameCards.get(gameCardIndex-1).getName();
                    viewTag = new CardMultiTag(0,0,cardName, "bank");
                    view.setTag(viewTag);
                } else {
                    int totalDispX = cost4CardLocStart + (displacement * (i));
                    cardName = gameCards.get(gameCardIndex - 1).getName();
                    viewTag = new CardMultiTag(0,0, cardName, "bank");
                    Drawable drawable = getImageDps(this,
                            gameCards.get(gameCardIndex - 1).getName(), smallCardWidth/2);
                    ImageView newView = makeNewImageView(totalDispX, cost4CardLocTop, viewTag, drawable);
                    pileName = String.valueOf(bankImageViews.size());
                    bankImageViews.add(newView);
                    layout.addView(newView);
                    counterView = makePileSizeCounter(totalDispX, cost4CardLocTop, pileName, "10");
                    layout.addView(counterView);
                }
            }
        }
        int cost567 = gameCardStats.getCardCostTally()[4] + gameCardStats.getCardCostTally()[5]
                + gameCardStats.getCardCostTally()[6]; //number of action cards that cost 3
        if (cost567 > 0) {
            viewId = getResources().getIdentifier("cost5", "id", getPackageName());
            view = (ImageView) layout.getViewById(viewId);
            pileName = String.valueOf(bankImageViews.size());
            bankImageViews.add(view);                                                                  // first of cost 5, 6 or 7
            int cost5CardLocStart = 248*2; // pixels
            int cost5CardLocTop = 432*2; //pixels
            counterView = makePileSizeCounter(cost5CardLocStart, cost5CardLocTop, pileName, "10");
            layout.addView(counterView);
            if (cost567 <= 3) displacement = smallCardWidth + 2 * buffer;
            else {
                int availSpace = 3 * smallCardWidth + 120;
                int neededSpace = cost567 * smallCardWidth + 2 * buffer * (cost567 - 1);
                int overlap = (neededSpace - availSpace) / (cost567 - 1);
                displacement = smallCardWidth + 2 * buffer - overlap;
            }
            int cost567Index = 0;
            for (int j = 4; j < 7; j++) {
                for (int i = 0; i < gameCardStats.getCardCostTally()[j]; i++) { //for cards costing 5,6, or 7
                    gameCardIndex += 1;
                    cost567Index += 1;
                    if (cost567Index == 1) {
                        Drawable drawable = getImageDps(this,
                                gameCards.get(gameCardIndex - 1).getName(), smallCardWidth/2);
                        view.setImageDrawable(drawable);
                        cardName = gameCards.get(gameCardIndex-1).getName();
                        viewTag = new CardMultiTag(0,0,cardName, "bank");
                        view.setTag(viewTag);
                    } else {
                        int totalDispX = cost5CardLocStart + (displacement * (cost567Index - 1));
                        cardName = gameCards.get(gameCardIndex - 1).getName();
                        viewTag = new CardMultiTag(0,0, cardName, "bank");
                        Drawable drawable = getImageDps(this,
                                gameCards.get(gameCardIndex - 1).getName(), smallCardWidth/2);
                        ImageView newView =  makeNewImageView(totalDispX, cost5CardLocTop, viewTag, drawable);
                        pileName = String.valueOf(bankImageViews.size());
                        bankImageViews.add(newView);
                        layout.addView(newView);
                        counterView = makePileSizeCounter(totalDispX, cost5CardLocTop, pileName, "10");
                        layout.addView(counterView);
                    }
                }
            }
        }
    }



    private void pullOutCards(ArrayList<String> actionCardList) {
        for (int i=0; i<actionCardList.size(); i++){
            String string = actionCardList.get(i);
            Card card = ChooseGameCardsActivity.basicCardSet.getCard(string);
            gameCards.add(card);
        }

    }



    private void reorderByCost(ArrayList<Card> gameCard) {
        for (int i = 0; i < (gameCard.size()-1); i++){
            Card temp;
            for (int j = i+1; j < gameCard.size(); j++){
                if (gameCard.get(i).getCost() > gameCard.get(j).getCost()){
                    temp = gameCard.get(i);
                    gameCard.set(i, gameCard.get(j));
                    gameCard.set(j, temp);
                }
            }
        }
    }



    private boolean checkCurse(ArrayList<String> actionCardList) {
        boolean witchChosen = false;
        for (int i = 0; i < actionCardList.size(); i++){
            if (actionCardList.get(i).equals("witch")) {
                witchChosen = true;
                gameCardList.add("curse");
            }
        }
        return witchChosen;
    }



    public Drawable getImageDps(Activity activity, String imageName, int size){
        Drawable drawable;
        String drawableString = "";
        String[] parsedName = imageName.split("(?=\\p{Upper})");
        if (parsedName.length == 2){
                parsedName[1] = Character.toLowerCase(parsedName[1].charAt(0)) + parsedName[1].substring(1);
                drawableString = parsedName[0] + "_" + parsedName[1] + Integer.toString(size);
        } else if (parsedName.length == 1) {
                drawableString = parsedName[0] + Integer.toString(size);
        }
        int drawableResourceId = activity.getResources().getIdentifier(drawableString,
                    "drawable", activity.getPackageName() );
        drawable = ContextCompat.getDrawable(activity, drawableResourceId);
        return drawable;
    }



    ImageView makeNewImageView(int left, int top, CardMultiTag viewTag, Drawable drawable){
        ImageView view = new ImageView(this);
        ConstraintLayout.LayoutParams params =
                new ConstraintLayout.LayoutParams(smallCardWidth, smallCardHeight);
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(left, top, 0, 0);
        view.setTag(viewTag);
        view.setImageDrawable(drawable);
        view.setLayoutParams(params);
        return view;
    }



    TextView makePileSizeCounter(int left, int top, String nameTag, String text){
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextAppearance(getApplicationContext(), R.style.appPileCounterText);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.alegreya_sc);
        view.setTypeface(typeface);
        left += 20;
        top += 20;
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(left, top, 0, 0);
        view.setTag(nameTag);
        bankPileCounterViews.add(view);                                                       //new imageviews for cost 3
        view.setLayoutParams(params);
        return view;
    }



    void putDiscardInDeck(Player player){
        player.putDiscardInDeck();
        discardPileList.clear();
        discardPileListCatalogue = 0;
        deckPileList.clear();
        deckPileListCatalogue = 0;
        player.shuffleDeck();
        for (int i = 0; i < player.deck.size(); i++){
            String cardName = player.deck.get(i).getName();
            deckPileList.add(new CardMultiTag
                    (deckPileListCatalogue, deckPileListCatalogue, cardName,
                            "deck"));
            deckPileListCatalogue +=1;
        }
        Drawable drawable = getImageDps(this, "back", cardWidth/2);
        ImageView imageView = (ImageView) layout.getViewById(R.id.player_discard);
        imageView.setImageDrawable(drawable);
        TextView deckView = (TextView) layout.getViewById(R.id.deck_size);
        deckView.setText(String.valueOf(player.deck.size()));
    }




    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent motionEvent){
            doubleTap = true;
            return true;
        }
        @Override
        public boolean onDown(MotionEvent motionEvent){
            return true;
        }
    }// GestureListener

}
