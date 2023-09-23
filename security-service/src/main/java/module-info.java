module com.udacity.catpoint.security {
    requires java.desktop;
    requires com.udacity.catpoint.imageservice;
    requires com.miglayout.swing;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    opens com.udacity.catpoint.secservice.data to com.google.gson;
}