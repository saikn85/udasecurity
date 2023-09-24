module com.udacity.catpoint.security {
    requires com.google.common;
    requires com.google.gson;
    requires com.miglayout.swing;
    requires com.udacity.catpoint.imageservice;
    requires java.desktop;
    requires java.prefs;

    opens com.udacity.catpoint.secservice.data to com.google.gson;
}