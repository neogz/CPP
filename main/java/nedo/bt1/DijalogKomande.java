package nedo.bt1;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by fejzi on 24.08.2016..
 */
public class DijalogKomande extends DialogFragment implements View.OnClickListener{
    private static final String MY_PREFS_NAME = "komande" ;


    Button yes, no;
    EditText otkljucajKomanda, zakljucajKomanda;

    Communicator communicator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communicator = (Communicator) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_komande,null);



        otkljucajKomanda = (EditText) view.findViewById(R.id.editOtkljucajKomanda);
        zakljucajKomanda = (EditText) view.findViewById(R.id.editZakljucavanjeKomanda);
        ucitajVrijednosti(view);

        yes = (Button) view.findViewById(R.id.btnDialogYes);
        no = (Button) view.findViewById(R.id.btnDialogNo);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);






        return view;
    }

    private void ucitajVrijednosti(View v) {
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME,0);
        String restoredUnlock = prefs.getString("unlockk", null);
        String restoredLock = prefs.getString("lockk", null);

        otkljucajKomanda.setText(restoredUnlock);
        zakljucajKomanda.setText(restoredLock);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnDialogYes){
           // Toast.makeText(getActivity(),"yes je klinut", Toast.LENGTH_LONG).show();
            String yesK = otkljucajKomanda.getText().toString();
            String noK = zakljucajKomanda.getText().toString();
            if (yesK != "" &&  noK!= "")
                communicator.onDialogMessage(yesK, noK);

        }
        else {

         Toast.makeText(getActivity(),"no je klinut", Toast.LENGTH_LONG).show();


        }

        dismiss();
    }

    interface Communicator{
        public void onDialogMessage(String yes, String no);
    }
}
