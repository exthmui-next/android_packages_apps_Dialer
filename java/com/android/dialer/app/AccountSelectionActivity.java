/*
 * Copyright (C) 2018-2023 The LineageOS Project
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
 * limitations under the License.
 */

package com.android.dialer.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.android.contacts.common.widget.SelectPhoneAccountDialogFragment;
import com.android.contacts.common.widget.SelectPhoneAccountDialogOptions;
import com.android.contacts.common.widget.SelectPhoneAccountDialogOptionsUtil;
import com.android.dialer.R;
import com.android.dialer.callintent.CallInitiationType;
import com.android.dialer.callintent.CallIntentBuilder;
import com.android.dialer.util.CallUtil;

import java.util.ArrayList;
import java.util.List;

public class AccountSelectionActivity extends AppCompatActivity {
  public static Intent createIntent(Context context, String number,
          CallInitiationType.Type initiationType) {
    if (TextUtils.isEmpty(number)) {
      return null;
    }

    List<PhoneAccount> accounts =
        CallUtil.getCallCapablePhoneAccounts(context, PhoneAccount.SCHEME_TEL);
    if (accounts == null || accounts.size() <= 1) {
      return null;
    }
    ArrayList<PhoneAccountHandle> accountHandles = new ArrayList<>();
    for (PhoneAccount account : accounts) {
      accountHandles.add(account.getAccountHandle());
    }

    return new Intent(context, AccountSelectionActivity.class)
        .putExtra("number", number)
        .putExtra("accountHandles", accountHandles)
        .putExtra("type", initiationType.ordinal());
  }

  private String number;
  private CallInitiationType.Type initiationType;

  private final SelectPhoneAccountDialogFragment.SelectPhoneAccountListener listener =
      new SelectPhoneAccountDialogFragment.SelectPhoneAccountListener() {
    @Override
    public void onPhoneAccountSelected(PhoneAccountHandle selectedAccountHandle,
        boolean setDefault, String callId) {
      Intent intent = new CallIntentBuilder(number, initiationType)
          .setPhoneAccountHandle(selectedAccountHandle)
          .build();
      startActivity(intent);
      finish();
    }

    @Override
    public void onDialogDismissed(String callId) {
      finish();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    number = getIntent().getStringExtra("number");
    initiationType = CallInitiationType.Type.values()[getIntent().getIntExtra("type", 0)];

    if (getSupportFragmentManager().findFragmentByTag("dialog") == null) {
      List<PhoneAccountHandle> handles = getIntent().getParcelableArrayListExtra("accountHandles",
              PhoneAccountHandle.class);
      SelectPhoneAccountDialogOptions options = SelectPhoneAccountDialogOptionsUtil
          .builderWithAccounts(handles)
          .setTitle(R.string.call_via_dialog_title)
          .setCanSetDefault(false)
          .build();
      SelectPhoneAccountDialogFragment dialog =
          SelectPhoneAccountDialogFragment.newInstance(options, listener);

      dialog.show(getSupportFragmentManager(), "dialog");
    }
  }
}
