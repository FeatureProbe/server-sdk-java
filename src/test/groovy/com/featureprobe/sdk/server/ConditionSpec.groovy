package com.featureprobe.sdk.server

import com.featureprobe.sdk.server.model.*
import spock.lang.Specification

import java.time.Instant

class ConditionSpec extends Specification {

    def Condition condition
    def FPUser user
    def Map<String, Segment> segments


    def setup() {
        condition = new Condition()
        condition.setType(ConditionType.STRING)
        condition.setSubject("userId")
        user = new FPUser().stableRollout("test_user")
        segments = ["test_project\$test_segment": new Segment(uniqueId: "test_project\$test_segment", version: 1,
                rules: [new SegmentRule(conditions: [new Condition(type: ConditionType.STRING, subject: "userId",
                        predicate: PredicateType.IS_ONE_OF, objects: ["1", "2"])])])]
    }

    def "[is one of] string condition match"() {
        when:
        condition.setObjects(["12345", "987654", "665544", "13797347245"])
        condition.setPredicate(PredicateType.IS_ONE_OF)
        user.with("userId", "12345")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "999999")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", "\t \n  ")
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[ends with] string condition match"() {
        when:
        condition.setObjects(["123", "888"])
        condition.setPredicate(PredicateType.ENDS_WITH)
        user.with("userId", "123123")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "999999")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", null)
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[starts with] string condition match"() {
        when:
        condition.setObjects(["123"])
        condition.setPredicate(PredicateType.STARTS_WITH)
        user.with("userId", "123321")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "3333")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[contains] string condition match"() {
        when:
        condition.setObjects(["123", "456"])
        condition.setPredicate(PredicateType.CONTAINS)
        user.with("userId", "456433")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "999999")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[matches regex] string condition match"() {
        when:
        condition.setObjects(["0?(13|14|15|18)[0-9]{9}"])
        condition.setPredicate(PredicateType.MATCHES_REGEX)
        user.with("userId", "13797347245")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "122122")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[matches invalid regex] string condition match"() {
        when:
        condition.setObjects(["\\\\\\"])
        condition.setPredicate(PredicateType.MATCHES_REGEX)
        user.with("userId", "13797347245")
        def hitMiss = condition.matchObjects(user, segments)
        user.with("userId", "122122")
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        !hitMiss
        !hitMiss2
    }

    def "[is not any of] string condition match"() {
        when:
        condition.setObjects(["12345", "987654", "665544"])
        condition.setPredicate(PredicateType.IS_NOT_ANY_OF)
        user.with("userId", "999999999")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "12345")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[does not end with] string condition match"() {
        when:
        condition.setObjects(["123", "456"])
        condition.setPredicate(PredicateType.DOES_NOT_END_WITH)
        user.with("userId", "3333333")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "456456")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[does not start with] string condition match"() {
        when:
        condition.setObjects(["123", "456"])
        condition.setPredicate(PredicateType.DOES_NOT_START_WITH)
        user.with("userId", "3333333")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "123456")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[does not contain] string condition match"() {
        when:
        condition.setObjects(["12345", "987654", "665544"])
        condition.setPredicate(PredicateType.DOES_NOT_CONTAIN)
        user.with("userId", "999999999")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "12345")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[does not match regex] string condition match"() {
        when:
        condition.setObjects(["0?(13|14|15|18)[0-9]{9}"])
        condition.setPredicate(PredicateType.DOES_NOT_MATCH_REGEX)
        user.with("userId", "2122121")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "13797347245")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[is in] segment condition match"() {
        when:
        condition.setType(ConditionType.SEGMENT)
        condition.setObjects(["test_project\$test_segment"])
        condition.setPredicate(PredicateType.IS_IN)
        user.with("userId", "1")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "3")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[is not in] segment condition match"() {
        when:
        condition.setType(ConditionType.SEGMENT)
        condition.setObjects(["test_project\$test_segment"])
        condition.setPredicate(PredicateType.IS_NOT_IN)
        user.with("userId", "3")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "1")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[after] datetime condition match"() {
        when:
        condition.setType(ConditionType.DATETIME)
        condition.setObjects([Instant.now().getEpochSecond().toString()])
        condition.setPredicate(PredicateType.AFTER)
        user.with("userId", (Instant.now().getEpochSecond()).toString())
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", (Instant.now().getEpochSecond() + 1).toString())
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", null)
        def hitSuccess3 = condition.matchObjects(user, segments)
        user.with("userId", "1000")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        hitSuccess3
        !hitMiss
    }

    def "[before] datetime condition match"() {
        when:
        condition.setType(ConditionType.DATETIME)
        condition.setObjects([Instant.now().getEpochSecond().toString()])
        condition.setPredicate(PredicateType.BEFORE)
        user.with("userId", (Instant.now().getEpochSecond() - 2).toString())
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", (Instant.now().getEpochSecond() + 1).toString())
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", "invalid date")
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[=] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["12", "10.1"])
        condition.setPredicate(PredicateType.EQUAL_TO)
        user.with("userId", "  12.000000 \n")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", "  10.10 \n")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", "foo.bar+1")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", " ")
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        !hitMiss1
        !hitMiss2
    }

    def "[!=] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["12", "16"])
        condition.setPredicate(PredicateType.NOT_EQUAL_TO)
        user.with("userId", "  13 \n")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "\t16.0 ")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", "foo")
        def hitMiss2 = condition.matchObjects(user, segments)

        condition.setObjects(["foo", "16"])
        user.with("userId", "1")
        def hitMiss3 = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
        !hitMiss3
    }

    def "[>] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["12"])
        condition.setPredicate(PredicateType.GREATER_THAN)
        user.with("userId", "  13 \n")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "\t11.998 ")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", "\t12.0 ")
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[>=] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["12"])
        condition.setPredicate(PredicateType.GREATER_OR_EQUAL)
        user.with("userId", "  13 \n")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", "\t12.0 ")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", "\t11.919999998 ")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        !hitMiss
    }

    def "[<] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["17"])
        condition.setPredicate(PredicateType.LESS_THAN)
        user.with("userId", "  13 \n")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "\t18")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", "\t17.00000000000001 ")  // parsed as 17
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[<=] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["17"])
        condition.setPredicate(PredicateType.LESS_OR_EQUAL)
        user.with("userId", "  13 \n")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", "17")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", "\t18")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        !hitMiss
    }

    def "[=] semver condition match"() {
        when:
        condition.setType(ConditionType.SEMVER)
        condition.setObjects(["1.1.3", "1.1.5"])
        condition.setPredicate(PredicateType.EQUAL_TO)
        user.with("userId", "1.1.3")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", "1.1.5")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", "1.0.1")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", "")
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        !hitMiss1
        !hitMiss2
    }

    def "[!=] semver condition match"() {
        when:
        condition.setType(ConditionType.SEMVER)
        condition.setObjects(["1.1.0", "1.2.0"])
        condition.setPredicate(PredicateType.NOT_EQUAL_TO)
        user.with("userId", "1.3.0")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "1.1.0")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("userId", "1.2.0")
        def hitMiss2 = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[>] semver condition match"() {
        when:
        condition.setType(ConditionType.SEMVER)
        condition.setObjects(["1.1.0", "1.2.0"])
        condition.setPredicate(PredicateType.GREATER_THAN)
        user.with("userId", "1.1.1")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "1.0.0")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess
        !hitMiss
    }

    def "[>=] semver condition match"() {
        when:
        condition.setType(ConditionType.SEMVER)
        condition.setObjects(["1.1.0", "1.2.0"])
        condition.setPredicate(PredicateType.GREATER_OR_EQUAL)
        user.with("userId", "1.1.1")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", "1.1.0")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", "1.0.0")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        !hitMiss
    }

    def "[<] semver condition match"() {
        when:
        condition.setType(ConditionType.SEMVER)
        condition.setObjects(["1.1.0", "1.2.0"])
        condition.setPredicate(PredicateType.LESS_THAN)
        user.with("userId", "1.0.1")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", "1.1.7")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", "1.2.0")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        !hitMiss
    }

    def "[<=] semver condition match"() {
        when:
        condition.setType(ConditionType.SEMVER)
        condition.setObjects(["1.1.0", "1.2.0"])
        condition.setPredicate(PredicateType.LESS_OR_EQUAL)
        user.with("userId", "1.0.1")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("userId", "1.2.0")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("userId", "1.2.1")
        def hitMiss = condition.matchObjects(user, segments)

        then:
        hitSuccess1
        hitSuccess2
        !hitMiss
    }

}
