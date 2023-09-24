module com.udacity.catpoint.imageservice {
    exports com.udacity.catpoint.imageservice;

    requires transitive java.desktop;
    requires org.slf4j;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.rekognition;
}