package ai.gotit.giap_android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import ai.gotit.giap.GIAP;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private Button signUpButton;
    private Button visitButton;
    private Button askButton;
    private Button setFullNameButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.sign_up_button);
        visitButton = findViewById(R.id.visit_button);
        askButton = findViewById(R.id.ask_button);
        setFullNameButton = findViewById(R.id.set_full_name_button);
        logoutButton = findViewById(R.id.logout_button);

        setAuthState(false);

        final GIAP giap = GIAP.initialize(
                "https://5e4a53406eafb7001488c319.mockapi.io",
                "123",
                MainActivity.this
        );

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInputDialog(InputType.TYPE_CLASS_TEXT, "User ID", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        giap.identify(text);
                        setAuthState(true);
                    }
                });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInputDialog(InputType.TYPE_CLASS_TEXT, "User ID", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        giap.alias(text);
                        setAuthState(true);
                    }
                });
            }
        });

        visitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInputDialog(InputType.TYPE_CLASS_NUMBER, "Economy group", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        try {
                            JSONObject props = new JSONObject();
                            props.put("economy_group", Integer.parseInt(text, 10));
                            giap.track("Visit", props);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        askButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInputDialog(InputType.TYPE_CLASS_NUMBER, "Problem text", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        try {
                            JSONObject props = new JSONObject();
                            props.put("problem_text", text);
                            giap.track("Ask", props);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        setFullNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInputDialog(InputType.TYPE_CLASS_NUMBER, "Full name", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        try {
                            JSONObject props = new JSONObject();
                            props.put("full_name", text);
                            //giap.setProfileProperty(props);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // giap.reset();
                setAuthState(false);
            }
        });
    }

    private void setAuthState(boolean loggedIn) {
        if (loggedIn) {
            loginButton.setVisibility(Button.GONE);
            signUpButton.setVisibility(Button.GONE);
            visitButton.setVisibility(Button.GONE);
            askButton.setVisibility(Button.VISIBLE);
            setFullNameButton.setVisibility(Button.VISIBLE);
            logoutButton.setVisibility(Button.VISIBLE);
        } else {
            loginButton.setVisibility(Button.VISIBLE);
            signUpButton.setVisibility(Button.VISIBLE);
            visitButton.setVisibility(Button.VISIBLE);
            askButton.setVisibility(Button.GONE);
            setFullNameButton.setVisibility(Button.GONE);
            logoutButton.setVisibility(Button.GONE);
        }
    }

    private void showTextInputDialog(int inputType, String title, final OnSubmitDialogListener onSubmit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(inputType);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSubmit.onSubmit(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    interface OnSubmitDialogListener {
        void onSubmit(String text);
    }
}
