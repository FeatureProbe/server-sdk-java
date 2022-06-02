package com.featureprobe.sdk.server

import spock.lang.Specification

class PollingSynchronizerSpec extends Specification{

    def FPConfig config
    def FeatureProbe featureProbe
    def repository

    def setup() {
        config = FPConfig.builder().pollingMode().useMemoryRepository().build()
    }

    def "Polling Mode Synchronizer"() {
        when:
        featureProbe = new FeatureProbe("server-61db54ecea79824cae3ac38d73f1961d698d0477", config)
        repository = featureProbe.dataRepository
        then:
        with(repository) {

        }
    }
}

