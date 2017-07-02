package com.vale.velu.eiga2.ui;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;

import com.vale.velu.eiga2.R;

/**
 * Created by kumar_velu on 1/5/17.
 */
public abstract class BaseFragment extends Fragment {

    private ProgressDialog mProgressDialog;

    protected void closeFragment() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count > 0)
            getFragmentManager().popBackStackImmediate();
    }

    void showProgressDialog(){
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.show();
    }

    void dismissProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }
}