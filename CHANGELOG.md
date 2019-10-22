#### Version History:

* `v1.0.0`
  * initial release
* `v1.1.0`
  * Preference `Sender` changed to `Sender must end with`
    * in `v1.0.0`:
      * exact match
    * in `v1.1.0`:
      * value entered in whitelist rule only needs to occur at the end of the SMS `sender`
      * the purpose for this change is to ignore optional country codes, and such
        * example:
          * field value: `9876543210`
          * SMS sender: `+19876543210`
          * is match?:
            * `v1.0.0`: _no_
            * `v1.1.0`: _yes_
* `v1.2.0`
  * Preference `Sender must end with`
    * adds support for a match-all glob pattern
      * when the value for this field in a rule is exactly: `*`
      * then the field will match the SMS sender for all incoming SMS text messages
* `v2.0.0`
  * the idea was borrowed from: [Silent Ping SMS for Android](https://github.com/itds-consulting/android-silent-ping-sms)
  * __details__:
    * uses: `SmsManager.sendDataMessage()`
    * port: `9200`
  * __usage__:
    * to send:
      * open app from launcher
      * click 'silent' menu icon
      * enter phone number of recipient (who must also have _'SMS my GPS'_ installed and its service enabled)
      * click 'send'
    * on receiving end:
      * the SMS does not trigger any notification or sound alert
      * the SMS does not register in the normal text SMS app
      * the SMS does reach _'SMS my GPS'_
        * if the phone number of the sender matches the (non-glob) value of the sender field in any whitelist rule, then a match occurs and a reply text SMS is sent with GPS data.
  * __limitations__:
    * `SmsManager.sendDataMessage()` does not work on CDMA phones
      * GSM only
  * __status__:
    * normal functionality has been tested, and works the same as `v1.2.0`
    * silent data-only functionality is untested
      * I only own CDMA phones
      * I have no idea if `message.getUserData()` in [SMSReceiver](https://github.com/warren-bank/Android-SMS-Automatic-Reply-GPS/blob/v2.0.0/android-studio-project/SMS-my-GPS/src/main/java/com/github/warren_bank/sms_automatic_reply_gps/event/SMSReceiver.java) returns the same `byte[]` out as was originally passed in to `sendDataMessage()` in [SilentSMSSender](https://github.com/warren-bank/Android-SMS-Automatic-Reply-GPS/blob/v2.0.0/android-studio-project/SMS-my-GPS/src/main/java/com/github/warren_bank/sms_automatic_reply_gps/event/SilentSMSSender.java)
      * feedback is welcome
* `v2.0.1`
  * minor fix
    * persist Preference data after deletion of each whitelist rule
