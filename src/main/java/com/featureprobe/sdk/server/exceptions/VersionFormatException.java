package com.featureprobe.sdk.server.exceptions;


public class VersionFormatException extends IllegalArgumentException {

    public VersionFormatException() {
        super();
    }

    public VersionFormatException(String s) {
        super(s);
    }

    static VersionFormatException forInputString(String s) {
        return new VersionFormatException("For input string: \"" + s + "\"");
    }

}
