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

    private String predicate;

    private List<String> objects;

    private static final Map<String, Matcher> matchers = new HashMap<>(10);

    static {
        matchers.put("is one of", (target, objects) ->
                objects.contains(target));
        matchers.put("ends with", (target, objects) ->
                objects.stream().filter(s -> target.endsWith(s)).findAny().isPresent());
        matchers.put("starts with", (target, objects) ->
                objects.stream().filter(s -> target.startsWith(s)).findAny().isPresent());
        matchers.put("contains", (target, objects) ->
                objects.stream().filter(s -> target.indexOf(s) != -1).findAny().isPresent());
        matchers.put("matches regex", (target, objects) ->
                objects.stream().filter(s -> Pattern.compile(s).matcher(target).find()).findAny().isPresent());
        matchers.put("is not any of", (target, objects) ->
                !objects.contains(target));
        matchers.put("does not end with", (target, objects) ->
                !objects.stream().filter(s -> target.endsWith(s)).findAny().isPresent());
        matchers.put("does not start with", (target, objects) ->
                !objects.stream().filter(s -> target.startsWith(s)).findAny().isPresent());
        matchers.put("does not contain", (target, objects) ->
                !objects.stream().filter(s -> target.indexOf(s) != -1).findAny().isPresent());
        matchers.put("does not match regex", (target, objects) ->
                !objects.stream().filter(s -> Pattern.compile(s).matcher(target).find()).findAny().isPresent());
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
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

}
