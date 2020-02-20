package ai.gotit.giap_android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SignUpDialogFragment extends DialogFragment {

    public interface OnSubmitListener {
        void onSubmit(String userId, String email);
    }

    private OnSubmitListener onSubmitListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_sign_up, null);

        final EditText userIdInput = root.findViewById(R.id.user_id_input);
        final EditText emailInput = root.findViewById(R.id.email_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setView(root)
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                onSubmitListener.onSubmit(userIdInput.getText().toString(), emailInput.getText().toString());
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                )
                .create();
    }

    public void setOnSubmitListener(OnSubmitListener onSubmitListener) {
        this.onSubmitListener = onSubmitListener;
    }
}
