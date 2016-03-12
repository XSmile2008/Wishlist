package com.company.wishlist.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;

import com.company.wishlist.R;
import com.company.wishlist.util.AuthUtils;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;


public class IntroActivity extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(AppIntroFragment.newInstance(getString(R.string.first_intro_title), getString(R.string.first_intro_content),
                R.drawable.circle_gift, Color.parseColor("#2196F3")));

        addSlide(AppIntroFragment.newInstance("Create wishes", Html.fromHtml("Create new wish that allow friends to know your dreams and don't keep it in your mind."),
                R.drawable.own_wish, Color.parseColor("#2196F3")));

        addSlide(AppIntroFragment.newInstance("Keep your own Wish List", Html.fromHtml("Wish list view your wishes that separated by sections, " +
                        "that will help to navigate through list."),
                R.drawable.your_wish, Color.parseColor("#2196F3")));

        addSlide(AppIntroFragment.newInstance("Navigation drawer", Html.fromHtml("This panel view your profile data and friend list. </br> " +
                        "After selection of friend you will see friend wishes and your own list for this friend."),
                R.drawable.friends, Color.parseColor("#2196F3")));

        addSlide(AppIntroFragment.newInstance("Friend wish list", Html.fromHtml("Watching for actual friend dreams and reserve it to give. "),
                R.drawable.friend_wish, Color.parseColor("#2196F3")));

        addSlide(AppIntroFragment.newInstance("Gift list", Html.fromHtml("Bring own wish list for friend."),
                R.drawable.gift_list, Color.parseColor("#2196F3")));

        setBarColor(Color.parseColor("#303F9F"));
        setSeparatorColor(Color.parseColor("#3F51B5"));
        showSkipButton(true);

        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed() {
        AuthUtils.firstOpen();
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        AuthUtils.firstOpen();
        finish();
    }

    @Override
    public void onSlideChanged() {

    }
}
