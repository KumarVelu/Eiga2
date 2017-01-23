package com.vale.velu.eiga2.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by kumar_velu on 1/5/17.
 */
public abstract class BaseFragment extends Fragment {

    private final static String TAG = BaseFragment.class.getSimpleName();
    private ProgressDialog mProgressDialog;

    protected void closeFragment() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count > 0)
            getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void setArguments(Bundle args) {
        try {
            super.setArguments(args);
        } catch (Exception e) {
            updateArgs(args);
        }
    }

    protected abstract void updateArgs(Bundle args);

    protected void onBackPressed(){

    }

    void showProgresDialog(){
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Loading");
        mProgressDialog.show();
    }

    void dismissProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }
}