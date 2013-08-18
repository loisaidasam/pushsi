pushsi
======

A simple library for sending push notifications to your phone, particularly via command line.

Useful for monitoring long-running scripts or knowing when cron jobs finish without having to babysit them!

## Overview

This project is broken up into three pieces:

1. A `command-line` python script that can be used for initiating the push notifications
2. The `web` interface that provides an API for sending push notifications to your phone
3. The `android` app that handles receiving of those push notifications

## Command-Line

Lib not yet on pypi. For now, clone this repo and set it up manually:

    $ git clone git@github.com:loisaidasam/pushsi.git
    $ cd pushsi/command-line
    $ python setup.py install

Usage:

To register your phone with your computer, click the register button in the Android app and then on the command line use the four digit pin provided like this:

    $ pushsi -r <your four digit pin here>

To check the status of what phones are hooked up to your computer:

    $ pushsi -s
    1 phone currently hooked up to this computer:
    Phone UUID:	Registered:
    <UUID will be here>	2013-07-23 10:59:03

Once your phone is registered with your computer, use the command-line interface like this:

    $ pushsi echo "hello"

which will execute `echo "hello"` and then send the results to your phone via pushsi. Cool huh?

Another example:

    $ pushsi python my_long_running_script.py

This one will execute `python my_long_running_script.py` and then send the results to your phone. Get it?

For help:

    $ pushsi -h

## Web

Available in the wild here: http://push.si

## Android

Not yet on the Android market, but available for now here: https://github.com/loisaidasam/pushsi/blob/master/android/bin/Pushsi-Android.apk?raw=true


## TODO:

- Change command-line into a python lib
- Make python lib available on pypi
- Migrate from [C2DM](https://developers.google.com/android/c2dm/) to [GCM](http://developer.android.com/google/gcm/index.html)
- Make more data available in the Android app
- Release to Android market
