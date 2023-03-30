package com.featureprobe.sdk.server

import com.featureprobe.sdk.server.model.Split
import spock.lang.Specification

class SplitSpec extends Specification {

    def Split split
    def FPUser user

    def setup() {
        split = new Split([[[0, 5000]], [[5000, 10000]]])
        user = new FPUser().stableRollout("test_user_key")
    }

    def "Get user group"() {
        when:
        def commonIndex = split.findIndex(user, "test_toggle_key")
        split.setBucketBy("email")
        split.setSalt("abcddeafasde")
        user.with("email", "test@gmail.com")
        def customIndex = split.findIndex(user, "test_toggle_key")
        then:
        with(commonIndex) {
            0 == commonIndex.index.get()
            1 == customIndex.index.get()
        }
    }

    def "Get hash key" () {
        when:
        def hash = split.hash("13", "tutorial_rollout", 10000)
        then:
        hash == 9558
    }

    def "User not has key"() {
        when:
        user = new FPUser()
        def result1 = split.findIndex(user, "test_toggle_key")
        def key1 = user.key
        def result2 = split.findIndex(user, "test_toggle_key")
        def key2 = user.key
        then:
        key1 == key2
        result1.index.get() == result2.index.get()
    }

}
