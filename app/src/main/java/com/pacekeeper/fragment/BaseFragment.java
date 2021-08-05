package com.pacekeeper.fragment;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.pacekeeper.activity.BaseActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    @Nullable
    private ProgressDialog mProgressDialog;

    //region Fragment
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }
    //endregion

    //region LoadingUiHandler
    public void showNonCancelableProgressDialog(@NonNull String message) {
        if (getView() == null) {
            return;
        }

        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(getView().getContext());
        } else {
            mProgressDialog.dismiss();
        }

        setKeepScreenOn(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(message);

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void hideNonCancelableProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
    //endregion

    protected abstract int getLayoutResId();

    protected void showToast(String stringRes) {
        Toast.makeText(getContext(), stringRes, Toast.LENGTH_LONG).show();
    }

    protected void showToast(@StringRes int intRes) {
        Toast.makeText(getContext(), intRes, Toast.LENGTH_LONG).show();
    }

    protected void hideKeyboard() {
        if (getActivity() != null) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.hideKeyboard();
        }
    }

    protected void showKeyboard() {
        if (getActivity() != null) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.showKeyboard();
        }
    }

    protected void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    protected void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    protected void removeStickyEvents(@NonNull Class<?>... events) {
        EventBus eventBus = EventBus.getDefault();
        for (Class<?> event : events) {
            eventBus.removeStickyEvent(event);
        }
    }

    protected void showProgress() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).showProgress();
        }
    }

    protected void hideProgress() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).hideProgress();
        }
    }

    //region Utility API
    private void setKeepScreenOn(boolean keepScreenOn) {
        if (null != getActivity()) {
            if (keepScreenOn) {
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }
    //endregion
}
