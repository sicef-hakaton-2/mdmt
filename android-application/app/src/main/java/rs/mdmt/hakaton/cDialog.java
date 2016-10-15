package rs.mdmt.hakaton;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by DjoleObli on 11/15/2015.
 */
public class cDialog extends Dialog implements android.view.View.OnClickListener {

    private DialogListener listener;

    public cDialog(Context context) {
        super(context);
        setContentView(R.layout.route_dialog);
    }

    public interface DialogListener
    {
        void okButtonClick(int seats, String time);
    }

    public void setDialogListener(DialogListener lstr)
    {
        listener = lstr;
    }

    @Override
    public void onClick(View v) {

        final EditText seatsText = (EditText) findViewById(R.id.seatsText);
        final EditText timeText = (EditText) findViewById(R.id.timeText);

        listener.okButtonClick(Integer.parseInt(seatsText.getText().toString()), timeText.getText().toString());

    }
}
