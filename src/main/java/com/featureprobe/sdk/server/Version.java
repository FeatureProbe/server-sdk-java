package com.featureprobe.sdk.server;

import com.featureprobe.sdk.server.exceptions.VersionFormatException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Version {

    private static final Pattern versionRegex = Pattern.compile("[0-9]+(\\.[0-9]+){2,}");

    private List<Integer> items;

    public Version(String version) {
        this.items = parseVersions(version);
    }

    public boolean isVersion(String item) {
        return versionRegex.matcher(item).matches();
    }

    public boolean greaterThan(Version other) {
        int selfLength = items.size();
        int otherLength = other.items.size();
        for (int index = 0; index < selfLength; index++) {
            if (index > otherLength - 1) {
                return true;
            }
            int value = this.items.get(index);
            int otherValue = other.items.get(index);
            if (value > otherValue) {
                return true;
            } else if (value < otherValue) {
                return false;
            }
        }
        return false;
    }

    public boolean greaterThanOrEqual(Version other) {
        return greaterThan(other) || equals(other);
    }

    public boolean equals(Version other) {
        return other.items.equals(this.items);
    }

    public boolean lessThan(Version other) {
        return other.greaterThan(this);
    }

    public boolean lessThanOrEqual(Version other) {
        return lessThan(other) || equals(other);
    }

    public static Version parseVersion(String version) {
        return new Version(version);
    }

    private List<Integer> parseVersions(String version) throws VersionFormatException {
        String[] fields = version.split("\\.");
        List<Integer> versions = new ArrayList<>();
        try {
            for (String field : fields) {
                versions.add(Integer.parseInt(field));
            }
        } catch (NumberFormatException e) {
            throw new VersionFormatException();
        }
        return versions;
    }

}
