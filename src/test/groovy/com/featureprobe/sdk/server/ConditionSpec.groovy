package com.featureprobe.sdk.server

import com.featureprobe.sdk.server.model.Condition
import com.featureprobe.sdk.server.model.ConditionType
import com.featureprobe.sdk.server.model.PredicateType
import spock.lang.Specification

class ConditionSpec extends Specification {

    def Condition condition
    def FPUser user

    def setup() {
        condition = new Condition()
        condition.setType(ConditionType.STRING)
        condition.setSubject("userId")
        user = new FPUser("test_user")
    }

    def "[is one of] condition match"() {
        when:
        condition.setObjects(["12345", "987654", "665544", "13797347245"])
        condition.setPredicate(PredicateType.IS_ONE_OF)
        user.with("userId", "12345")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "999999")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[ends with] condition match"() {
        when:
        condition.setObjects(["123", "888"])
        condition.setPredicate(PredicateType.ENDS_WITH)
        user.with("userId", "123123")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "999999")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[starts with] condition match"() {
        when:
        condition.setObjects(["123"])
        condition.setPredicate(PredicateType.STARTS_WITH)
        user.with("userId", "123321")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "3333")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[contains] condition match"() {
        when:
        condition.setObjects(["123", "456"])
        condition.setPredicate(PredicateType.CONTAINS)
        user.with("userId", "456433")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "999999")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[matches regex] condition match"() {
        when:
        condition.setObjects(["0?(13|14|15|18)[0-9]{9}"])
        condition.setPredicate(PredicateType.MATCHES_REGEX)
        user.with("userId", "13797347245")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "122122")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[is not any of] condition match"() {
        when:
        condition.setObjects(["12345", "987654", "665544"])
        condition.setPredicate(PredicateType.IS_NOT_ANY_OF)
        user.with("userId", "999999999")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "12345")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[does not end with] condition match"() {
        when:
        condition.setObjects(["123", "456"])
        condition.setPredicate(PredicateType.DOES_NOT_END_WITH)
        user.with("userId", "3333333")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "456456")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[does not start with] condition match"() {
        when:
        condition.setObjects(["123", "456"])
        condition.setPredicate(PredicateType.DOES_NOT_START_WITH)
        user.with("userId", "3333333")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "123456")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[does not contain] condition match"() {
        when:
        condition.setObjects(["12345", "987654", "665544"])
        condition.setPredicate(PredicateType.DOES_NOT_CONTAIN)
        user.with("userId", "999999999")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "12345")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

    def "[does not match regex] condition match"() {
        when:
        condition.setObjects(["0?(13|14|15|18)[0-9]{9}"])
        condition.setPredicate(PredicateType.DOES_NOT_MATCH_REGEX)
        user.with("userId", "2122121")
        def hitSuccess = condition.matchObjects(user)
        user.with("userId", "13797347245")
        def hitMiss = condition.matchObjects(user)
        then:
        with() {
            hitSuccess
            !hitMiss
        }
    }

}

