module com.udacity.catpoint.imageservice {
    exports com.udacity.catpoint.imageservice;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires transitive java.desktop;
    requires software.amazon.awssdk.core;
    requires org.slf4j;
}