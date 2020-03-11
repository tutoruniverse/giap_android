package ai.gotit.giap_android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.gotit.giap.GIAP;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private Button signUpButton;
    private Button visitButton;
    private Button testButton;
    private Button askButton;
    private Button setFullNameButton;
    private Button logoutButton;
    private Button increaseButton;
    private Button decreaseButton;
    private Button addTagsButton;
    private Button removeTagsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.sign_up_button);
        visitButton = findViewById(R.id.visit_button);
        testButton = findViewById(R.id.test_button);
        askButton = findViewById(R.id.ask_button);
        setFullNameButton = findViewById(R.id.set_full_name_button);
        logoutButton = findViewById(R.id.logout_button);
        increaseButton = findViewById(R.id.increase_button);
        decreaseButton = findViewById(R.id.decrease_button);
        addTagsButton = findViewById(R.id.add_tags_button);
        removeTagsButton = findViewById(R.id.remove_tags_button);

        setAuthState(false);

        final GIAP giap = GIAP.initialize(
                "https://analytics-api.got-it.io",
                "demo",
                MainActivity.this
        );

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInputDialog(InputType.TYPE_CLASS_TEXT, "User ID", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        if (text.equals("")) {
                            return;
                        }
                        giap.identify(text);
                        setAuthState(true);
                    }
                });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignUpDialog(new SignUpDialogFragment.OnSubmitListener() {
                    @Override
                    public void onSubmit(String userId, String email) {
                        if (userId.equals("") || email.equals("")) {
                            return;
                        }
                        try {
                            JSONObject props = new JSONObject();
                            props.put("email", email);
                            giap.track("Sign Up", props);
                            giap.alias(userId);
                            giap.updateProfile(props);
                            setAuthState(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                        if (text.equals("")) {
                            return;
                        }
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

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 1; i < 31; i++) {
                    try {
                        JSONObject props = new JSONObject();
                        props.put("index", i);
                        giap.track("Test event", props);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        askButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextInputDialog(InputType.TYPE_CLASS_TEXT, "Problem text", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        if (text.equals("")) {
                            return;
                        }
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
                showTextInputDialog(InputType.TYPE_CLASS_TEXT, "Full name", new OnSubmitDialogListener() {
                    @Override
                    public void onSubmit(String text) {
                        if (text.equals("")) {
                            return;
                        }
                        try {
                            JSONObject props = new JSONObject();
                            props.put("full_name", text);
                            giap.updateProfile(props);
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
                giap.reset();
                setAuthState(false);
            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giap.increaseProperty("count", 1);
            }
        });

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giap.increaseProperty("count", -1);
            }
        });

        addTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagsDialog(new TagsDialogFragment.OnSubmitListener() {
                    @Override
                    public void onSubmit(String[] tags) {
                        JSONArray array = new JSONArray();
                        for (String tag : tags) {
                            array.put(tag);
                        }
                        giap.appendToProperty("tags", array);
                    }
                });
            }
        });

        removeTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagsDialog(new TagsDialogFragment.OnSubmitListener() {
                    @Override
                    public void onSubmit(String[] tags) {
                        JSONArray array = new JSONArray();
                        for (String tag : tags) {
                            array.put(tag);
                        }
                        giap.removeFromProperty("tags", array);
                    }
                });
            }
        });
    }

    private void setAuthState(boolean loggedIn) {
        if (loggedIn) {
            loginButton.setVisibility(Button.GONE);
            signUpButton.setVisibility(Button.GONE);
            visitButton.setVisibility(Button.GONE);
            testButton.setVisibility(Button.GONE);
            askButton.setVisibility(Button.VISIBLE);
            setFullNameButton.setVisibility(Button.VISIBLE);
            logoutButton.setVisibility(Button.VISIBLE);
            increaseButton.setVisibility(Button.VISIBLE);
            decreaseButton.setVisibility(Button.VISIBLE);
            addTagsButton.setVisibility(Button.VISIBLE);
            removeTagsButton.setVisibility(Button.VISIBLE);
        } else {
            loginButton.setVisibility(Button.VISIBLE);
            signUpButton.setVisibility(Button.VISIBLE);
            visitButton.setVisibility(Button.VISIBLE);
            testButton.setVisibility(Button.VISIBLE);
            askButton.setVisibility(Button.GONE);
            setFullNameButton.setVisibility(Button.GONE);
            logoutButton.setVisibility(Button.GONE);
            increaseButton.setVisibility(Button.GONE);
            decreaseButton.setVisibility(Button.GONE);
            addTagsButton.setVisibility(Button.GONE);
            removeTagsButton.setVisibility(Button.GONE);
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

    private void showSignUpDialog(SignUpDialogFragment.OnSubmitListener onSubmitListener) {
        SignUpDialogFragment dialog = new SignUpDialogFragment();
        dialog.setOnSubmitListener(onSubmitListener);
        dialog.show(getSupportFragmentManager(), "sign_up_dialog");
    }

    private void showTagsDialog(TagsDialogFragment.OnSubmitListener onSubmitListener) {
        TagsDialogFragment dialog = new TagsDialogFragment();
        dialog.setOnSubmitListener(onSubmitListener);
        dialog.show(getSupportFragmentManager(), "tags_dialog");
    }

    interface OnSubmitDialogListener {
        void onSubmit(String text);
    }
}
