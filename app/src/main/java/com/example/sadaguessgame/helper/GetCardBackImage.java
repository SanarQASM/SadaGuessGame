package com.example.sadaguessgame.helper;


import com.example.sadaguessgame.R;

public class GetCardBackImage {

    public static int getImage(String key) {

        if (key == null) return 0;

        switch (key) {

            case "animal":
                return R.drawable.animal;

            case "behavior":
                return R.drawable.behavior;

            case "challenge":
                return R.drawable.challenge;

            case "equipment":
                return R.drawable.equipment;

            case "food":
                return R.drawable.food;

            case "general":
                return R.drawable.general;

            case "occupation":
                return R.drawable.occupation;

            case "people":
                return R.drawable.people;

            case "place":
                return R.drawable.place;

            default:
                return 0; // none
        }
    }

}
