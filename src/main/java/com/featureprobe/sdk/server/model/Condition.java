package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.Matcher;
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

    private static final Map<PredicateType, Matcher> matchers = new HashMap<>(PredicateType.values().length);


    static {
        matchers.put(PredicateType.IS_ONE_OF, (target, objects) ->
                objects.contains(target));
        matchers.put(PredicateType.ENDS_WITH, (target, objects) ->
                objects.stream().anyMatch(target::endsWith));
        matchers.put(PredicateType.STARTS_WITH, (target, objects) ->
                objects.stream().anyMatch(target::startsWith));
        matchers.put(PredicateType.CONTAINS, (target, objects) ->
                objects.stream().anyMatch(target::contains));
        matchers.put(PredicateType.MATCHES_REGEX, (target, objects) ->
                objects.stream().anyMatch(s -> Pattern.compile(s).matcher(target).find()));
        matchers.put(PredicateType.IS_NOT_ANY_OF, (target, objects) ->
                !objects.contains(target));
        matchers.put(PredicateType.DOES_NOT_END_WITH, (target, objects) ->
                objects.stream().noneMatch(target::endsWith));
        matchers.put(PredicateType.DOES_NOT_START_WITH, (target, objects) ->
                objects.stream().noneMatch(target::startsWith));
        matchers.put(PredicateType.DOES_NOT_CONTAIN, (target, objects) ->
                objects.stream().noneMatch(target::contains));
        matchers.put(PredicateType.DOES_NOT_MATCH_REGEX, (target, objects) ->
                objects.stream().noneMatch(s -> Pattern.compile(s).matcher(target).find()));
    }

    public boolean matchObjects(FPUser user) {
        String subjectValue = user.getAttrs().get(subject);
        if (StringUtils.isBlank(subjectValue)) {
            return false;
        }
        switch (type) {
            case STRING:
                return matchStringCondition(subjectValue);
            case SEGMENT:
                // TODO
            case DATE:
                // TODO
            default:
                return false;
        }
    }

    private boolean matchStringCondition(String subjectValue) {
        Matcher matcher = matchers.get(this.predicate);
        if (Objects.nonNull(matcher)) {
            return matcher.match(subjectValue, this.objects);
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
