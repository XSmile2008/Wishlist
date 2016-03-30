package com.company.wishlist.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.company.wishlist.R;
import com.company.wishlist.util.AuthUtils;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

    @Override
    public void init(Bundle savedInstanceState) {

        //Title slide
        addSlide(AppIntroFragment.newInstance(getString(R.string.first_intro_title), getString(R.string.first_intro_content),
                R.drawable.circle_gift, Color.parseColor("#2196F3")));

        addSlide(AppIntroFragment.newInstance("Keep your own Wish List", "Create your own wishlist, " +
                        "fill it with all your wishes, and let your friends know what you wish.",
                R.drawable.tablet, Color.parseColor("#3E2723")));

        addSlide(AppIntroFragment.newInstance("Friends", "Discover your friends wishlists. \n" +
                        "You can watch what your friends wish, and realize their dreams",
                R.drawable.user, Color.parseColor("#0D47A1")));

        addSlide(AppIntroFragment.newInstance("Create wishes", "Create new wish that allow friends to know your dreams and don't keep it in your mind.",
                R.drawable.notes, Color.parseColor("#F57F17")));

        addSlide(AppIntroFragment.newInstance("Add images", "You can choose image for you wish from your phone or use our image search",
                R.drawable.images, Color.parseColor("#33691E")));

        addSlide(AppIntroFragment.newInstance("Reservation", "If you want make some gift from wishlist reserve it and another users will be seen that this wish will be fulfilled by you",
                R.drawable.love, Color.parseColor("#B71C1C")));

        addSlide(AppIntroFragment.newInstance("Notifications", "After you reserve some wish app notify you buy a gift",
                R.drawable.count, Color.parseColor("#263238")));

        showSkipButton(true);
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed() {
        AuthUtils.firstOpen();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        AuthUtils.firstOpen();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    @Override
    public void onSlideChanged() {

    }

}
