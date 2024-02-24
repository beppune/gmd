package it.posteitaliane.gdc.gadc

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootApplication
class GestioneAccessiDatacenterApplication {

	@Bean
	fun runner(jdbc:JdbcTemplate) : CommandLineRunner {
		return CommandLineRunner {
			jdbc.update("CREATE TABLE PERSONS(" +
					"id INT NOT NULL PRIMARY KEY," +
					"lastname VARCHAR(100) NOT NULL," +
					"firstname VARCHAR(100) NOT NULL)")

			val faker = GAFaker()
			for (i in  1..1000) {
				jdbc.update("INSERT INTO PERSONS VALUES(?, ?, ?)",
					faker.number().positive(), faker.name().lastName(), faker.name().firstName())
			}
		}
	}

}

fun main(args: Array<String>) {
	runApplication<GestioneAccessiDatacenterApplication>(*args)
}
