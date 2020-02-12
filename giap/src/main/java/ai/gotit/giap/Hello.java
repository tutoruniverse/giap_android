package ai.gotit.giap;

import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

public class Hello {
    private AppCompatActivity mainActivity;

    public Hello(AppCompatActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this.mainActivity).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Alert message to be shown");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
