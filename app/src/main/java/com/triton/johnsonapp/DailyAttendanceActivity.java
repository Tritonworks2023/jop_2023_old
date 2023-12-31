package com.triton.johnsonapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyLog;
import com.google.gson.Gson;
import com.triton.johnson_tap_app.RestUtils;
import com.triton.johnson_tap_app.api.APIInterface;
import com.triton.johnson_tap_app.api.RetrofitClient;
import com.triton.johnson_tap_app.responsepojo.SuccessResponse;
import com.triton.johnsonapp.activity.MainActivity;
import com.triton.johnsonapp.adapter.DailyAttendanceTableListAdapter;
import com.triton.johnsonapp.interfaces.OnItemClickDataChangeListener;
import com.triton.johnsonapp.requestpojo.AttendanceHelperListRequest;
import com.triton.johnsonapp.requestpojo.HelperAttendanceSubmitRequest;
import com.triton.johnsonapp.responsepojo.AttendanceHelperListResponse;
import com.triton.johnsonapp.session.SessionManager;
import com.triton.johnsonapp.worksheet_joblist.Worksheet_jobno_detailsActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DailyAttendanceActivity extends AppCompatActivity implements OnItemClickDataChangeListener,
        View.OnClickListener{

    public static String formattedDate = "";
    private final String TAG = DailyAttendanceActivity.class.getSimpleName();
    RecyclerView rv_daily_attendance_list;
    EditText f_date;
    TextView txt_NoRecords;
    ImageView iv_back;
    Button submit;
    //    ArrayList<LocalDummyResponse.DataBean> localDummyList;
    AttendanceHelperListResponse attendanceHelperListResponse = new AttendanceHelperListResponse();
    DailyAttendanceTableListAdapter dailyAttendanceTableListAdapter;
    Dialog dialog;
    Context context;
    private DatePickerDialog datepicker;
    private TimePickerDialog timePickerDialog;
    private SharedPreferences sharedPreferences;

    private String se_id,userid,agent_code,str_date;
    private EditText etStartTime, etEndTime, etRemark;
    private int day, month, year;

    private String Locationid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_daily_attendance);

        context = this;
        if(getIntent().hasExtra("Location")){
            // Log.e("CheckingForExtraMain",getIntent().getStringExtra("Location"));
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Locationid = prefs.getString("Location", "");

        }else {
            Log.e("CheckingForExtraMin","null or not found");
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        agent_code = sharedPreferences.getString("agent_code", "");

        SessionManager session = new SessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        userid = user.get(SessionManager.KEY_USERID);

        Log.w("user_id",userid);
        System.out.println("agent_code" + agent_code);

        rv_daily_attendance_list = findViewById(R.id.rv_daily_attendance_list);
        iv_back = findViewById(R.id.iv_back);
        f_date = findViewById(R.id.f_date);
        txt_NoRecords = findViewById(R.id.txt_no_records);
        submit = findViewById(R.id.submit);

        f_date.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        submit.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            str_date = extras.getString("date");
        }

        if(str_date == null){
            getTodayDate();
        }
        else {
            f_date.setText(str_date);
            String inputPattern = "dd-MM-yyyy";
            String outputPattern = "dd-MMM-yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            //SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

            Date date = null;
            String str = null;

            try {
                date = inputFormat.parse(str_date);
                str = sdf.format(date);
                formattedDate = str;

                Log.i(TAG, "setDate: formattedDate-> " + formattedDate);

                callAttendanceHelperList();

            } catch (ParseException e) {
                Log.e(TAG, "setDate: ParseException-> ", e);
            }
        }
    }

    public void itemClickDataChangeListener(int position, String strParam, String strData) {

        if (strParam.equals("status") && strData.equals("Permission")) {
            openDialogStartEndTime(position, strParam, strData);
        } else if (strParam.equals("status")) {
            switch (strData) {
                case "FN - Leave": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("FNL");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
                case "AN - Leave": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("ANL");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
                case "Full Leave": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("FDL");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
                case "Present": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("PRE");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
                case "Absent": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("ABS");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
                case "Com-Off": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("COM");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
                case "Holiday": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("HOLI");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
                case "Out-Station": {
                    attendanceHelperListResponse.getData().get(position).setJlsHaStatus("Station");
                    attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData);
                }
                break;
            }
            dailyAttendanceTableListAdapter.notifyDataSetChanged();
        } else if (strParam.equals("remark") && strData.equals("add")) {
            openDialogAddEditRemark(position, strParam, strData);
        } else if (strParam.equals("remark") && strData.equals("edit")) {
            openDialogAddEditRemark(position, strParam, strData);
        } else if (strParam.equals("remark") && strData.equals("delete")) {
            attendanceHelperListResponse.getData().get(position).setJlsHaRemarks("");
            dailyAttendanceTableListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.f_date) {
            callDatePicker();
        } else if (view.getId() == R.id.submit) {
            for (AttendanceHelperListResponse.Data data : attendanceHelperListResponse.getData()) {
                if (data.getJlsHaStatus() == null || data.getJlsHaStatus().isEmpty()) {
                    Toast.makeText(context, "Please enter attendance for all Helpers", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            callHelperAttendanceSubmit();
        } else if (view.getId() == R.id.iv_back) {
            onBackPressed();
        }
    }

    private void getTodayDate() {
        final Calendar cldr = Calendar.getInstance();
        day = cldr.get(Calendar.DAY_OF_MONTH);
        month = cldr.get(Calendar.MONTH);
        year = cldr.get(Calendar.YEAR);

        setDate(day, month, year);
    }

    private void callDatePicker() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);

        datepicker = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) ->
                        setDate(dayOfMonth, monthOfYear, year1), year, month, day);
        datepicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        datepicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datepicker.show();
    }

    private void setDate(int dayOfMonth, int monthOfYear, int year1) {

        String dateTime = dayOfMonth + "-" + "0"+(monthOfYear + 1) + "-" + year1;
        f_date.setText(dateTime);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("date",dateTime);
        editor.apply();

        System.out.println("date" + dateTime);

        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(dateTime);
            str = outputFormat.format(date);
            formattedDate = str;/*.replace("AM", "am").replace("PM", "pm")*/

            Log.i(TAG, "setDate: formattedDate-> " + formattedDate);

            callAttendanceHelperList();

        } catch (ParseException e) {
            Log.e(TAG, "setDate: ParseException-> ", e);
        }
    }

    private void callTimePicker(String startEnd) {
        final Calendar cldr = Calendar.getInstance();
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int min = cldr.get(Calendar.MINUTE);

        timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) ->
                        setTime(hourOfDay, minute, startEnd), hour, min, true);

        timePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void setTime(int hourOfDay, int minute, String startEnd) {
        Log.i(TAG, "setTime: hourOfDay-> " + hourOfDay + " minute-> " + minute);
        String hrs = hourOfDay+"";
        String mins = minute+"";

        if (mins.length() == 1) {
            mins = "0" + mins;
        }

        if (hrs.length() == 1) {
            hrs = "0" + hrs;
        }

        if (startEnd.equals("start")) {
            etStartTime.setText(hrs + ":" + mins);
        } else if (startEnd.equals("end")) {
            etEndTime.setText(hrs + ":" + mins);
        }
    }

    private void openDialogStartEndTime(int position, String strParam, String strData) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = getLayoutInflater().inflate(R.layout.dialog_time, null);
        etStartTime = mView.findViewById(R.id.etStartTime);
        etEndTime = mView.findViewById(R.id.etEndTime);
        Button btn_submit = mView.findViewById(R.id.btn_submit);
        Button btn_cancel = mView.findViewById(R.id.btn_cancel);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.setCancelable(false);
        dialog.show();

        etStartTime.setOnClickListener(view -> {
            callTimePicker("start");
        });

        etEndTime.setOnClickListener(view -> {
            callTimePicker("end");
        });

        btn_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        btn_submit.setOnClickListener(view -> {
            if (etStartTime.getText().toString().length() == 0) {
                Toast.makeText(this, "Select Start Time", Toast.LENGTH_SHORT).show();
            } else if (etEndTime.getText().toString().length() == 0) {
                Toast.makeText(this, "Select End Time", Toast.LENGTH_SHORT).show();
            } else {
                attendanceHelperListResponse.getData().get(position).setJlsHaStatus("PRM");
                attendanceHelperListResponse.getData().get(position).setJlsHaFromtime(formattedDate + " " + etStartTime.getText().toString().trim());
                attendanceHelperListResponse.getData().get(position).setJlsHaTotime(formattedDate + " " + etEndTime.getText().toString().trim());

                attendanceHelperListResponse.getData().get(position).setStatusDisplay(strData + "\n" + etStartTime.getText().toString().trim() + " - " + etEndTime.getText().toString().trim());
                dailyAttendanceTableListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

    }

    private void openDialogAddEditRemark(int position, String strParam, String strData) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_edit_remark, null);
        etRemark = mView.findViewById(R.id.etRemark);
        Button btn_submit = mView.findViewById(R.id.btn_submit);
        Button btn_cancel = mView.findViewById(R.id.btn_cancel);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.setCancelable(false);
        dialog.show();

        etRemark.setText(
                attendanceHelperListResponse.getData().get(position).getJlsHaRemarks());

        btn_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        btn_submit.setOnClickListener(view -> {
            if (etRemark.getText().toString().length() == 0) {
                Toast.makeText(this, "Enter your Remark", Toast.LENGTH_SHORT).show();
            } else {
                attendanceHelperListResponse.getData().get(position).setJlsHaRemarks(etRemark.getText().toString().trim());
                dailyAttendanceTableListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

    }

    private AttendanceHelperListRequest attendanceHelperListRequest() {
        @SuppressLint("SimpleDateFormat")

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        String dateStr = sdf.format(new Date());
        System.out.println("Date:-" +dateStr.toUpperCase());

        AttendanceHelperListRequest attendanceHelperListRequest = new AttendanceHelperListRequest();
        attendanceHelperListRequest.setJLS_OA_ENGGCODE(agent_code); // agent_code -> my agent_code :E12095 ; Dummy_code : E13503
        attendanceHelperListRequest.setJLS_OA_ATTDATE(formattedDate); // dateStr
        attendanceHelperListRequest.setPhone_number(userid);
        Log.i(TAG, "Attendance Helper List Request --> " + new Gson().toJson(attendanceHelperListRequest));
        return attendanceHelperListRequest;
    }

    private HelperAttendanceSubmitRequest helperAttendanceSubmitRequest() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ",Locale.ENGLISH);
        String Date = sdf.format(new Date());

        HelperAttendanceSubmitRequest helperAttendanceSubmitRequest = new HelperAttendanceSubmitRequest();

        for (int i = 0; i < attendanceHelperListResponse.getData().size(); i++) {

            helperAttendanceSubmitRequest.getAttendanceDetails().add(new HelperAttendanceSubmitRequest.AttendanceDetail(
                    attendanceHelperListResponse.getData().get(i).getJlsHaEnggcode(), attendanceHelperListResponse.getData().get(i).getJlsHaEnggname(),
                    attendanceHelperListResponse.getData().get(i).getJlsHaHelpercode(), attendanceHelperListResponse.getData().get(i).getJlsHaHelpername(),
                    attendanceHelperListResponse.getData().get(i).getJlsHaRoutecd(), attendanceHelperListResponse.getData().get(i).getJlsHaAttdate(),
                    attendanceHelperListResponse.getData().get(i).getJlsHaStatus(), attendanceHelperListResponse.getData().get(i).getJlsHaFromtime(),
                    attendanceHelperListResponse.getData().get(i).getJlsHaTotime(), attendanceHelperListResponse.getData().get(i).getJlsHaSubmitdate(),
                    attendanceHelperListResponse.getData().get(i).getJlsHaRemarks(), attendanceHelperListResponse.getData().get(i).getJlsHaValue()));
        }
        Log.i(TAG, "Attendance Helper List Request " + new Gson().toJson(helperAttendanceSubmitRequest));
        return helperAttendanceSubmitRequest;
    }

    private void callAttendanceHelperList() {

        dialog = new Dialog(context, R.style.NewProgressDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progroess_popup);
        dialog.show();

        APIInterface apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<AttendanceHelperListResponse> call = apiInterface.attendanceHelperList(RestUtils.getContentType(), attendanceHelperListRequest());
        Log.i(TAG, "callAttendanceHelperList url  :%s" + " " + call.request().url());

        call.enqueue(new Callback<AttendanceHelperListResponse>() {
            @Override
            public void onResponse(Call<AttendanceHelperListResponse> call, Response<AttendanceHelperListResponse> response) {

                Log.i(TAG, "callAttendanceHelperList url  :%s" + " " + call.request().url());
                Log.i(VolleyLog.TAG, " callAttendanceHelperList Response" + new Gson().toJson(response.body()));

                dialog.dismiss();

                if (response.body() != null) {

                    if (response.body().getCode() == 200) {

                        Log.i(TAG, "onResponse: AttendanceHelperListResponse - " + new Gson().toJson(response.body()));
                        attendanceHelperListResponse = response.body();

                        if (attendanceHelperListResponse.getData().size() != 0) {

                            for (int i = 0; i < attendanceHelperListResponse.getData().size(); i++) {
                                attendanceHelperListResponse.getData().get(i).setJlsHaAttdate(formattedDate);
                                attendanceHelperListResponse.getData().get(i).setJlsHaSubmitdate(formattedDate);
                            }
                            rv_daily_attendance_list.setVisibility(View.VISIBLE);
                            txt_NoRecords.setVisibility(View.GONE);

                            dailyAttendanceTableListAdapter = new DailyAttendanceTableListAdapter(attendanceHelperListResponse, DailyAttendanceActivity.this);

                            rv_daily_attendance_list.setAdapter(dailyAttendanceTableListAdapter);


                        } else {
                            dialog.dismiss();
                            rv_daily_attendance_list.setVisibility(View.GONE);
                            txt_NoRecords.setVisibility(View.VISIBLE);
                        }

                    } else {
                        dialog.dismiss();
                        rv_daily_attendance_list.setVisibility(View.GONE);
                        txt_NoRecords.setVisibility(View.VISIBLE);
                        txt_NoRecords.setText("Error 404 Found");
                    }

                } else {

                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<AttendanceHelperListResponse> call, Throwable t) {
                dialog.dismiss();
                rv_daily_attendance_list.setVisibility(View.GONE);
                txt_NoRecords.setVisibility(View.VISIBLE);
                txt_NoRecords.setText("Something Went Wrong.. Try Again Later");
                Log.e(TAG, "onFailure: callAttendanceHelperList - onFailure " + t.getMessage(), t);
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void callHelperAttendanceSubmit() {

        dialog = new Dialog(context, R.style.NewProgressDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progroess_popup);
        dialog.show();

        HelperAttendanceSubmitRequest helperAttendanceSubmitRequest = helperAttendanceSubmitRequest();
        Log.i(TAG, "callHelperAttendanceSubmit helperAttendanceSubmitRequest  :%s" + " " + new Gson().toJson(helperAttendanceSubmitRequest));

        APIInterface apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<SuccessResponse> call = apiInterface.helperAttendanceSubmit(RestUtils.getContentType(), helperAttendanceSubmitRequest);

        Log.i(TAG, "callHelperAttendanceSubmit url  : " + call.request().url());

        call.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponse> call, @NonNull Response<SuccessResponse> response) {

                Log.i(TAG, " callHelperAttendanceSubmit Response " + new Gson().toJson(response.body()));

                dialog.dismiss();

                if (response.body() != null) {

                    if (response.body().getCode() == 200) {
                        Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    } else {
                        dialog.dismiss();
                    }

                } else {
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponse> call, @NonNull Throwable t) {
                dialog.dismiss();
                Log.e(TAG, "onFailure: callHelperAttendanceSubmit - onFailure " + t.getMessage(), t);
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DailyAttendanceActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}