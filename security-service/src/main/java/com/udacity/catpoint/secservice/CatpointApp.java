package com.udacity.catpoint.secservice;

import com.udacity.catpoint.secservice.application.CatpointGui;

/**
 * This is the main class that launches the application.
 */
public class CatpointApp {
    public static void main(String[] args) {
        CatpointGui gui = new CatpointGui();
        gui.setVisible(true);
    }
}
