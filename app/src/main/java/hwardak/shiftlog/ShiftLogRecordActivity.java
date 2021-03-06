package hwardak.shiftlog;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * This activity is where the history of shifts is displayed. They can be viewed in here a summary
 * type view or a detailed view by date. The views can be requested via selections made in the
 * spinners.
 */
public class ShiftLogRecordActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    ShiftsDataAccess shiftsDataAccess = new ShiftsDataAccess(this);

    Spinner displayBySpinner;
    Spinner employeeSpinner;
    Spinner daySpinner;
    Spinner monthSpinner;
    Spinner yearSpinner;

    TextView totalHoursEditText;
    TextView overLapHoursTextView;
    TextView overLapHoursPerDayTextView;
    TextView detailedDayDateTextView;

    int monthNumber;
    String monthString;

    int year;

    ArrayList<String> shiftList;

    int employeeId;
    String employee;

    int counter = 0;

    /**
     * Activity creation point, includes instantiation of views and variables, also invokes methods
     * that populate the activity's views.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_log_record);


        instantiateTextViews();
        instantiateDisplayBySpinner();
        instantiateEmployeeSpinner();
        instantiateDaySpinner();
        instantiateMonthSpinner();
        instantiateYearSpinner();


        loadShifts(shiftsDataAccess.getShiftsCursor());
        loadHoursDetails(shiftsDataAccess.getTotalHoursByMonthYearEmployee(), shiftsDataAccess.getTotalDistinctDates());

    }

    /**
     * EditTexts and TextViews are instantiated here.
     */
    private void instantiateTextViews() {
        totalHoursEditText = (TextView) findViewById(R.id.totalHoursTextView);
        overLapHoursTextView = (TextView) findViewById(R.id.overLapHoursTextView);
        overLapHoursPerDayTextView = (TextView) findViewById(R.id.overLapHoursPerDayTextView);
        detailedDayDateTextView = (TextView) findViewById(R.id.detailedDayDateTextView);
    }


    /**
     * Instantiate spinner responsible for display selections. User can pick; All Shifts, Detailed
     * Day, or Pay Period.
     */
    private void instantiateDisplayBySpinner() {
        displayBySpinner = (Spinner) findViewById(R.id.displayBySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.displayByArray, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        displayBySpinner.setAdapter(adapter);

        displayBySpinner.setOnItemSelectedListener(this);


    }

    /**
     * Spinner for selecting which employee's shifts to display in the All Shifts display.
     */
    private void instantiateEmployeeSpinner() {
        employeeSpinner = (Spinner) findViewById(R.id.employeeSpinner);

        ArrayList<String> employeeList = shiftsDataAccess.getEmployeeList();
        employeeList.add(0, "All Employees");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, employeeList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(adapter);

        employeeSpinner.setOnItemSelectedListener(this);

    }

    /**
     * Spinner for selecting which year's shifts to display in the All Shifts display.
     */
    private void instantiateYearSpinner() {
        yearSpinner = (Spinner) findViewById(R.id.yearSpinner);

        ArrayList<String> yearList = shiftsDataAccess.getYearList();
        yearList.add(0, "All Years");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, yearList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);

        yearSpinner.setOnItemSelectedListener(this);

    }

    /**
     * Spinner for selecting which day's shifts to display in the All Shifts display.
     * CURRENTLY NOT IN USE.
     */
    private void instantiateDaySpinner() {
        daySpinner = (Spinner) findViewById(R.id.daySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        daySpinner.setOnItemSelectedListener(this);

    }

    /**
     * Spinner for selecting which month's shifts to display in the All Shifts display.
     */
    private void instantiateMonthSpinner() {
        monthSpinner = (Spinner) findViewById(R.id.monthSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        monthSpinner.setOnItemSelectedListener(this);

    }


    /**
     * Whenever any of the Spinners in this activity are interacted with, this method is invoked.
     * Depending on which Spinner (parent) has invoked it, and the selection(s) made, the respective
     * set of selections from each spinner are passed to a method in the dataAccess layer. Which
     * will return a new cursor, whihc is passed to the loadShifts method, which will refresh the
     * ListView with the data in the new cursor.
     * Depending on the above Spinner selections, the respective amount of hours is also pulled from
     * the DB and passed to the loadHoursDetails method.
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        //When each spinner is instantiated, this method is called, invoking it unessecarily the
        //the first time. Thus a counter was placed to skip the body of this method the first 4
        //times, once for each spinner.

        counter++;

        if (counter >= 4) {

            //If monthSpinner item is selected.
            if (parent == monthSpinner) {
                if (position != 0) {
                    //The position number is one higher than month's number value, so we negate one to
                    //level them.
                    monthNumber = position - 1;
                } else {
                    monthNumber = -1;
                }
                monthString = parent.getItemAtPosition(position).toString();
            }

            //If yearSpinner item is selected.
            if (parent == yearSpinner) {
                if (position != 0) {
                    year = Integer.parseInt(parent.getItemAtPosition(position).toString());
                } else {
                    year = 0;
                }
            }

            //If employeeSpinner item is selected.
            if (parent == employeeSpinner) {
                if (position != 0) {
                    employee = parent.getItemAtPosition(position).toString();
                } else {
                    employee = null;
                }
            }

            //Assign the values from the above spinner in this String array for later handling by
            //data access layer.
            String[] monthYearEmployee = {String.valueOf(monthNumber), String.valueOf(year), employee};


            //If displayBySpinner item is selected.
            if (parent == displayBySpinner) {

                // Display all shifts.
                if (position == 0) {
                    detailedDayDateTextView.setVisibility(View.GONE);
                    loadShifts(shiftsDataAccess.getShiftsCursor(monthYearEmployee));
                    loadHoursDetails(shiftsDataAccess.getTotalHoursByMonthYearEmployee(monthYearEmployee), shiftsDataAccess.getTotalDistinctDates(monthYearEmployee));
                    loadHoursDetails(shiftsDataAccess.getTotalHoursByMonthYearEmployee(String.valueOf(monthNumber), String.valueOf(year), employee), shiftsDataAccess.getTotalDistinctDates(String.valueOf(monthNumber), String.valueOf(year), employee));

                    //Display detailed day
                } else if (position == 1) {
                    detailedDayDateTextView.setVisibility(View.VISIBLE);

                    loadDetailedDay(null);

                    //Display shifts by pay period
                } else if (position == 2) {
                    loadPayPeriod();
                }
            }

            //If all shift are to be displayed, the sub-selection of month, year, and employee are
            //used to display those specific shifts.
            //The hours worked details are recalculated
            if (displayBySpinner.getSelectedItemPosition() == 0) {
                loadShifts(shiftsDataAccess.getShiftsCursor(String.valueOf(monthNumber), String.valueOf(year), employee));
                loadHoursDetails(shiftsDataAccess.getTotalHoursByMonthYearEmployee(String.valueOf(monthNumber), String.valueOf(year), employee), shiftsDataAccess.getTotalDistinctDates(String.valueOf(monthNumber), String.valueOf(year), employee));
            } else if (displayBySpinner.getSelectedItemPosition() == 1) {
                //TODO: fix this.
//                loadHoursDetails(shiftsDataAccess.getTotalHoursByDate());
            }
        }
    }

    private void loadPayPeriod() {


    }


    /**
     * This method instantiates,
     * a String[] of Columns name who's contents we would like, and an Array of ints, each
     * representing the resource id of the view within the row layout. There must be an equal count
     * of resource ID's and Column names.
     * The resource id, column name arrays, along with the resource id of the row layout we
     * want to use are passed the SimpleListAdapter constructor
     *
     * @param cursor
     */
    private void loadShifts(Cursor cursor) {

        //All shifts are retrieved if the cursor is null. Like when this activity is first created.
        if (cursor == null) {
            cursor = shiftsDataAccess.getShiftsCursor();
        }

        //Array of columns that are needed for our row layout.
        String[] columns = {
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_EMPLOYEE_NAME,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DATE,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DECLARED_START_TIME,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DECLARED_END_TIME,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_HOURS_WORKED
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SHIFT_ID,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_EMPLOYEE_ID,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_TILL_NUMBER,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_STARTING_TILL_AMOUNT,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_REDEMPTIONS,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DRIVE_OFFS,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_FINAL_DROP_AMOUNT,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SHORT_OVER,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_PRINT_OUT_TERMINAL_COUNT,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_PRINT_OUT_PASSPORT_COUNT,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_PRINT_OUT_DIFFERENCE,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_START,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_ADD,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_CLOSE,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_SOLD,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_PASSPORT,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_DIFFERENCE,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_ACTUAL_START_TIME,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_ACT
        };


        //Int array of resource ID's, one for each column in the columns String array.
        int[] resourceIds = {
                R.id.listviewRowEmployeeName,
                R.id.listviewRowDate,
                R.id.listviewRowStartTime,
                R.id.listviewRowEndTime,
                R.id.listviewRowHoursWorked

        };

        //Resource ID of the Listview we want to populate.
        ListView listView = (ListView) findViewById(R.id.shiftRecordListView);


        //The array of resource ID's, column name's, and layout for the listview row are passed to
        // the below constructor to be assemebled.
        ListAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.shift_listview_row,
                cursor, columns, resourceIds, 0);

        listView.setAdapter(listAdapter);

        listView.invalidateViews();

    }


    private void loadDetailedDay(String date) {
        Cursor cursor;

        if (date == null) {
            date = shiftsDataAccess.getMostRecentDate();
        }

        date = date.trim();

        //Retrieve a cursor of shifts of a given date.
        cursor = shiftsDataAccess.getShiftsCursorByDate(date.substring(0, 10), date.substring(11));

        //Set that date in the date textview.
        detailedDayDateTextView.setText(date.substring(0, 10) + " " + date.substring(10));


        //Array of columns that are needed for our row layout.
        String[] columns = {
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_EMPLOYEE_NAME,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DATE,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DECLARED_START_TIME,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DECLARED_END_TIME,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_HOURS_WORKED,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SHIFT_ID,
//                ShiftLogDBOpenHelper.SHIFTS_COLUMN_EMPLOYEE_ID,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_TILL_NUMBER,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_STARTING_TILL_AMOUNT,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_REDEMPTIONS,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_DRIVE_OFFS,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_FINAL_DROP_AMOUNT,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SHORT_OVER,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_PRINT_OUT_TERMINAL_COUNT,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_PRINT_OUT_PASSPORT_COUNT,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_PRINT_OUT_DIFFERENCE,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_START,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_ADD,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_CLOSE,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_SOLD,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_PASSPORT,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_SCRATCH_DIFFERENCE,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_ACTUAL_START_TIME,
                ShiftLogDBOpenHelper.SHIFTS_COLUMN_ACTUAL_END_TIME};


        //Int array of resource ID's, one for each column in the columns String array.
        int[] resourceIds = {
                R.id.detailedDayListViewRowName,
//                R.id.detailedDayListViewRowDate,
                R.id.detailedDayListViewRowStartTime,
                R.id.detailedDayListViewRowEndTime,
                R.id.detailedDayListViewRowHoursWorked,
                R.id.detailedDayListViewRowTill,
                R.id.detailedDayListViewStartingTill,
                R.id.detailedDayListViewRedemptions,
                R.id.detailedDayListViewDriveOffs,
                R.id.detailedDayListViewFinalDrop,
                R.id.detailedDayListViewShortOver,
                R.id.detailedDayListViewPrintOutTerminal,
                R.id.detailedDayListViewPrintOutPassport,
                R.id.detailedDayListViewPrintOutDifference,
                R.id.detailedDayListViewScratchStart,
                R.id.detailedDayListViewScratchAdd,
                R.id.detailedDayListViewScratchClose,
                R.id.detailedDayListViewScratchSold,
                R.id.detailedDayListViewScratchPassport,
                R.id.detailedDayListViewScratchDifference


        };


        ListView listView = (ListView) findViewById(R.id.shiftRecordListView);

        ListAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.detailed_day_listview_row,
                cursor, columns, resourceIds, 0) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the current item from ListView
                View view = super.getView(position, convertView, parent);
                ++position;

                //Rows are to be alternatively colored, blue, green, red, and repeating.
                switch (position % 3){
                    case 0:
                        view.setBackgroundColor(Color.parseColor("#990033")); // red
                        break;
                    case 1:
                        view.setBackgroundColor(Color.parseColor("#0066ff")); //blue
                        break;
                    case 2:
                        view.setBackgroundColor(Color.parseColor("#009900")); // green

                }

                return view;
            }


        };



        listView.setAdapter(listAdapter);

        listView.invalidateViews();




    }

    /**
     * This method receives the total hours and days for a given query of shifts. The data is then
     * presented to the user through its respective EditText.
     * Total hours, overlap hours, and overlap hours per day are set in the EditTexts.
     *
     * Since different stores have different hours of operations, the variable numberOfHoursOpen,
     * should be updated, through a settings activity.
     *
     * @param totalHours
     * @param totalDays
     */
    private void loadHoursDetails(double totalHours, int totalDays) {
        int numberOfHoursOpen = 19;

        totalHoursEditText.setText(String.valueOf(Math.floor(totalHours*4)/4));

        overLapHoursPerDayTextView.setText(String.valueOf(Math.floor(totalHours/totalDays*4)/4));

        double overLapHours = totalHours - (totalDays * numberOfHoursOpen);
        overLapHours = Math.floor((overLapHours*4)/4);

        overLapHoursTextView.setText(String.valueOf(overLapHours));

    }


    @Override
    public void onNothingSelected (AdapterView < ? > parent){

    }

    /**
     * Retrieves the previous date to the date in display in the Detailed Day view.
     * @param view
     * @throws ParseException
     */
    public void onPreviousButtonClick(View view) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd yyyyy");
        Date myDate = simpleDateFormat.parse(detailedDayDateTextView.getText().toString());
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(myDate);
        cal1.add(Calendar.DAY_OF_YEAR, -1);
        Date previousDate = cal1.getTime();

        Log.d("previous date ", String.valueOf(previousDate).substring(0,10) + " " + String.valueOf(previousDate).substring(24));

        loadDetailedDay(String.valueOf(previousDate).substring(0,10) + " " + String.valueOf(previousDate).substring(24));

    }

    /**
     * Retrieves the next date to the date in display in the Detailed Day view.
     * @param view
     * @throws ParseException
     */
    public void onNextButtonClick(View view) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd yyyyy");
        Date myDate = simpleDateFormat.parse(detailedDayDateTextView.getText().toString());
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(myDate);
        cal1.add(Calendar.DAY_OF_YEAR, +1);
        Date previousDate = cal1.getTime();

        Log.d("previous date ", String.valueOf(previousDate).substring(0,10) + " " + String.valueOf(previousDate).substring(24));

        loadDetailedDay(String.valueOf(previousDate).substring(0,10) + " " + String.valueOf(previousDate).substring(24));

    }


}
