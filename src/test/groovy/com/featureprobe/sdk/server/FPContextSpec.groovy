package com.featureprobe.sdk.server

import spock.lang.Specification

class FPContextSpec extends Specification {

    def fpContextInstance = new FPContext("", new FPConfig(new FPConfig.Builder()))

    def 'test getVersion when properties file exists'() {
        given:
        Properties properties = new Properties()
        String expectedVersion = '1.0.0'
        properties.setProperty('version', expectedVersion)
        InputStream inputStream = new ByteArrayInputStream(propertiesToString(properties).bytes)
        FPContext spyFPContextInstance = Spy(fpContextInstance)
        spyFPContextInstance.getResourceAsStream(_) >> inputStream

        when:
        String actualVersion = spyFPContextInstance.getVersion()

        then:
        actualVersion == expectedVersion
    }

    def 'test getVersion when properties file not exists but package version exists'() {
        given:
        Package mockedPackage = Mock(Package)
        mockedPackage.getImplementationVersion() >> '1.0.1'
        mockedPackage.getSpecificationVersion() >> '1.0.2'
        FPContext spyFPContextInstance = Spy(fpContextInstance)
        spyFPContextInstance.getResourceAsStream(_) >> null
        spyFPContextInstance.getaPackage() >> mockedPackage

        when:
        String actualVersion = spyFPContextInstance.getVersion()

        then:
        actualVersion == '1.0.1'
    }

    def 'test getVersion when properties file not exists and only specification version exists'() {
        given:
        Package mockedPackage = Mock(Package)
        mockedPackage.getImplementationVersion() >> null
        mockedPackage.getSpecificationVersion() >> '1.0.2'
        FPContext spyFPContextInstance = Spy(fpContextInstance)
        spyFPContextInstance.getResourceAsStream(_) >> null
        spyFPContextInstance.getaPackage() >> mockedPackage

        when:
        String actualVersion = spyFPContextInstance.getVersion()

        then:
        actualVersion == '1.0.2'
    }

    def 'test getVersion when properties file not exists and package version not exists'() {
        given:
        FPContext spyFPContextInstance = Spy(fpContextInstance)
        spyFPContextInstance.getResourceAsStream(_) >> null
        spyFPContextInstance.getaPackage() >> null

        when:
        String actualVersion = spyFPContextInstance.getVersion()

        then:
        actualVersion == "unknown"
    }

    private String propertiesToString(Properties properties) {
        StringBuilder sb = new StringBuilder()
        properties.each { key, value ->
            sb.append("${key}=${value}\n")
        }
        return sb.toString()
    }
}
