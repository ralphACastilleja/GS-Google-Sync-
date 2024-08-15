package com.example;

import java.util.prefs.Preferences;

public class SharedPreferences {
    public static final Preferences prefs = Preferences.userRoot().node("com.example.shared");
}
