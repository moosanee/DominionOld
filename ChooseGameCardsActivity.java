package com.example.dominion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChooseGameCardsActivity extends AppCompatActivity {

    private static final int BASIC_CARDS_ACTIVITY_CODE = 6;
    private static final int INTRIGUE_CARDS_ACTIVITY_CODE = 7;
    private static final int GAME_BOARD_ACTIVITY_CODE = 8;
    private static final int TESTER_BUTTON_ID = 474747;
    final static int ACCENT_COLOR = Color.parseColor("#ffe9be");
    final static int BACKGROUND_COLOR_DARK = Color.parseColor("#363c61");

    final static public BasicCards basicCardSet = new BasicCards(); //define a basic set of cards
    ArrayList<String> gameCardList = new ArrayList<>(); //define the set of game "action" cards
    ArrayList<Card> gameCards = new ArrayList<>();
    ArrayList<PlayerInfo> playerList = new ArrayList<>();
    GameCardStats gameCardStats = new GameCardStats();
    Context context = this;
    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game_cards);
        this.playerList = (ArrayList<PlayerInfo>) getIntent().getSerializableExtra("playerListKey");
        constraintLayout = findViewById(R.id.activity_choose_game_cards);

        addTesterButton(constraintLayout);

        addButtonListeners();

    }

    private void addTesterButton(ConstraintLayout layout) {
        Button testerButton = new Button(this);
        testerButton.setId(TESTER_BUTTON_ID);
        testerButton.setText("Test Cards");
        testerButton.setTextSize(20f);
        testerButton.setTextColor(BACKGROUND_COLOR_DARK);
        testerButton.setBackgroundResource(R.drawable.text120);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.alegreya_sc);
        testerButton.setTypeface(typeface);
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        testerButton.setLayoutParams(params);
        layout.addView(testerButton);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(TESTER_BUTTON_ID, ConstraintSet.TOP,
                constraintLayout.getId(), constraintSet.TOP);
        constraintSet.connect(TESTER_BUTTON_ID, ConstraintSet.END,
                constraintLayout.getViewById(R.id.set_up_game_button).getId(), constraintSet.END);
        constraintSet.setMargin(TESTER_BUTTON_ID, ConstraintSet.TOP, 800);
        constraintSet.applyTo(constraintLayout);
    }

    private void addButtonListeners() {
        Button chooseBasicCards = findViewById(R.id.basic_cards_button);
        chooseBasicCards.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), BasicCardsActivity.class);
                intent.putStringArrayListExtra("gameCardListKey", gameCardList);
                Bundle statBundle = new Bundle();
                statBundle.putSerializable("cardStatKey", gameCardStats);
                intent.putExtra("statBundleKey", statBundle);
                startActivityForResult(intent, BASIC_CARDS_ACTIVITY_CODE);
            }
        });
        Button chooseIntrigueCards = findViewById(R.id.intrigue_button);
        chooseIntrigueCards.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), IntrigueCardsActivity.class);
                intent.putStringArrayListExtra("gameCardListKey", gameCardList);
                Bundle statBundle = new Bundle();
                statBundle.putSerializable("cardStatKey", gameCardStats);
                intent.putExtra("statBundleKey", statBundle);
                startActivityForResult(intent, INTRIGUE_CARDS_ACTIVITY_CODE);
            }
        });
        Button chooseTesterCards = findViewById(TESTER_BUTTON_ID);
        chooseTesterCards.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                gameCardList.clear();
                gameCards.clear();
                gameCardList.add("chapel");
                gameCardList.add("moat");
                gameCardList.add("merchant");
                gameCardList.add("village");
                gameCardList.add("gardens");
                gameCardList.add("poacher");
                gameCardList.add("smithy");
                gameCardList.add("market");
                gameCardList.add("witch");
                gameCardList.add("artisan");
                gameCards.add(basicCardSet.getCard("chapel"));
                gameCards.add(basicCardSet.getCard("moat"));
                gameCards.add(basicCardSet.getCard("merchant"));
                gameCards.add(basicCardSet.getCard("village"));
                gameCards.add(basicCardSet.getCard("gardens"));
                gameCards.add(basicCardSet.getCard("poacher"));
                gameCards.add(basicCardSet.getCard("smithy"));
                gameCards.add(basicCardSet.getCard("market"));
                gameCards.add(basicCardSet.getCard("witch"));
                gameCards.add(basicCardSet.getCard("artisan"));
                gameCardStats.calculateGameCardStats(gameCards);
                setCountViews(gameCardStats, gameCardList);

            }
        });

        Button setUpGame = findViewById(R.id.set_up_game_button);
        setUpGame.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                if (playerList.size() == 0) {
                    Toast.makeText(getApplicationContext(),
                            "please fill out player information\non previous screen",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(view.getContext(), GameBoardActivity.class);
                    intent.putStringArrayListExtra("gameCardListKey", gameCardList);
                    intent.putExtra("playerListKey", playerList);
                    startActivityForResult(intent, GAME_BOARD_ACTIVITY_CODE);
                }
            }
        });


        final Button returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putStringArrayListExtra("gameCardListKey", gameCardList);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //ConstraintLayout layout = findViewById(R.id.activity_game_board);
        //ImageView curseView;
        if (requestCode == BASIC_CARDS_ACTIVITY_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> returnedCardList = data.getStringArrayListExtra("cardListKey");
                pullOutNewCards(returnedCardList);
                gameCardStats.calculateGameCardStats(gameCards);
                setCountViews(gameCardStats, gameCardList);


            }
        }
    }

    private void setCountViews(GameCardStats gameCardStats, ArrayList<String> cardList){
        TextView view = findViewById(R.id.costs1_count);
        view.setText(String.valueOf(gameCardStats.getCardCostTally()[0]));
        view = findViewById(R.id.costs2_count);
        view.setText(String.valueOf(gameCardStats.getCardCostTally()[1]));
        view = findViewById(R.id.costs3_count);
        view.setText(String.valueOf(gameCardStats.getCardCostTally()[2]));
        view = findViewById(R.id.costs4_count);
        view.setText(String.valueOf(gameCardStats.getCardCostTally()[3]));
        view = findViewById(R.id.costs5_count);
        view.setText(String.valueOf(gameCardStats.getCardCostTally()[4]));
        view = findViewById(R.id.costs6_count);
        view.setText(String.valueOf(gameCardStats.getCardCostTally()[5]));
        view = findViewById(R.id.costs7_count);
        view.setText(String.valueOf(gameCardStats.getCardCostTally()[6]));

        view = findViewById(R.id.extra_card1_count);
        view.setText(String.valueOf(gameCardStats.getCardDrawTally()[0]));
        view = findViewById(R.id.extra_card2_count);
        view.setText(String.valueOf(gameCardStats.getCardDrawTally()[1]));
        view = findViewById(R.id.extra_card3_count);
        view.setText(String.valueOf(gameCardStats.getCardDrawTally()[2]));
        view = findViewById(R.id.extra_card4_count);
        view.setText(String.valueOf(gameCardStats.getCardDrawTally()[3]));

        view = findViewById(R.id.extra_action1_count);
        view.setText(String.valueOf(gameCardStats.getExtraActionTally()[0]));
        view = findViewById(R.id.extra_action2_count);
        view.setText(String.valueOf(gameCardStats.getExtraActionTally()[1]));
        view = findViewById(R.id.extra_action3_count);
        view.setText(String.valueOf(gameCardStats.getExtraActionTally()[2]));
        view = findViewById(R.id.extra_action4_count);
        view.setText(String.valueOf(gameCardStats.getExtraActionTally()[3]));

        view = findViewById(R.id.extra_buy1_count);
        view.setText(String.valueOf(gameCardStats.getExtraBuysTally()[0]));
        view = findViewById(R.id.extra_buy2_count);
        view.setText(String.valueOf(gameCardStats.getExtraBuysTally()[1]));
        view = findViewById(R.id.extra_buy3_count);
        view.setText(String.valueOf(gameCardStats.getExtraBuysTally()[2]));
        view = findViewById(R.id.extra_buy4_count);
        view.setText(String.valueOf(gameCardStats.getExtraBuysTally()[3]));

        view = findViewById(R.id.extra_coin1_count);
        view.setText(String.valueOf(gameCardStats.getExtraCoinTally()[0]));
        view = findViewById(R.id.extra_coin2_count);
        view.setText(String.valueOf(gameCardStats.getExtraCoinTally()[1]));
        view = findViewById(R.id.extra_coin3_count);
        view.setText(String.valueOf(gameCardStats.getExtraCoinTally()[2]));
        view = findViewById(R.id.extra_coin4_count);
        view.setText(String.valueOf(gameCardStats.getExtraCoinTally()[3]));

        view = findViewById(R.id.attacks_count);
        view.setText(String.valueOf(gameCardStats.getcardTypeTally().get(3).getTypeTally()));
        view = findViewById(R.id.trashes_count);
        view.setText(String.valueOf(gameCardStats.getTrashTally()));
        view = findViewById(R.id.card_count);
        view.setText(String.valueOf(cardList.size()));
    }

    private void pullOutNewCards(ArrayList<String> actionCardList) {
        for (int i=0; i<actionCardList.size(); i++){
            String string = actionCardList.get(i);
            int index;
            boolean inList = false;
            for (int j = 0; j < gameCards.size(); j++){
                if (gameCards.get(j).getName().equals(string)) {
                    inList = true;
                    index = j;
                }
            }
            if (!inList) {
                Card card = ChooseGameCardsActivity.basicCardSet.getCard(string);
                gameCards.add(card);
                gameCardList.add(string);
            }
        }

    }
}
