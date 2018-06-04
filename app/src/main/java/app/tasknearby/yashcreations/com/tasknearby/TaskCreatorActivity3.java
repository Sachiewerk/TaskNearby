package app.tasknearby.yashcreations.com.tasknearby;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;

import app.tasknearby.yashcreations.com.tasknearby.database.DbConstants;
import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.DistanceUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.WeekdayCodeUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;

public class TaskCreatorActivity3 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TaskCreatorActivity.class.getSimpleName();

    /**
     * Since this activity serves both edit and add task operations, when this extra is set in
     * the calling intent, it will be started in edit mode.
     */
    private static final String EXTRA_EDIT_MODE_TASK_ID = "editTaskIdTaskCreatorActivity";

    /**
     * Request code constants.
     */
    private static final int REQUEST_CODE_PLACE_PICKER = 0;
    private static final int REQUEST_CODE_LOCATION_SELECTION = 1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CODE_CAMERA_IMAGE = 3;
    private static final int REQUEST_CODE_GALLERY_IMAGE_PICKER = 4;

    private EditText taskNameInput;
    private EditText locationNameInput;
    private EditText reminderRangeInput;
    private EditText noteInput;
    private TextView startTimeTv, endTimeTv;
    private TextView startDateTv, endDateTv;
    private TextView unitsTv;
    private ImageView taskImageView, arrowAttachmentImage, arrowScheduleImage;
    private Switch alarmSwitch;
    private Switch anytimeSwitch;
    private Switch repeatSwitch;
    private ViewStub weekdaysStub;
    private LinearLayout selectLocationLayout, selectImageLayout, attachmentTitleLayout,
            scheduleTitleLayout, timeIntervalLayout, startTimeLayout, endTimeLayout,
            startDateLayout, endDateLayout;
    private FrameLayout scheduleFrameLayout, attachmentFrameLayout;

    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * Tells if the task present is being edited or a new one is being created.
     */
    private TaskModel taskBeingEdited = null;

    /**
     * For keeping track of selected location.
     */
    private boolean hasSelectedLocation = false;
    private LocationModel mSelectedLocation;

    private TaskRepository mTaskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_creator3);
        setActionBar();
        // Find views and set click listeners.
        initializeViews();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }


    /**
     * Sets the toolbar as actionBar and also sets the up button.
     */
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setElevation(0);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }
    }


    /**
     * Finds views by id and sets OnClickListener to them.
     */
    private void initializeViews() {

        // initializing all views
        taskNameInput = findViewById(R.id.edit_text_task_name);
        locationNameInput = findViewById(R.id.editText_location_name);
        reminderRangeInput = findViewById(R.id.edit_text_reminder_range);
        noteInput = findViewById(R.id.edit_text_note);
        taskImageView = findViewById(R.id.image_selected_image);
        alarmSwitch = findViewById(R.id.switch_alarm);
        attachmentFrameLayout = findViewById(R.id.frame_layout_attachment);
        scheduleFrameLayout = findViewById(R.id.frame_layout_schedule);
        selectLocationLayout = findViewById(R.id.layout_select_location);
        selectImageLayout = findViewById(R.id.layout_select_image);
        attachmentTitleLayout = findViewById(R.id.layout_title_attachment);
        scheduleTitleLayout = findViewById(R.id.layout_title_schedule);
        arrowAttachmentImage = findViewById(R.id.image_arrow_attachment);
        arrowScheduleImage = findViewById(R.id.image_arrow_schedule);
        unitsTv = findViewById(R.id.text_units);
        anytimeSwitch = findViewById(R.id.switch_anytime);
        timeIntervalLayout = findViewById(R.id.layout_time_interval);
        startTimeLayout = findViewById(R.id.layout_time_from);
        endTimeLayout = findViewById(R.id.layout_time_to);
        startTimeTv = findViewById(R.id.text_time_from);
        endTimeTv = findViewById(R.id.text_time_to);
        startDateLayout = findViewById(R.id.layout_date_from);
        endDateLayout = findViewById(R.id.layout_date_to);
        startDateTv = findViewById(R.id.text_date_from);
        endDateTv = findViewById(R.id.text_date_to);
        repeatSwitch = findViewById(R.id.switch_repeat);
        weekdaysStub = findViewById(R.id.viewStub_repeat);

        // setting on click listeners
        attachmentTitleLayout.setOnClickListener(this);
        scheduleTitleLayout.setOnClickListener(this);
        selectImageLayout.setOnClickListener(this);
        startDateLayout.setOnClickListener(this);
        endDateLayout.setOnClickListener(this);
        startTimeLayout.setOnClickListener(this);
        endTimeLayout.setOnClickListener(this);
        selectLocationLayout.setOnClickListener(this);

        // setting defaults and other settings

        // setting default distance range
        String defReminderRange = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_distance_range_key),
                        getString(R.string.pref_distance_range_default));
        reminderRangeInput.setText(defReminderRange);

        // setting units
        setReminderRangeUnits();

        // setting anytime switch
        anytimeSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            timeIntervalLayout.setVisibility((isChecked) ? View.GONE : View.VISIBLE);
        }));

        // setting time interval tags with default value
        startTimeTv.setTag(new LocalTime(0, 0));
        endTimeTv.setTag(new LocalTime(23, 59));

        // setting date interval tags with default value
        startDateTv.setTag(new LocalDate());
        endDateTv.setTag(null);

        // setting repeat switch
        repeatSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                weekdaysStub.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        // setting weekday stub
        setupWeekdayBar();
    }

    /**
     * Specifies the action to be taken when a view is clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.layout_title_attachment:
                if (attachmentFrameLayout.getVisibility() == View.GONE) {
                    expandAttachment();
                } else {
                    collapseAttachment();
                }
                break;
            case R.id.layout_title_schedule:
                if (scheduleFrameLayout.getVisibility() == View.GONE) {
                    expandSchedule();
                } else {
                    collapseSchedule();
                }
                break;
            case R.id.layout_select_image:
                addTaskImage();
                break;
            case R.id.layout_time_from:
                timeSelectionTriggered(startTimeTv);
                break;
            case R.id.layout_time_to:
                timeSelectionTriggered(endTimeTv);
                break;
            case R.id.layout_date_from :
                dateSelectionTriggered(startDateTv);
                break;
            case R.id.layout_date_to :
                dateSelectionTriggered(endDateTv);
                break;
            case R.id.layout_select_location :
                onPlacePickerRequested();
                break;

        }
    }

    /**
     * Returns the slideup animation.
     */
    private Animation getSlideUpAnimation() {
        return AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
    }

    /**
     * Returns the slidedown animation.
     */
    private Animation getSlideDownAnimation() {
        return AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
    }

    /**
     * Expands the attachment layout
     */
    private void expandAttachment() {
        attachmentFrameLayout.setVisibility(View.VISIBLE);
        attachmentFrameLayout.startAnimation(getSlideDownAnimation());
        arrowAttachmentImage.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24px);
    }

    /**
     * Collapses attachment layout.
     */
    private void collapseAttachment() {
        attachmentFrameLayout.setVisibility(View.GONE);
        attachmentFrameLayout.startAnimation(getSlideUpAnimation());
        arrowAttachmentImage.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24px);
    }

    /**
     * Expands schedule layout.
     */
    private void expandSchedule() {
        scheduleFrameLayout.setVisibility(View.VISIBLE);
        scheduleFrameLayout.startAnimation(getSlideDownAnimation());
        arrowScheduleImage.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24px);
    }

    /**
     * Collapses schedule layout.
     */
    private void collapseSchedule() {
        scheduleFrameLayout.setVisibility(View.GONE);
        scheduleFrameLayout.startAnimation(getSlideUpAnimation());
        arrowScheduleImage.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24px);
    }

    /**
     * Handles the adding of the task image.This also checks and requests if required permissions
     * are not available.
     */
    private void addTaskImage() {
        mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_ADD_IMAGE, new Bundle());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            // Permission is available.
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images
                    .Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_CODE_GALLERY_IMAGE_PICKER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addTaskImage();
                } else {
                    Toast.makeText(this, R.string.creator_error_image_permission,
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_GALLERY_IMAGE_PICKER:
                if (resultCode == RESULT_OK) {
                    onTaskImageSelected(data);
                }
                break;
            case REQUEST_CODE_PLACE_PICKER:
                if (resultCode == RESULT_OK) {
                    onPlacePickerSuccess(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Action to take when user selectes the image.
     */
    private void onTaskImageSelected(Intent data) {
        if (data.getData() == null) {
            Toast.makeText(this, R.string.creator_msg_image_selection_failed, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Uri selectedImageUri = data.getData();
        taskImageView.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load(selectedImageUri)
                .fit()
                .centerCrop()
                .into(taskImageView);
        // We need to generate the image file path from the uri.
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn,
                null, null, null);
        cursor.moveToFirst();
        String imageFilePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
        cursor.close();
        // Set the path as a tag to the imageView for storing in the database
        // for retrieval later on.
        taskImageView.setTag(imageFilePath);
    }

    /**
     * sets units for reminder range.
     */
    private void setReminderRangeUnits() {
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitsPref = defaultPref.getString(getString(R.string.pref_unit_key), getString(R
                .string.pref_unit_default));
        if (unitsPref.equals(getString(R.string.pref_unit_metric))) {
            unitsTv.setText(getString(R.string.unit_metres));
        } else {
            unitsTv.setText(getString(R.string.unit_yards));
        }
    }

    private void setupWeekdayBar() {
        // Assumption: No day is selected initially.
        weekdaysStub.setTag(0);
        WeekdaysDataSource wds = new WeekdaysDataSource(this, R.id.viewStub_repeat)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setUnselectedColorRes(R.color.dark_grey)
                .start(new WeekdaysDataSource.Callback() {
                    /**
                     * Called every time an item is clicked (selected or deselected).
                     *
                     * @param weekdaysDataItem calling getCalendarDayId() on this returns the
                     *                         day's index as in Java Calendar API. Sunday = 1,
                     *                         Monday = 2....
                     */
                    @Override
                    public void onWeekdaysItemClicked(int i, WeekdaysDataItem weekdaysDataItem) {
                        int dayCode = WeekdayCodeUtils
                                .getDayCodeByCalendarDayId(weekdaysDataItem.getCalendarDayId());
                        int selection = (int) weekdaysStub.getTag();
                        // Doing an XOR here so that if tapped again, then the day is removed.
                        selection ^= dayCode;
                        weekdaysStub.setTag(selection);
                        Log.d(TAG, "Selected days : " + selection);
                    }

                    @Override
                    public void onWeekdaysSelected(int i, ArrayList<WeekdaysDataItem> arrayList) {
                    }
                });
        // Need to explicitly make it GONE in code.
        weekdaysStub.setVisibility(View.GONE);
    }

    private void timeSelectionTriggered(TextView v) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    Log.d(TAG, "Time selected, " + hourOfDay + ":" + minute);
                    // storing the time object in the textView itself.
                    LocalTime localTime = new LocalTime(hourOfDay, minute);
                    v.setTag(localTime);
                    // set selected Time on textView.
                    v.setText(AppUtils.getReadableTime(TaskCreatorActivity3.this, localTime));
                }, 12, 0, false); // time at which timepicker opens.
        timePickerDialog.show();
    }

    /**
     * Called when user clicks on Date display.
     */
    private void dateSelectionTriggered(TextView v) {
        Calendar calendar = Calendar.getInstance();
        // what to do when date is set.
        DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month,
                                                                dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            v.setTag(LocalDate.fromCalendarFields(calendar));
            v.setText(AppUtils.getReadableDate(this, calendar.getTime()));
            Log.d(TAG, "Date selected: " + calendar.getTime().toString());
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
                calendar.get(Calendar.YEAR),            // current year.
                calendar.get(Calendar.MONTH),           // current month (0 indexed)
                calendar.get(Calendar.DAY_OF_MONTH));   // current day.
        datePickerDialog.show();
    }

    /**
     * Triggered when the user clicks on the Pick Place button.
     */
    private void onPlacePickerRequested() {
        if (!isInternetConnected())
            return;
        PlacePicker.IntentBuilder placePickerIntent = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(placePickerIntent.build(this), REQUEST_CODE_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            mFirebaseAnalytics.logEvent(AnalyticsConstants.PLACE_PICKER_EXCEPTION, new Bundle());
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            mFirebaseAnalytics.logEvent(AnalyticsConstants.PLACE_PICKER_FATAL, new Bundle());
            e.printStackTrace();
        }
    }

    /**
     * Checks for internet permission. If internet is not connected, it shows a snackbar and
     * return false.
     */
    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if (cm != null && cm.getActiveNetworkInfo() == null) {
            // No internet connection present. Show snackbar.
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R
                            .string.creator_no_internet_error),
                    Snackbar.LENGTH_SHORT);
            snackbar.show();
            return false;
        }
        return true;
    }

    /**
     * Initializes the location with place picker returned data. Also sets that to the UI.
     */
    private void onPlacePickerSuccess(Intent data) {
        Place place = PlacePicker.getPlace(this, data);
        // Create a new location object with use count = 1
        mSelectedLocation = new LocationModel(place.getName().toString(),
                place.getLatLng().latitude,
                place.getLatLng().longitude,
                1, 0, new LocalDate());
        hasSelectedLocation = true;
        onLocationSelected();
    }

    /**
     * Sets the selected location's name to the input textView.
     */
    private void onLocationSelected() {
        locationNameInput.setText(mSelectedLocation.getPlaceName());
        locationNameInput.setVisibility(View.VISIBLE);
    }



    //    LinearLayout layoutSelectLocation;
//    EditText editTextLocation;
//    LinearLayout layoutSelectImage;
//    ImageView imageSelected;
//    ViewStub viewStubRepeat;
//    LinearLayout layoutTitleAttachment, layoutTitleSchedule;
//    ConstraintLayout layoutContentAttachment, layoutContentSchedule;
//    Animation slideUp, slideDown;
//    Switch switchRepeat;
//    ImageView imageArrowAttachment, imageArrowSchedule;
//    RecyclerView recyclerView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_task_creator3);
//
//        layoutSelectLocation = findViewById(R.id.layout_select_location);
//        editTextLocation = findViewById(R.id.editText_location_name);
//        layoutSelectImage = findViewById(R.id.layout_select_image);
//        imageSelected = findViewById(R.id.image_selected_image);
//        viewStubRepeat = findViewById(R.id.viewStub_repeat);
//        layoutContentAttachment = findViewById(R.id.layout_content_attachment);
//        layoutContentSchedule = findViewById(R.id.layout_content_schedule);
//        layoutTitleAttachment = findViewById(R.id.layout_title_attachment);
//        layoutTitleSchedule = findViewById(R.id.layout_title_schedule);
//        slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
//        slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
//        switchRepeat = findViewById(R.id.switch_repeat);
//        imageArrowAttachment = findViewById(R.id.image_arrow_attachment);
//        imageArrowSchedule = findViewById(R.id.image_arrow_schedule);
//
//        recyclerView = findViewById(R.id.recycler_view_location);
//
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setElevation(0);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
//        actionBar.setTitle("Add Task");
//
//        switchRepeat.setOnCheckedChangeListener((buttonView,isChecked)->
//            viewStubRepeat.setVisibility(isChecked ?View.VISIBLE :View.GONE));
//
//    setupWeekdayBar();
//
//        layoutSelectLocation.setOnClickListener(v ->
//
//    {
//        editTextLocation.setVisibility(View.VISIBLE);
//    });
//
//        layoutSelectImage.setOnClickListener(v ->
//
//    {
//        imageSelected.setVisibility(View.VISIBLE);
//    });
//
//        layoutTitleAttachment.setOnClickListener(v ->
//
//    {
//        if (layoutContentAttachment.getVisibility() == View.GONE) {
//            layoutContentAttachment.setVisibility(View.VISIBLE);
//            layoutContentAttachment.startAnimation(slideDown);
//            imageArrowAttachment.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24px);
//
//        } else {
//            layoutContentAttachment.setVisibility(View.GONE);
//            layoutContentAttachment.startAnimation(slideUp);
//            imageArrowAttachment.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24px);
//        }
//
//    });
//
//        layoutTitleSchedule.setOnClickListener(v ->
//
//    {
//        if (layoutContentSchedule.getVisibility() == View.GONE) {
//            layoutContentSchedule.setVisibility(View.VISIBLE);
//            layoutContentSchedule.startAnimation(slideDown);
//            imageArrowSchedule.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24px);
//
//        } else {
//            layoutContentSchedule.setVisibility(View.GONE);
//            layoutContentSchedule.startAnimation(slideUp);
//            imageArrowSchedule.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24px);
//        }
//
//    });
//
//    LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this,
//            LinearLayoutManager.HORIZONTAL, false);
//        recyclerView.setLayoutManager(horizontalLayoutManager);
//        recyclerView.setAdapter(new
//
//    RecyclerAdapter());
//}
//
//    private void setupWeekdayBar() {
//        // Assumption: No day is selected initially.
//        viewStubRepeat.setTag(0);
//        WeekdaysDataSource wds = new WeekdaysDataSource(this, R.id.viewStub_repeat)
//                .setFirstDayOfWeek(Calendar.MONDAY)
//                .setUnselectedColorRes(R.color.dark_grey)
//                .start(new WeekdaysDataSource.Callback() {
//                    /**
//                     * Called every time an item is clicked (selected or deselected).
//                     * @param weekdaysDataItem calling getCalendarDayId() on this returns the
//                     * day's index as in Java Calendar API. Sunday = 1, Monday = 2....
//                     */
//                    @Override
//                    public void onWeekdaysItemClicked(int i, WeekdaysDataItem weekdaysDataItem) {
//                        int dayCode = WeekdayCodeUtils
//                                .getDayCodeByCalendarDayId(weekdaysDataItem.getCalendarDayId());
//                        int selection = (int) viewStubRepeat.getTag();
//                        // Doing an XOR here so that if tapped again, then the day is removed.
//                        selection ^= dayCode;
//                        viewStubRepeat.setTag(selection);
//                        Log.d("Shilpi", "Selected days : " + selection);
//                    }
//
//                    @Override
//                    public void onWeekdaysSelected(int i, ArrayList<WeekdaysDataItem> arrayList) {
//                    }
//                });
//        // Need to explicitly make it GONE in code.
//        viewStubRepeat.setVisibility(View.GONE);
//    }
//
//class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.LocationViewHolder> {
//
//    @NonNull
//    @Override
//    public RecyclerAdapter.LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
//            viewType) {
//        View v = getLayoutInflater().inflate(R.layout.list_item_location_chip, parent, false);
//        return new LocationViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerAdapter.LocationViewHolder holder, int
//            position) {
//        holder.bind(position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return 5;
//    }
//
//    public class LocationViewHolder extends RecyclerView.ViewHolder {
//        TextView textView;
//
//        public LocationViewHolder(View itemView) {
//            super(itemView);
//            textView = itemView.findViewById(R.id.text_view);
//        }
//
//        public void bind(int position) {
//
//            if (position == 4) {
//                textView.setText("MORE");
//                textView.setTextColor(Color.BLUE);
//                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
//                textView.setBackground(null);
//            }
//        }
//    }
//}

}
