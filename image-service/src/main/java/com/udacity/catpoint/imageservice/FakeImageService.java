package com.udacity.catpoint.imageservice;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Service that tries to guess if an image displays a cat.
 */
public class FakeImageService implements ImageService {
    private final Random r;

    public FakeImageService() {
        r = new Random();
    }

    @Override
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshold) {
        return r.nextBoolean();
    }
}
