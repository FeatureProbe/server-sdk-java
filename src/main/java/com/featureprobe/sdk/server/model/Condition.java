package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class Condition {

    private static final Logger logger = Loggers.EVALUATOR;

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

        datetimeMatchers.put(PredicateType.AFTER, ((target, objects) ->
                objects.stream().map(Long::parseLong).anyMatch(o -> target >= o)));
        datetimeMatchers.put(PredicateType.BEFORE, ((target, objects) ->
                objects.stream().map(Long::parseLong).anyMatch(o -> target < o)));

        numberMatchers.put(PredicateType.EQUAL_TO, ((target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(o -> target == o)));
        numberMatchers.put(PredicateType.NOT_EQUAL_TO, ((target, objects) ->
                objects.stream().map(Double::parseDouble).noneMatch(o -> target == o)));
        numberMatchers.put(PredicateType.GREATER_THAN, ((target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(o -> target > o)));
        numberMatchers.put(PredicateType.GREATER_OR_EQUAL, ((target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(o -> target >= o)));
        numberMatchers.put(PredicateType.LESS_THAN, ((target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(o -> target < o)));
        numberMatchers.put(PredicateType.LESS_OR_EQUAL, ((target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(o -> target <= o)));

        semverMatchers.put(PredicateType.EQUAL_TO, ((target, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> target.compareTo(t) == 0)));
        semverMatchers.put(PredicateType.NOT_EQUAL_TO, ((target, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).noneMatch(t -> target.compareTo(t) == 0)));
        semverMatchers.put(PredicateType.GREATER_THAN, ((target, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> target.compareTo(t) > 0)));
        semverMatchers.put(PredicateType.GREATER_OR_EQUAL, ((target, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> target.compareTo(t) >= 0)));
        semverMatchers.put(PredicateType.LESS_THAN, ((target, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> target.compareTo(t) < 0)));
        semverMatchers.put(PredicateType.LESS_OR_EQUAL, ((target, objects) ->
                objects.stream().filter(Objects::nonNull).map(ComparableVersion::new).anyMatch(t -> target.compareTo(t) <= 0)));

    }

    public boolean matchObjects(FPUser user, Map<String, Segment> segments) {
        switch (type) {
            case STRING:
                return matchStringCondition(user);

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

    private boolean matchStringCondition(FPUser user) {
        String subjectValue = user.getAttr(subject);
        if (StringUtils.isBlank(subjectValue)) {
            return false;
        }

        StringMatcher stringMatcher = stringMatchers.get(this.predicate);
        if (Objects.isNull(stringMatcher)) {
            return false;
        }

        try {
            return stringMatcher.match(subjectValue, this.objects);
        } catch (PatternSyntaxException e) {
            logger.error("Invalid regex pattern", e);
            return false;
        }
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

        String customValue = user.getAttr(this.subject);
        long cv;
        try {
            cv = StringUtils.isBlank(customValue)
                    ? System.currentTimeMillis() / MILLISECONDS_IN_ONE_SEC
                    : Long.parseLong(customValue);
        } catch (NumberFormatException e) {
            logger.error("User attribute type mismatch. attribute value: {}, target type long", customValue);
            return false;
        }
        try {
            return datetimeMatcher.match(cv, objects);
        } catch (NumberFormatException e) {
            logger.error("Met a string that cannot be parsed to long in Condition.objects: {}", e.getMessage());
            return false;
        }
    }

    private boolean matchNumberCondition(FPUser user) {
        NumberMatcher numberMatcher = numberMatchers.get(this.predicate);
        if (Objects.isNull(numberMatcher)) {
            return false;
        }

        String customValue = user.getAttr(this.subject);
        if (StringUtils.isBlank(customValue)) {
            return false;
        }
        double cv;
        try {
            cv = Double.parseDouble(customValue);
        } catch (NumberFormatException e) {
            logger.error("User attribute type mismatch. attribute value : {}, target type double", customValue);
            return false;
        }
        try {
            return numberMatcher.match(cv, this.objects);
        } catch (NumberFormatException e) {
            logger.error("Met a string that cannot be parsed to double in Condition.objects: {}", e.getMessage());
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
