package it.posteitaliane.gdc.gadc

import it.posteitaliane.gdc.gadc.model.*
import net.datafaker.Faker
import net.datafaker.providers.base.AbstractProvider
import net.datafaker.providers.base.BaseProviders
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GaProvider : AbstractProvider<BaseProviders>(Faker()) {

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

    fun role() = Operator.Role.values().random()

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

    fun operator(u: String? = null, active: Boolean = true, r: Operator.Role? = null, dcs: List<Datacenter>) : Operator {

        val username:String = u ?: uid()

        return Operator(
            username = username,
            lastName = faker.name().lastName(),
            firstName = faker.name().firstName(),
            email = "${username}@${faker.internet().domainName()}",
            role = r ?: role(),
            isActive = active,
            localPassword = null
            ).apply {
                permissions.addAll(dcs)
        }
    }

    fun datacenter() : Datacenter {
        return Datacenter(
            short = dcshort(),
            fullName = dcfull(),
            legal = faker.address().streetAddress()
        ).apply {
            locations = faker.getFaker<Faker>()
                .collection({faker.expression("#{examplify 'AB - 12'}")})
                .minLen(2).maxLen(12).generate()
        }
    }

    fun supplier() : Supplier {
        return Supplier(
            name = faker.company().name(),
            legal = faker.address().streetAddress(),
            piva = piva()
        ).apply {
            addresses.addAll(
                faker.getFaker<Faker>().collection({faker.address().streetAddress()})
                    .len(2).generate()
            )
        }
    }

    fun order(items:List<String>, dcs:List<Datacenter>, ops:List<Operator>, sups:List<Supplier>, t: Order.Type?=null, s:Order.Subject?=null) : Order {
        return Order(
            dc = dcs.random(),
            op = ops.random(),
            type = t ?: Order.Type.values().random(),
            subject = s ?: Order.Subject.values().random(),
            supplier = sups.random(),
            status = Order.Status.COMPLETED,
            issued = LocalDate.now()
        ).apply {
            lines.add( orderline(this, items, dc) )
            lines.add( orderline(this, items, dc) )
            lines.add( orderline(this, items, dc) )
        }
    }

    fun orderline(ownedby:Order, items:List<String>, dc:Datacenter) : OrderLine {
        return OrderLine(
            order = ownedby,
            item = items.random(),
            position = dc.locations.random(),
            amount = Random.nextInt(1, 10)
        )
    }

    fun piva() = faker.number().digits(11).toString()

}

class GAFaker : Faker() {

    fun ga(): GaProvider {
        return getProvider(GaProvider::class.java, {GaProvider()}, this)
    }

}