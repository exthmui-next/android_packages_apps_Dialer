/*
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2023 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.contacts.common.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.android.dialer.R;
import com.android.dialer.util.PermissionsUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Dialog that clears the frequently contacted list after confirming with the user. */
public class ClearFrequentsDialog extends DialogFragment {

  /** Preferred way to show this dialog */
  public static void show(FragmentManager fragmentManager) {
    ClearFrequentsDialog dialog = new ClearFrequentsDialog();
    dialog.show(fragmentManager, "clearFrequents");
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Context context = getActivity().getApplicationContext();
    final ContentResolver resolver = getActivity().getContentResolver();
    final OnClickListener okListener = (dialog, which) -> {
        if (!PermissionsUtil.hasContactsReadPermissions(context)) {
            return;
        }

        final ProgressDialog progressDialog =
          ProgressDialog.show(
              getContext(),
              getString(R.string.clearFrequentsProgress_title),
              null,
              true,
              true);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {
          resolver.delete(ContactsContract.DataUsageFeedback.DELETE_USAGE_URI, null, null);
          handler.post(progressDialog::dismiss);
        });
    };
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.clearFrequentsConfirmation_title)
        .setMessage(R.string.clearFrequentsConfirmation)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, okListener)
        .setCancelable(true)
        .create();
  }
}
