package com.featureprobe.sdk.server

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.featureprobe.sdk.server.model.Repository
import com.google.common.io.ByteStreams
import spock.lang.Specification

import java.nio.charset.Charset

class RepositorySerializationSpec extends Specification {

    def test_data_local = "datasource/repo.json"
    def ObjectMapper mapper

    def setup() {
        mapper = new ObjectMapper()
    }

    def "Serialization Toggles to repository"() {
        when:

        InputStream is = getClass().getClassLoader().getResourceAsStream(test_data_local)
        String data = new String(ByteStreams.toByteArray(is), Charset.forName("UTF-8"))
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        Repository repository = mapper.readValue(data, Repository.class)
        def repo = mapper.writeValueAsString(repository)
        then:
        with(repository) {
            1 <= repository.getToggles().size()
        }
    }



}