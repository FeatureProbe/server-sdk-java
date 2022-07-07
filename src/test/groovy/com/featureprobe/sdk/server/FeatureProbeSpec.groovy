package com.featureprobe.sdk.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.featureprobe.sdk.server.model.Repository
import com.google.common.io.ByteStreams
import spock.lang.Specification
import java.nio.charset.Charset

class FeatureProbeSpec extends Specification {


    def test_data_local = "test/server-sdk-specification/spec/toggle_simple_spec.json";
    def FeatureProbe featureProbe
    def ObjectMapper mapper
    def JsonNode testCase

    def setup() {
        mapper = new ObjectMapper()
        InputStream is = getClass().getClassLoader().getResourceAsStream(test_data_local)
        String data = new String(ByteStreams.toByteArray(is), Charset.forName("UTF-8"))
        testCase = mapper.readTree(data)
    }

    def "FeatureProbe initialized with empty sdkKey"() {
        expect:
        try {
            featureProbe = new FeatureProbe("")
            fail("should not allow empty sdk key")
        } catch (Exception e) {
            assert e instanceof IllegalArgumentException
        }

        try {
            featureProbe = new FeatureProbe(" \n\t  ")
            fail("should not allow empty sdk key")
        } catch (Exception e) {
            assert e instanceof IllegalArgumentException
        }

        try {
            featureProbe = new FeatureProbe("foo")
        } catch (Exception ignored) {
            fail("should not fail with not empty sdk key")
        }
    }


    def "FeatureProbe case test"() {
        when:
        def tests = testCase.get("tests").asList()
        for (int i = 0; i < tests.size(); i++) {
            def scenario = tests.get(i)
            def name = scenario.get("scenario").asText()
            def fixture = scenario.get("fixture")
            def dataRepository = new MemoryDataRepository()
            def repository = mapper.readValue(fixture.toPrettyString(), Repository.class)
            dataRepository.refresh(repository)
            featureProbe = new FeatureProbe(dataRepository);
            def cases = scenario.get("cases")
            for (int j = 0; j < cases.size(); j++) {
                def testCase = cases.get(j)
                def caseName = testCase.get("name").asText()
                println("starting execute scenario : " + name + ",case : " + caseName)
                def userCase = testCase.get("user")
                FPUser user = new FPUser(userCase.get("key").asText())
                def customValues = userCase.get("customValues").asList()
                for (int x = 0; x < customValues.size(); x++) {
                    def customValue = customValues.get(x)
                    user.with(customValue.get("key").asText(), customValue.get("value").asText())
                }
                def functionCase = testCase.get("function")
                def functionName = functionCase.get("name").asText()
                def toggleKey = functionCase.get("toggle").asText()
                def expectResult = testCase.get("expectResult")
                def defaultValue = functionCase.get("default")
                def expectValue = expectResult.get("value")
                switch (functionName) {
                    case "bool_value":
                        def boolRes = featureProbe.boolValue(toggleKey, user, defaultValue.asBoolean())
                        assert boolRes == expectValue.asBoolean()
                        break
                    case "string_value":
                        def stringRes = featureProbe.stringValue(toggleKey, user, defaultValue.asText())
                        assert stringRes == expectValue.asText()
                        break
                    case "number_value":
                        def numberRes = featureProbe.numberValue(toggleKey, user, defaultValue.asDouble())
                        assert numberRes == expectValue.asDouble()
                        break
                    case "json_value":
                        def jsonDefaultMap = mapper.readValue(defaultValue.toPrettyString(), Map.class)
                        def jsonRres = featureProbe.jsonValue(toggleKey, user, jsonDefaultMap, Map.class)
                        def jsonExpectString = mapper.writeValueAsString(expectValue);
                        def jsonResString = mapper.writeValueAsString(jsonRres)
                        assert jsonExpectString == jsonResString
                        break
                    case "bool_detail":
                        def boolDetailRes = featureProbe.boolDetail(toggleKey, user,
                                defaultValue.asBoolean())
                        def detailStr = boolDetailRes.toString()
                        assert boolDetailRes.value == expectValue.asBoolean()
                        break
                    case "number_detail":
                        def numberDetailRes = featureProbe.numberDetail(toggleKey, user,
                                defaultValue.asDouble())
                        assert numberDetailRes.value == expectValue.asDouble()
                        break
                    case "json_detail":
                        def jsonDetailDefaultMap = mapper.readValue(defaultValue.toPrettyString(), Map.class)
                        def jsonDetailRes = featureProbe.jsonDetail(toggleKey, user,
                                jsonDetailDefaultMap, Map.class)
                        def jsonExpectString = mapper.writeValueAsString(expectValue)
                        def jsonResString = mapper.writeValueAsString(jsonDetailRes.value)
                        assert jsonExpectString == jsonResString
                        break
                    case "string_detail":
                        def stringDetailRes = featureProbe.stringDetail(toggleKey, user,
                                defaultValue.asText())
                        assert stringDetailRes.value == expectValue.asText()
                        break
                }
            }
        }
        then:
        with(FeatureProbe) {

        }
    }


    def "FeatureProbe repository uninitialized"() {

    }

}

