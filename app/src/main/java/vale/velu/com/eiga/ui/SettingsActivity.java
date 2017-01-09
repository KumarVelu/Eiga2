package vale.velu.com.eiga.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import vale.velu.com.eiga.R;

/**
 * Created by kumar_velu on 29-12-2016.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MyPreferenceFragment()).commit();

    }

    public static class MyPreferenceFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.sortorder_pref);
        }
    }

}
