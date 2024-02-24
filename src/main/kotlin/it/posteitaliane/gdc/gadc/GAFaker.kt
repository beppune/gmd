package it.posteitaliane.gdc.gadc

import it.posteitaliane.gdc.gadc.model.Person
import net.datafaker.Faker
import net.datafaker.providers.base.AbstractProvider
import net.datafaker.providers.base.BaseProviders
import kotlin.random.Random

class PersonsProvider : AbstractProvider<BaseProviders>(Faker()) {

    fun person(
        uuid:Int=Random.nextInt(),
        lastName:String=faker.name().lastName(),
        firstName:String=faker.name().firstName()
        ):Person {
        return Person(uuid,lastName,firstName)
    }

}

class GAFaker : Faker() {

    fun person(): PersonsProvider {
        return getProvider(PersonsProvider::class.java, {PersonsProvider()}, this)
    }

}