# OTP Notifier

![build status](https://travis-ci.org/revanmj/OTP-Notifier.svg?branch=master)

This app can extract OTPs (One Time Passwords) from incoming text messages, show them to you in a clean and readable notifications and optionally copy them to clipboard automatically or by touching "Copy" button in notification.

You can manually add sender numbers or names to the whitelist (or disable it and allow the app to look for OTPs in all incoming messages).

App currently supports OTPs containing only numbers with lenght of 4 to 9 characters. 

I plan on adding ability to set your own regular expression that will be used to scan and extract OTPs (there is support in code, but no UI for this yet).

For this app to work on Android 6.0 and higher, you must launch it at least once and grant it necessary permissions.

App does not have permission to access internet connection, so it is unable to transmit OTPs or text messages anywhere.

Due to recent changes in Google Play policy, app now has to be installed manually (APK can be downloaded from [releases tab](https://github.com/revanmj/SMSPasswordNotifier/releases)).
