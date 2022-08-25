package com.featureprobe.sdk.server;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;


/**
 * A collection of attributes that can affect toggle evaluation, usually corresponding to a user of your application.
 */
public class FPUser {

    private String key;

    private Map<String, String> attrs = new HashMap<>();

    /**
     * Creates a new FPUser
     */
    public FPUser() {}

    /**
     * Creates a new FPUser
     * @param key user unique id for percentage rollout
     */
    @Deprecated
    public FPUser(String key) {
        this.key = key;
    }

    /**
     * Set user unique id for percentage rollout
     * @param key user unique id for percentage rollout
     */
    public FPUser stableRollout(String key) {
        this.key = key;
        return this;
    }

    /**
     * Add an attribute to the user
     * @param name attribute name
     * @param value attribute value
     * @return the FPUser
     */
    public FPUser with(String name, String value) {
        attrs.put(name, value);
        return this;
    }

    /**
     * Check attribute exists
     * @param name attribute name
     * @return
     */
    public boolean containAttr(String name) {
        return attrs.containsKey(name);
    }

    /**
     * Get FPUser unique identifier
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Get FPUser all attribute
     * @return attribute set
     */
    public Map<String, String> getAttrs() {
        return attrs;
    }

    /**
     * Add multiple attribute to the user
     * @param attrs attribute set
     */
    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    /**
     * Get the specified attribute value
     * @param name attribute name
     * @return attribute value
     */
    public String getAttr(String name) {
        return attrs.get(name);
    }

}
