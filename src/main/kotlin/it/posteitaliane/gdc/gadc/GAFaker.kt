package it.posteitaliane.gdc.gadc

import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Person
import net.datafaker.Faker
import net.datafaker.providers.base.AbstractProvider
import net.datafaker.providers.base.BaseProviders
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class PersonsProvider : AbstractProvider<BaseProviders>(Faker()) {

    fun person(
        uuid:Int=Random.nextInt(),
        lastName:String=faker.name().lastName(),
        firstName:String=faker.name().firstName()
        ):Person {
        return Person(uuid,lastName,firstName)
    }

    fun cf(): String? {
        Random.nextBoolean().also {
            if(it) {
                return null
            }
            return faker.expression("#{examplify 'ABCDEF12D34F032L'}")
        }
    }

    fun dcshort() = faker.expression("#{examplify 'AB1'}")

    fun dcfull() = faker.country().capital()

    fun idnumber() = faker.expression("#{examplify 'CA1231434'})")

    fun idtype() = faker.expression("#{options.option 'CI', 'PASS', 'PA', 'PU'}")

    fun role() = faker.expression("#options.option 'OPERATOR', 'ADMIN'}")

    fun expiration() : LocalDate? {
        Random.nextBoolean().also {
            if(it) return null
            return faker.date().future(1000, TimeUnit.DAYS).toLocalDateTime().toLocalDate()
        }
    }

    fun birthdate() : LocalDate? {
        Random.nextBoolean().also {
            if(it) return null
            return faker.date().birthday().toLocalDateTime().toLocalDate()
        }
    }

    fun uid() : String {
        return faker.internet().username().take(8).uppercase()
    }

    fun sn() = faker.expression("#{examplify 'SN12312FDREW3432'})")

    fun pt() = faker.expression("#{examplify '12345678'}")

    fun operator(u:String?=null, active:Boolean=true) : Operator {

        val username:String = u ?: uid()

        return Operator(
            username = username,
            lastName = faker.name().lastName(),
            firstName = faker.name().firstName(),
            email = "${username}@${faker.internet().domainName()}",
            role = role(),
            isActive = active,
            localPassword = null
            )
    }

}

class GAFaker : Faker() {

    fun ga(): PersonsProvider {
        return getProvider(PersonsProvider::class.java, {PersonsProvider()}, this)
    }

}