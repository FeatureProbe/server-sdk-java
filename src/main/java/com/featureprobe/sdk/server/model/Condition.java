package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.*;
import java.util.regex.Pattern;

public final class Condition {

    private ConditionType type;

    private String subject;

    private PredicateType predicate;

    private List<String> objects;

    private static final long MILLISECONDS_IN_ONE_SEC = 1000;

    private static final Map<PredicateType, StringMatcher> stringMatchers = new EnumMap<>(PredicateType.class);

    private static final Map<PredicateType, SegmentMatcher> segmentMatchers = new EnumMap<>(PredicateType.class);

    private static final Map<PredicateType, DatetimeMatcher> datetimeMatchers = new EnumMap<>(PredicateType.class);

    private static final Map<PredicateType, NumberMatcher> numberMatchers = new EnumMap<>(PredicateType.class);

    private static final Map<PredicateType, SemverMatcher> semverMatchers = new EnumMap<>(PredicateType.class);

    static {

        stringMatchers.put(PredicateType.IS_ONE_OF, (target, objects) ->
                objects.contains(target));
        stringMatchers.put(PredicateType.ENDS_WITH, (target, objects) ->
                objects.stream().anyMatch(target::endsWith));
        stringMatchers.put(PredicateType.STARTS_WITH, (target, objects) ->
                objects.stream().anyMatch(target::startsWith));
        stringMatchers.put(PredicateType.CONTAINS, (target, objects) ->
                objects.stream().anyMatch(target::contains));
        stringMatchers.put(PredicateType.MATCHES_REGEX, (target, objects) ->
                objects.stream().anyMatch(s -> Pattern.compile(s).matcher(target).find()));
        stringMatchers.put(PredicateType.IS_NOT_ANY_OF, (target, objects) ->
                !objects.contains(target));
        stringMatchers.put(PredicateType.DOES_NOT_END_WITH, (target, objects) ->
                objects.stream().noneMatch(target::endsWith));
        stringMatchers.put(PredicateType.DOES_NOT_START_WITH, (target, objects) ->
                objects.stream().noneMatch(target::startsWith));
        stringMatchers.put(PredicateType.DOES_NOT_CONTAIN, (target, objects) ->
                objects.stream().noneMatch(target::contains));
        stringMatchers.put(PredicateType.DOES_NOT_MATCH_REGEX, (target, objects) ->
                objects.stream().noneMatch(s -> Pattern.compile(s).matcher(target).find()));

        segmentMatchers.put(PredicateType.IS_IN, (user, segments, objects) ->
                objects.stream().anyMatch(s -> segments.get(s).contains(user, segments)));
        segmentMatchers.put(PredicateType.IS_NOT_IN, (user, segments, objects) ->
                objects.stream().noneMatch(s -> segments.get(s).contains(user, segments)));

        datetimeMatchers.put(PredicateType.AFTER, ((target, customValue) -> customValue >= target));
        datetimeMatchers.put(PredicateType.BEFORE, ((target, customValue) -> customValue < target));

        numberMatchers.put(PredicateType.EQUAL, ((customValue, objects) ->
                objects.stream().map(Float::parseFloat).anyMatch(o -> customValue == o)));
        numberMatchers.put(PredicateType.NOT_EQUAL, ((customValue, objects) ->
                objects.stream().map(Float::parseFloat).noneMatch(o -> customValue == o)));
        numberMatchers.put(PredicateType.GREATER_THAN, ((customValue, objects) ->
                objects.stream().map(Float::parseFloat).anyMatch(o -> customValue > o)));
        numberMatchers.put(PredicateType.GREATER_OR_EQUAL, ((customValue, objects) ->
                objects.stream().map(Float::parseFloat).anyMatch(o -> customValue >= o)));
        numberMatchers.put(PredicateType.LESS_THAN, ((customValue, objects) ->
                objects.stream().map(Float::parseFloat).anyMatch(o -> customValue < o)));
        numberMatchers.put(PredicateType.LESS_OR_EQUAL, ((customValue, objects) ->
                objects.stream().map(Float::parseFloat).anyMatch(o -> customValue <= o)));

        semverMatchers.put(PredicateType.EQUAL, ((customValue, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> customValue.compareTo(t) == 0)));
        semverMatchers.put(PredicateType.NOT_EQUAL, ((customValue, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).noneMatch(t -> customValue.compareTo(t) == 0)));
        semverMatchers.put(PredicateType.GREATER_THAN, ((customValue, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> customValue.compareTo(t) > 0)));
        semverMatchers.put(PredicateType.GREATER_OR_EQUAL, ((customValue, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> customValue.compareTo(t) >= 0)));
        semverMatchers.put(PredicateType.LESS_THAN, ((customValue, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> customValue.compareTo(t) < 0)));
        semverMatchers.put(PredicateType.LESS_OR_EQUAL, ((customValue, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> customValue.compareTo(t) <= 0)));

    }

    public boolean matchObjects(FPUser user, Map<String, Segment> segments) {
        switch (type) {
            case STRING:
                String subjectValue = user.getAttr(subject);
                if (StringUtils.isBlank(subjectValue)) {
                    return false;
                }
                return matchStringCondition(subjectValue);

            case SEGMENT:
                return matchSegmentCondition(user, segments);

            case DATETIME:
                return matchDatetimeCondition(user);

            case NUMBER:
                return matchNumberCondition(user);

            case SEMVER:
                return matchSemverCondition(user);

            default:
                return false;
        }
    }

    private boolean matchStringCondition(String subjectValue) {
        StringMatcher stringMatcher = stringMatchers.get(this.predicate);
        if (Objects.isNull(stringMatcher)) {
            return false;
        }

        return stringMatcher.match(subjectValue, this.objects);
    }

    private boolean matchSegmentCondition(FPUser user, Map<String, Segment> segments) {
        SegmentMatcher segmentMatcher = segmentMatchers.get(this.predicate);
        if (Objects.isNull(segmentMatcher)) {
            return false;
        }

        return segmentMatcher.match(user, segments, this.objects);
    }

    private boolean matchDatetimeCondition(FPUser user) {
        DatetimeMatcher datetimeMatcher = datetimeMatchers.get(this.predicate);
        if (Objects.isNull(datetimeMatcher)) {
            return false;
        }

        try {
            String customValue = user.getAttr(this.subject);
            long cv = StringUtils.isBlank(customValue)
                    ? System.currentTimeMillis() / MILLISECONDS_IN_ONE_SEC
                    : Long.parseLong(customValue);
            return this.objects.stream()
                    .mapToLong(Long::parseLong)
                    .anyMatch(t -> datetimeMatcher.match(t, cv));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean matchNumberCondition(FPUser user) {
        NumberMatcher numberMatcher = numberMatchers.get(this.predicate);
        if (Objects.isNull(numberMatcher)) {
            return false;
        }

        try {
            String customValue = user.getAttr(this.subject);
            if (StringUtils.isBlank(customValue)) {
                return false;
            }
            float cv = Float.parseFloat(customValue);
            return numberMatcher.match(cv, this.objects);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean matchSemverCondition(FPUser user) {
        SemverMatcher semverMatcher = semverMatchers.get(this.predicate);
        if (Objects.isNull(semverMatcher)) {
            return false;
        }

        String customValue = user.getAttr(this.subject);
        if (StringUtils.isBlank(customValue)) {
            return false;
        }
        ComparableVersion cv = new ComparableVersion(customValue);
        return semverMatcher.match(cv, this.objects);
    }

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return Objects.isNull(predicate) ? null : predicate.toValue();
    }

    public void setPredicate(PredicateType predicate) {
        this.predicate = predicate;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

}
