package it.posteitaliane.gdc.gadc.runners

import it.posteitaliane.gdc.gadc.GAFaker
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Supplier
import it.posteitaliane.gdc.gadc.services.BackOffice
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.CannotCreateTransactionException
import kotlin.random.Random

@Component
@Profile("initdb")
class CreateDatabaseRunner(val bo:BackOffice, val ctx:ApplicationContext, val config:GMDConfig) : CommandLineRunner {

    fun exit() = SpringApplication.exit(ctx)

    override fun run(vararg args: String?) {

        val firm = Supplier(
            name = config.firmName,
            legal = config.firmLegal,
            piva = config.firmPiva
        )

        bo.sups.create(firm)

        val faker = GAFaker()

        for (i in 0..2) {
            val result = bo.dcs.create(faker.ga().datacenter())
            if(result.isError()) {
                println(result)
                exit()
            }
        }

        val dcs = bo.dcs.findAll(true)

        val ops:List<Operator> = faker.collection({faker.ga().operator( r = Operator.Role.OPERATOR, dcs=dcs.subList(0, Random.nextInt(dcs.size) ) ) })
            .len(10).generate()

        ops.forEach { op ->
            val result = bo.ops.create(op)
            if(result.isError()) {
                println(result)
                exit()
            }
        }

        faker.ga().operator(r=Operator.Role.ADMIN, dcs = dcs).also {

            bo.ops.create(it)
            println("ADMIN is ${it.username}")
        }


        val items = faker.collection({faker.appliance().equipment()})
            .len(100, 100).generate<List<String>>().distinct()

        items.forEach {

            val result = bo.ss.addItem(it)
            if(result.isError()) {
                println(result)
                exit()
            }

        }


        val suppliers = faker.collection({faker.ga().supplier()})
            .len(50).generate<List<Supplier>>().distinct()

        suppliers.forEach {  s ->
            val result = bo.sups.create(s)
            if(result.isError()) {
                println(result)
                exit()
            }
        }

        val orders = faker.collection({
            faker.ga().order(items, dcs, ops, suppliers, s = Order.Subject.INTERNAL, t = Order.Type.INBOUND, withUnique = true)
        }).len(10).generate<List<Order>>()

        orders.forEach { o ->
            try {
                val result = bo.os.submit(o)
                if (result.isError()) {
                    println(result.error)
                    exit()
                }
            }catch (ex:CannotCreateTransactionException) {
                ex.printStackTrace()
            }
        }

    }


}