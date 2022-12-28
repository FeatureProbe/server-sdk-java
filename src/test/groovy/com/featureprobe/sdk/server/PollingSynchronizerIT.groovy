package com.featureprobe.sdk.server

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class PollingSynchronizerIT extends Specification {

    def "Socketio realtime toggle update"() {

        (LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger).setLevel(Level.DEBUG)

        given:
        def config = FPConfig.builder()
                .pollingMode()
                .remoteUri("https://featureprobe.io/server")
                .realtimeUri("https://featureprobe.io/server/realtime")
                .useMemoryRepository()
                .build()
        def featureProbe = new FeatureProbe("server-61db54ecea79824cae3ac38d73f1961d698d0477", config)
        def repository = featureProbe.dataRepository
        def socket = (featureProbe.synchronizer as PollingSynchronizer).socket
        def updateCnt = 0
        socket.on("update", objects -> updateCnt++)

        sleep(5000)

        featureProbe.close()

        sleep(5000)

        expect:
        repository.initialized()
        !socket.connected()
        updateCnt > 0
    }
}

