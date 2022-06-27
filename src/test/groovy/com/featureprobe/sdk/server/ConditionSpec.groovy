package com.featureprobe.sdk.server

import com.featureprobe.sdk.server.model.Condition
import com.featureprobe.sdk.server.model.ConditionType
import com.featureprobe.sdk.server.model.PredicateType
import com.featureprobe.sdk.server.model.Segment
import com.featureprobe.sdk.server.model.SegmentRule
import spock.lang.Specification

class ConditionSpec extends Specification {

    def Condition condition
    def FPUser user
    def Map<String, Segment> segments


    def setup() {
        condition = new Condition()
        condition.setType(ConditionType.STRING)
        condition.setSubject("userId")
        user = new FPUser("test_user")
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
        def hitMiss = condition.matchObjects(user, segments)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[ends with] sting condition match"() {
        when:
        condition.setObjects(["123", "888"])
        condition.setPredicate(PredicateType.ENDS_WITH)
        user.with("userId", "123123")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("userId", "999999")
        def hitMiss = condition.matchObjects(user, segments)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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
        with() {
            hitSuccess
            !hitMiss
        }
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

    def "[Is not in] segment condition match"() {
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

    def "[equal to] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["10", "20"])
        condition.setSubject("age")
        condition.setPredicate(PredicateType.EQUAL_TO)
        user.with("age", "10")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("age", "11")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("age", "abc")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[not equal to] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["10", "20"])
        condition.setSubject("age")
        condition.setPredicate(PredicateType.NOT_EQUAL_TO)
        user.with("age", "11")
        def hitSuccess = condition.matchObjects(user, segments)
        user.with("age", "20")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("age", "abc")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess
        !hitMiss1
        !hitMiss2
    }

    def "[less than] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["10", "20"])
        condition.setSubject("age")
        condition.setPredicate(PredicateType.LESS_THAN)
        user.with("age", "9")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("age", "15")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("age", "20")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("age", "25")
        def hitMiss2 = condition.matchObjects(user, segments)
        user.with("age", "abc")
        def hitMiss3 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        !hitMiss1
        !hitMiss2
        !hitMiss3
    }

    def "[less than or equal to] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["10", "20"])
        condition.setSubject("age")
        condition.setPredicate(PredicateType.LESS_THAN_OR_EQUAL_TO)
        user.with("age", "9")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("age", "10")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("age", "15")
        def hitSuccess3 = condition.matchObjects(user, segments)
        user.with("age", "25")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("age", "abc")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        hitSuccess3
        !hitMiss1
        !hitMiss2
    }

    def "[greater than] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["10", "20"])
        condition.setSubject("age")
        condition.setPredicate(PredicateType.GREATER_THAN)
        user.with("age", "15")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("age", "25")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("age", "10")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("age", "9")
        def hitMiss2 = condition.matchObjects(user, segments)
        user.with("age", "abc")
        def hitMiss3 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        !hitMiss1
        !hitMiss2
        !hitMiss3
    }

    def "[greater than or equal to] number condition match"() {
        when:
        condition.setType(ConditionType.NUMBER)
        condition.setObjects(["10", "20"])
        condition.setSubject("age")
        condition.setPredicate(PredicateType.GREATER_THAN_OR_EQUAL_TO)
        user.with("age", "15")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("age", "20")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("age", "25")
        def hitSuccess3 = condition.matchObjects(user, segments)
        user.with("age", "9")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("age", "abc")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        hitSuccess3
        !hitMiss1
        !hitMiss2
    }

    def "[before] datetime condition match"() {
        when:
        condition.setType(ConditionType.DATETIME)
        condition.setObjects(["1656217309", "1656303709"])
        condition.setSubject("loginTime")
        condition.setPredicate(PredicateType.BEFORE)
        user.with("loginTime", "1656130909")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("loginTime", "1656217309")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("loginTime", "1656303709")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("loginTime", "1656498109")
        def hitMiss2 = condition.matchObjects(user, segments)
        user.with("loginTime", "")
        def hitMiss3 = condition.matchObjects(user, segments)
        user.with("loginTime", "abc")
        def hitMiss4 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        !hitMiss1
        !hitMiss2
        !hitMiss3
        !hitMiss4
    }

    def "[after] datetime condition match"() {
        when:
        condition.setType(ConditionType.DATETIME)
        condition.setObjects(["1656217309", "1656303709"])
        condition.setSubject("loginTime")
        condition.setPredicate(PredicateType.AFTER)
        user.with("loginTime", "1656217309")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("loginTime", "1656257309")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("loginTime", "1656303709")
        def hitSuccess3 = condition.matchObjects(user, segments)
        user.with("loginTime", "")
        def hitSuccess4 = condition.matchObjects(user, segments)
        user.with("loginTime", "1656117309")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("loginTime", "abc")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        hitSuccess3
        hitSuccess4
        !hitMiss1
        !hitMiss2
    }

    def "[equal to] SemanticVersion condition match"() {
        when:
        condition.setType(ConditionType.SEM_VER)
        condition.setObjects(["1.1.1", "2.1.1"])
        condition.setSubject("version")
        condition.setPredicate(PredicateType.EQUAL_TO)
        user.with("version", "1.1.1")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.2")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1-RELEASE")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        !hitMiss1
        !hitMiss2
    }

    def "[not equal to] SemanticVersion condition match"() {
        when:
        condition.setType(ConditionType.SEM_VER)
        condition.setObjects(["1.1.1", "2.1.1"])
        condition.setSubject("version")
        condition.setPredicate(PredicateType.NOT_EQUAL_TO)
        user.with("version", "1.1.2")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1-RELEASE")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        !hitMiss1
        !hitMiss2
    }

    def "[less than] SemanticVersion condition match"() {
        when:
        condition.setType(ConditionType.SEM_VER)
        condition.setObjects(["1.1.1", "2.1.1"])
        condition.setSubject("version")
        condition.setPredicate(PredicateType.LESS_THAN)
        user.with("version", "1.1.0")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.2")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("version", "2.1.1")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("version", "3.1.1")
        def hitMiss2 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1-RELEASE")
        def hitMiss3 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        !hitMiss1
        !hitMiss2
        !hitMiss3
    }

    def "[less than or equal to] SemanticVersion condition match"() {
        when:
        condition.setType(ConditionType.SEM_VER)
        condition.setObjects(["1.1.1", "2.1.1"])
        condition.setSubject("version")
        condition.setPredicate(PredicateType.LESS_THAN_OR_EQUAL_TO)
        user.with("version", "1.1.0")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("version", "2.1.1")
        def hitSuccess3 = condition.matchObjects(user, segments)
        user.with("version", "3.1.1")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1-RELEASE")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        hitSuccess3
        !hitMiss1
        !hitMiss2
    }

    def "[greater than] SemanticVersion condition match"() {
        when:
        condition.setType(ConditionType.SEM_VER)
        condition.setObjects(["1.1.1", "2.1.1"])
        condition.setSubject("version")
        condition.setPredicate(PredicateType.GREATER_THAN)
        user.with("version", "1.1.2")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("version", "2.1.1")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("version", "3.3.1")
        def hitSuccess3 = condition.matchObjects(user, segments)
        user.with("version", "1.1.0")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1")
        def hitMiss2 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1-RELEASE")
        def hitMiss3 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        hitSuccess3
        !hitMiss1
        !hitMiss2
        !hitMiss3
    }

    def "[greater than or equal to] SemanticVersion condition match"() {
        when:
        condition.setType(ConditionType.SEM_VER)
        condition.setObjects(["1.1.1", "2.1.1"])
        condition.setSubject("version")
        condition.setPredicate(PredicateType.GREATER_THAN_OR_EQUAL_TO)
        user.with("version", "1.1.1")
        def hitSuccess1 = condition.matchObjects(user, segments)
        user.with("version", "2.1.1")
        def hitSuccess2 = condition.matchObjects(user, segments)
        user.with("version", "3.3.1")
        def hitSuccess3 = condition.matchObjects(user, segments)
        user.with("version", "1.1.0")
        def hitMiss1 = condition.matchObjects(user, segments)
        user.with("version", "1.1.1-RELEASE")
        def hitMiss2 = condition.matchObjects(user, segments)
        then:
        hitSuccess1
        hitSuccess2
        hitSuccess3
        !hitMiss1
        !hitMiss2
    }
}

