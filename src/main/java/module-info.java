module Vortex {
    requires com.dustinredmond.fxtrayicon;
    requires com.fasterxml.jackson.databind;
    requires com.github.kwhat.jnativehook;
    requires com.sun.jna;
    requires java.management;
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jul.to.slf4j;
    requires static lombok;
    requires org.controlsfx.controls;
    requires org.fxmisc.richtext;
    requires org.slf4j;
    requires reactfx;
    requires org.codehaus.janino;

    opens tech.minediamond.vortex.ui     to javafx.fxml, com.fasterxml.jackson.databind, com.sun.jna, javafx.graphics, com.fasterxml.jackson.core;
    opens tech.minediamond.vortex.css     to javafx.fxml, com.fasterxml.jackson.databind, com.sun.jna, javafx.graphics, com.fasterxml.jackson.core;
    opens tech.minediamond.vortex.service     to javafx.fxml, com.fasterxml.jackson.databind, com.sun.jna, javafx.graphics, com.fasterxml.jackson.core;
    opens tech.minediamond.vortex.model     to javafx.fxml, com.fasterxml.jackson.databind, com.sun.jna, javafx.graphics, com.fasterxml.jackson.core;
    opens tech.minediamond.vortex     to javafx.fxml, com.fasterxml.jackson.databind, com.sun.jna, javafx.graphics, com.fasterxml.jackson.core;
}