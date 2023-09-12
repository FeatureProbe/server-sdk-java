package com.featureprobe.sdk.server

import com.featureprobe.sdk.server.model.Serve
import com.featureprobe.sdk.server.model.Toggle
import spock.lang.Specification

class ToggleTest extends Specification {
    def toggle = new Toggle()
    def user = new FPUser();

    def setup() {
        toggle.setEnabled(Boolean.TRUE);
        toggle.setVariations([0,1])
        toggle.setDisabledServe(new Serve(0))
        toggle.setDefaultServe(new Serve(1))
    }

    def "If toggle is disabled serve disabled variation"() {
        given:
        toggle.setEnabled(Boolean.FALSE)

        when:
        def result = toggle.doEval(user, null, null, null, 1)

        then:
        0 == result.variationIndex.get()
    }

    def "If toggle is enabled serve default variation"() {
        given:
        toggle.setEnabled(Boolean.TRUE)

        when:
        def result = toggle.doEval(user, null, null, null, 1)

        then:
        1 == result.variationIndex.get()
    }

    def "When meetPrerequisite returns false should act like disabled"() {
        setup:
        Toggle toggleSpy = Spy()
        toggleSpy.enabled = true

        final DEFAULT_VARIATION = 1
        final DISABLED_VARIATION = 0

        toggleSpy.variations = [DEFAULT_VARIATION, DISABLED_VARIATION]
        toggleSpy.defaultServe = new Serve(DEFAULT_VARIATION)
        toggleSpy.disabledServe = new Serve(DISABLED_VARIATION)

        // Make meetPrerequisite return false
        toggleSpy.meetPrerequisite(user, null, null, 1) >> false

        when:
        def result = toggleSpy.doEval(user, null, null, null, 1)

        then:
        DISABLED_VARIATION == result.variationIndex.get()
    }

    def "When meetPrerequisite returns true should not act like disabled"() {
        setup:
        Toggle toggleSpy = Spy()
        toggleSpy.enabled = true
        final DEFAULT_VARIATION = 1
        final DISABLED_VARIATION = 0

        toggleSpy.variations = [DEFAULT_VARIATION, DISABLED_VARIATION]
        toggleSpy.defaultServe = new Serve(DEFAULT_VARIATION)
        toggleSpy.disabledServe = new Serve(DISABLED_VARIATION)

        // Make meetPrerequisite return false
        toggleSpy.meetPrerequisite(user, null, null, 1) >> true

        when:
        def result = toggleSpy.doEval(user, null, null, null, 1)

        then:
        DISABLED_VARIATION != result.variationIndex.get()
    }

}
