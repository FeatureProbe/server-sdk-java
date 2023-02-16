package com.featureprobe.sdk.server

import spock.lang.Specification

class AccessSummaryRecorderSpec extends Specification {

    def AccessSummaryRecorder accessRecorder
    def Event event

    def setup() {
        accessRecorder = new AccessSummaryRecorder()
        FPUser user = new FPUser().stableRollout("test_user")
        event = new AccessEvent(user.key, "test_toggle", "true", 1, 0, 1, "hid default rule.", true)
    }

    def "add a Event"() {
        when:
        accessRecorder.add(event)
        then:
        with(accessRecorder) {
            0 < accessRecorder.getStartTime()
            0 == accessRecorder.getEndTime()
            "true" == accessRecorder.getCounters().get("test_toggle").get(0).value
            1 == accessRecorder.getCounters().get("test_toggle").get(0).count
            1 == accessRecorder.getCounters().get("test_toggle").get(0).version
            0 == accessRecorder.getCounters().get("test_toggle").get(0).index
        }
    }

    def "get a snapshot"() {
        when:
        accessRecorder.add(event)
        def snapshot = accessRecorder.snapshot()
        then:
        with(snapshot) {
            1 == snapshot.counters.size()
            1 == snapshot.counters.get("test_toggle").size()
        }
    }

}

