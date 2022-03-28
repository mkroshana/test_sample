package com.mkroshana.libraryfeedbacksystem.AdminFrontEnd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.mkroshana.libraryfeedbacksystem.R;
import com.mkroshana.libraryfeedbacksystem.BackEnd.SharedPreferenceClass;
import com.mkroshana.libraryfeedbacksystem.BackEnd.UtilityService;
import com.mkroshana.libraryfeedbacksystem.UserFrontEnd.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity
{
    private Button LoginButton;
    private EditText Email_ET, Password_ET;
    android.widget.ProgressBar ProgressBar;

    private String Email, Password;
    UtilityService utilityService;
    SharedPreferenceClass sharedPreferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        LoginButton = findViewById(R.id.btnLogin);

        Email_ET = findViewById(R.id.txtEmail);
        Password_ET = findViewById(R.id.txtPassword);
        ProgressBar = findViewById(R.id.progressBar);
        utilityService = new UtilityService();
        sharedPreferenceClass = new SharedPreferenceClass(this);

        utilityService.ImmersiveFSMode(this);

        LoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                utilityService.hideKeyboard(v, AdminLoginActivity.this);
                Email = Email_ET.getText().toString();
                Password = Password_ET.getText().toString();

                if (ValidateLoginUser(v))
                {
                    LoginUser(v);
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        SharedPreferences loginPref = getSharedPreferences("user_feedback", MODE_PRIVATE);
        if (loginPref.contains("adminToken"))
        {
            startActivity(new Intent(AdminLoginActivity.this, RegisterActivity.class));
            finish();
        }
    }

    private void LoginUser(View v)
    {
        ProgressBar.setVisibility(View.VISIBLE);
        final HashMap<String, String> params = new HashMap<>();
        params.put("email", Email);
        params.put("password", Password);

        String APIKey = "https://library-feedback-system.herokuapp.com/api/library/auth/admin/login";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, APIKey, new JSONObject(params), new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try {
                    if (response.getBoolean("success"))
                    {
                        String admin_token = response.getString("token");
                        String admin_username = response.getString("username");
                        sharedPreferenceClass.setValue_string("adminToken", admin_token);
                        sharedPreferenceClass.setValue_string("adminUsername",admin_username);
                        //Toast.makeText(AdminLoginActivity.this, token+" "+username, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
                        finish();
                    }
                    ProgressBar.setVisibility(View.GONE);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    ProgressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null)
                {
                    try
                    {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));

                        JSONObject obj = new JSONObject(res);
                        Toast.makeText(AdminLoginActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                        ProgressBar.setVisibility(View.GONE);
                    }
                    catch (JSONException | UnsupportedEncodingException je)
                    {
                        je.printStackTrace();
                        ProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return params;
            }
        };

        //setting retry policies
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        //adding the request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private boolean ValidateLoginUser(View view)
    {
        boolean isValid;

            if (!TextUtils.isEmpty(Email))
            {
                if (!TextUtils.isEmpty(Password))
                {
                    isValid = true;
                }
                else
                {
                    Snackbar.make(view, "Please enter your Password", Snackbar.LENGTH_SHORT).show();
                    isValid = false;
                }
            }
            else
            {
                Snackbar.make(view, "Please enter your Email Address", Snackbar.LENGTH_SHORT).show();
                isValid = false;
            }
        return isValid;
    }
}