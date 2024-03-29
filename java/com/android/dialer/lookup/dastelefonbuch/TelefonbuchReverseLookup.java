/*
 * Copyright (C) 2014 Danny Baumann <dannybaumann@web.de>
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
 * limitations under the License.
 */

package com.android.dialer.lookup.dastelefonbuch;

import android.content.Context;

import com.android.dialer.lookup.ContactBuilder;
import com.android.dialer.lookup.ReverseLookup;
import com.android.dialer.phonenumbercache.ContactInfo;

import java.io.IOException;

public class TelefonbuchReverseLookup extends ReverseLookup {
  private static final String TAG = TelefonbuchReverseLookup.class.getSimpleName();

  public TelefonbuchReverseLookup(Context context) {
  }

  /**
   * Perform phone number lookup.
   *
   * @param context The application context
   * @param normalizedNumber The normalized phone number
   * @param formattedNumber The formatted phone number
   * @return The phone number info object
   */
  @Override
  public ContactInfo lookupNumber(Context context,
      String normalizedNumber, String formattedNumber) throws IOException {
    if (normalizedNumber.startsWith("+") && !normalizedNumber.startsWith("+49")) {
      // Das Telefonbuch only supports German numbers
      return null;
    }

    TelefonbuchApi.ContactInfo info = TelefonbuchApi.reverseLookup(context, normalizedNumber);
    if (info == null) {
      return null;
    }

    return ContactBuilder.forReverseLookup(normalizedNumber, formattedNumber)
        .setName(ContactBuilder.Name.createDisplayName(info.name))
        .addPhoneNumber(ContactBuilder.PhoneNumber.createMainNumber(info.formattedNumber))
        .addWebsite(ContactBuilder.WebsiteUrl.createProfile(info.website))
        .addAddress(ContactBuilder.Address.createFormattedHome(info.address))
        .build();
  }
}
