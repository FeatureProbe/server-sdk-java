package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.DatetimeMatcher;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.Loggers;
import com.featureprobe.sdk.server.NumberMatcher;
import com.featureprobe.sdk.server.SemVerMatcher;
import com.featureprobe.sdk.server.StringMatcher;
import com.featureprobe.sdk.server.SegmentMatcher;
import com.featureprobe.sdk.server.Version;
import com.featureprobe.sdk.server.exceptions.VersionFormatException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Condition {

    private static final Logger logger = Loggers.EVALUATOR;

    private ConditionType type;

    private String subject;

    private PredicateType predicate;

    private List<String> objects;

    private static final Map<PredicateType, StringMatcher> stringMatchers =
            new HashMap<>(PredicateType.values().length);

    private static final Map<PredicateType, SegmentMatcher> segmentMatchers =
            new HashMap<>(PredicateType.values().length);

    private static final Map<PredicateType, NumberMatcher> numberMatchers =
            new HashMap<>(PredicateType.values().length);

    private static final Map<PredicateType, DatetimeMatcher> datetimeMatchers =
            new HashMap<>(PredicateType.values().length);

    private static final Map<PredicateType, SemVerMatcher> semVerMatchers =
            new HashMap<>(PredicateType.values().length);

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

        numberMatchers.put(PredicateType.EQUAL_TO, (target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(s -> target == s));
        numberMatchers.put(PredicateType.NOT_EQUAL_TO, (target, objects) ->
                objects.stream().map(Double::parseDouble).noneMatch(s -> target == s));
        numberMatchers.put(PredicateType.LESS_THAN, (target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(s -> target < s));
        numberMatchers.put(PredicateType.LESS_THAN_OR_EQUAL_TO, (target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(s -> target <= s));
        numberMatchers.put(PredicateType.GREATER_THAN, (target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(s -> target > s));
        numberMatchers.put(PredicateType.GREATER_THAN_OR_EQUAL_TO, (target, objects) ->
                objects.stream().map(Double::parseDouble).anyMatch(s -> target >= s));

        datetimeMatchers.put(PredicateType.BEFORE, (target, objects) ->
                objects.stream().map(Long::parseLong).anyMatch(s -> target < s));
        datetimeMatchers.put(PredicateType.AFTER, (target, objects) ->
                objects.stream().map(Long::parseLong).anyMatch(s -> target >= s));

        semVerMatchers.put(PredicateType.EQUAL_TO, (target, objects) ->
                objects.stream().map(Version::parseVersion).anyMatch(s -> target.equals(s)));
        semVerMatchers.put(PredicateType.NOT_EQUAL_TO, (target, objects) ->
                objects.stream().map(Version::parseVersion).noneMatch(s -> target.equals(s)));
        semVerMatchers.put(PredicateType.LESS_THAN, (target, objects) ->
                objects.stream().map(Version::parseVersion).anyMatch(s -> target.lessThan(s)));
        semVerMatchers.put(PredicateType.LESS_THAN_OR_EQUAL_TO, (target, objects) ->
                objects.stream().map(Version::parseVersion).anyMatch(s -> target.lessThanOrEqual(s)));
        semVerMatchers.put(PredicateType.GREATER_THAN, (target, objects) ->
                objects.stream().map(Version::parseVersion).anyMatch(s -> target.greaterThan(s)));
        semVerMatchers.put(PredicateType.GREATER_THAN_OR_EQUAL_TO, (target, objects) ->
                objects.stream().map(Version::parseVersion).anyMatch(s -> target.greaterThanOrEqual(s)));
    }

    public boolean matchObjects(FPUser user, Map<String, Segment> segments) {
        switch (type) {
            case STRING:
                return matchStringCondition(user);
            case SEGMENT:
                return matchSegmentCondition(user, segments);
            case NUMBER:
                return matchNumberCondition(user);
            case DATETIME:
                return matchDatetimeCondition(user);
            case SEM_VER:
                return matchSemVerCondition(user);
            default:
                return false;
        }
    }

    private boolean matchStringCondition(FPUser user) {
        String subjectValue = user.getAttrs().get(subject);
        if (StringUtils.isBlank(subjectValue)) {
            return false;
        }
        StringMatcher stringMatcher = stringMatchers.get(this.predicate);
        if (Objects.nonNull(stringMatcher)) {
            return stringMatcher.match(subjectValue, this.objects);
        }
        return false;
    }

    private boolean matchSegmentCondition(FPUser user, Map<String, Segment> segments) {
        SegmentMatcher segmentMatcher = segmentMatchers.get(this.predicate);
        if (Objects.nonNull(segmentMatcher)) {
            return segmentMatcher.match(user, segments, this.objects);
        }
        return false;
    }

    private boolean matchNumberCondition(FPUser user) {
        NumberMatcher numberMatcher = numberMatchers.get(this.predicate);
        if (Objects.isNull(numberMatcher)) {
            return false;
        }
        String subjectValue = user.getAttrs().get(subject);
        if (StringUtils.isBlank(subjectValue)) {
            return false;
        }
        try {
            double target = Double.parseDouble(subjectValue);
            return numberMatcher.match(target, this.objects);
        } catch (NumberFormatException e) {
            logger.error("User attribute type mismatch. attribute value : {}, target type double", subjectValue);
            return false;
        }
    }

    private boolean matchDatetimeCondition(FPUser user) {
        DatetimeMatcher datetimeMatcher = datetimeMatchers.get(this.predicate);
        if (Objects.isNull(datetimeMatcher)) {
            return false;
        }
        String subjectValue = user.getAttrs().get(subject);
        try {
            long target = StringUtils.isBlank(subjectValue) ? System.currentTimeMillis() : Long.parseLong(subjectValue);
            return datetimeMatcher.match(target, this.objects);
        } catch (NumberFormatException e) {
            logger.error("User attribute type mismatch. attribute value : {}, target type long", subjectValue);
            return false;
        }
    }

    private boolean matchSemVerCondition(FPUser user) {
        SemVerMatcher semVerMatcher = semVerMatchers.get(this.predicate);
        if (Objects.isNull(semVerMatcher)) {
            return false;
        }
        String subjectValue = user.getAttrs().get(subject);
        if (StringUtils.isBlank(subjectValue)) {
            return false;
        }
        try {
            Version target = Version.parseVersion(subjectValue);
            return semVerMatcher.match(target, this.objects);
        } catch (VersionFormatException e) {
            logger.error("User attribute type mismatch. attribute value : {}, target type SemanticVersion",
                    subjectValue);
            return false;
        }
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
