package com.credr.android.launcher.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.credr.android.launcher.R;
import com.credr.android.launcher.Utils.Utils;
import com.credr.android.library.connection.REST;
import com.credr.android.library.connection.model.request.Credentials;
import com.credr.android.library.connection.model.response.LoginResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vijayagnihotri on 01/10/15.
 */
public class LoginFragment extends DialogFragment implements View.OnClickListener {

    private Context mContext;
    private EditText username, password;
    private RelativeLayout progressBar;
    public static final String LOGIN_FRAGMENT_TAG = "Login";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity != null)
            mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View cView = inflater.inflate(R.layout.login_popup, null);
        username = (EditText) cView.findViewById(R.id.username);
        password = (EditText) cView.findViewById(R.id.password);
        progressBar = (RelativeLayout) cView.findViewById(R.id.progressBarContainer);
        Button submitButton = (Button) cView.findViewById(R.id.btnLogin);
        submitButton.setOnClickListener(this);
        return cView;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnLogin) {
            String login = username.getText().toString();
            String pass =  password.getText().toString();
            if (isCredentialsValid(login, pass)){
                progressBar.setVisibility(View.VISIBLE);
                doLogin(login, pass);
            } else {
                Toast.makeText(mContext,"Please enter a login/password",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doLogin(final String login, String pass) {
        REST.API.login(new Credentials(login, pass), new Callback<LoginResponse>() {
            @Override
            public void success(LoginResponse loginResponse, Response response) {
                progressBar.setVisibility(View.GONE);
                loginResponse.user.save();
                if(loginResponse.user.is_rm) {
                    mContext.getPackageManager().clearPackagePreferredActivities(mContext.getPackageName());
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    editor.putBoolean(Utils.PREF_LOCKING_MODE, false);
                    editor.commit();
                    Toast.makeText(mContext, "Locking mode turned off", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                } else {
                    Toast.makeText(mContext, "You do not have permissions", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressBar.setVisibility(View.GONE);
                String message = "";
                switch (error.getKind()) {
                    case NETWORK:  {
                        message = error.getMessage();
                        if (message == null) {
                            message = "Network Error";
                        }
                    } break;
                    case HTTP: {
                        switch (error.getResponse().getStatus()) {
                            case 401:  {
                                message = ((LoginResponse)error.getBody()).message;
                            } break;
                            default: message = (String)error.getBodyAs(String.class);
                        }
                    }
                }
                Toast.makeText(mContext, "" + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isCredentialsValid(String login, String pass) {
        if(login != null && !login.isEmpty() && pass != null && !pass.isEmpty())
            return true;
        else
            return false;
    }
}
