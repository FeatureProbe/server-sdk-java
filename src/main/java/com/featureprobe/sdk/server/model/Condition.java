package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.StringMatcher;
import com.featureprobe.sdk.server.SegmentMatcher;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Condition {

    private ConditionType type;

    private String subject;

    private PredicateType predicate;

    private List<String> objects;

    private static final Map<PredicateType, StringMatcher> stringMatchers =
            new HashMap<>(PredicateType.values().length);

    private static final Map<PredicateType, SegmentMatcher> segmentMatchers =
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

    }

    public boolean matchObjects(FPUser user, Map<String, Segment> segments) {
        switch (type) {
            case STRING:
                String subjectValue = user.getAttrs().get(subject);
                if (StringUtils.isBlank(subjectValue)) {
                    return false;
                }
                return matchStringCondition(subjectValue);
            case SEGMENT:
                return matchSegmentCondition(user, segments);
            case DATE:
                // TODO
            default:
                return false;
        }
    }

    private boolean matchStringCondition(String subjectValue) {
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
