package app.tasknearby.yashcreations.com.tasknearby;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;

import java.util.ArrayList;
import java.util.Calendar;

import app.tasknearby.yashcreations.com.tasknearby.utils.WeekdayCodeUtils;

public class TaskCreatorNew extends AppCompatActivity {

    LinearLayout layoutNotes, layoutDateTime, layoutContentDateTime, layoutLocation, layoutContentLocation;
    EditText editTextNotes;
    ViewStub weekdaysStub;
    ImageView drop1, drop2, drop3;
    Switch repeatSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_creator_new);
        layoutNotes = findViewById(R.id.layout_note);
        editTextNotes = findViewById(R.id.edit_note);
        layoutDateTime = findViewById(R.id.layout_date_time);
        layoutContentDateTime = findViewById(R.id.layout_content_date_time);
        layoutLocation = findViewById(R.id.layout_location);
        layoutContentDateTime = findViewById(R.id.layout_content_location);
        weekdaysStub = findViewById(R.id.weekdays_stub);
        repeatSwitch = findViewById(R.id.switch_repeat);
        drop1 = findViewById(R.id.drop1);
        drop2 = findViewById(R.id.drop2);
        drop3 = findViewById(R.id.drop3);


        repeatSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                weekdaysStub.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        setupWeekdayBar();

        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim
                .slide_down);

        layoutNotes.setOnClickListener(v -> {
            Toast.makeText(this, "Clicked layout", Toast.LENGTH_SHORT).show();
            if (editTextNotes.getVisibility() == View.GONE) {
                Toast.makeText(this, "Making it visible", Toast.LENGTH_SHORT).show();
                editTextNotes.setVisibility(View.VISIBLE);
                editTextNotes.setAnimation(slideDown);
                drop1.setImageResource(R.drawable.ic_collapse_arrow_up_24);

            } else {
                Toast.makeText(this, "Making it INvisible", Toast.LENGTH_SHORT).show();
                editTextNotes.setVisibility(View.GONE);
                editTextNotes.setAnimation(slideUp);
                drop1.setImageResource(R.drawable.ic_expand_arrow_26);
            }
        });

        layoutDateTime.setOnClickListener(v -> {
            if (layoutContentDateTime.getVisibility() == View.GONE) {
                layoutContentDateTime.setVisibility(View.VISIBLE);
                layoutContentDateTime.setAnimation(slideDown);
                drop2.setImageResource(R.drawable.ic_collapse_arrow_up_24);
            } else {
                layoutContentDateTime.setVisibility(View.GONE);
                layoutContentDateTime.setAnimation(slideUp);

                drop2.setImageResource(R.drawable.ic_expand_arrow_26);

            }
        });

        layoutLocation.setOnClickListener(v -> {
            if(layoutContentLocation.getVisibility() == View.GONE){
                layoutContentLocation.setVisibility(View.VISIBLE);
                layoutContentLocation.setAnimation(slideDown);
                drop3.setImageResource(R.drawable.ic_collapse_arrow_up_24);
            } else {
                layoutContentLocation.setVisibility(View.GONE);
                layoutContentLocation.setAnimation(slideUp);
                drop3.setImageResource(R.drawable.ic_expand_arrow_26);
            }
        });

    }

    private void setupWeekdayBar() {
        // Assumption: No day is selected initially.
        weekdaysStub.setTag(0);
        WeekdaysDataSource wds = new WeekdaysDataSource(this, R.id.weekdays_stub)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setUnselectedColorRes(R.color.dark_grey)
                .start(new WeekdaysDataSource.Callback() {
                    /**
                     * Called every time an item is clicked (selected or deselected).
                     * @param weekdaysDataItem calling getCalendarDayId() on this returns the
                     * day's index as in Java Calendar API. Sunday = 1, Monday = 2....
                     */
                    @Override
                    public void onWeekdaysItemClicked(int i, WeekdaysDataItem weekdaysDataItem) {
                        int dayCode = WeekdayCodeUtils
                                .getDayCodeByCalendarDayId(weekdaysDataItem.getCalendarDayId());
                        int selection = (int) weekdaysStub.getTag();
                        // Doing an XOR here so that if tapped again, then the day is removed.
                        selection ^= dayCode;
                        weekdaysStub.setTag(selection);
                        Log.d("Shilpi", "Selected days : " + selection);
                    }

                    @Override
                    public void onWeekdaysSelected(int i, ArrayList<WeekdaysDataItem> arrayList) {
                    }
                });
        // Need to explicitly make it GONE in code.
        weekdaysStub.setVisibility(View.GONE);
    }
}
