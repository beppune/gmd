package it.posteitaliane.gdc.gadc

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

}

class GAFaker : Faker() {

    fun ga(): PersonsProvider {
        return getProvider(PersonsProvider::class.java, {PersonsProvider()}, this)
    }

}