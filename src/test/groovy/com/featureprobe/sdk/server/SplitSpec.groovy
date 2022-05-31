package com.featureprobe.sdk.server

import com.featureprobe.sdk.server.model.Split
import spock.lang.Specification

class SplitSpec extends Specification {

    def Split split
    def FPUser user

    def setup() {
        split = new Split([[[0, 5000]], [[5000, 10000]]])
        user = new FPUser("test_user_key")
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


}
