package it.posteitaliane.gdc.gadc

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import it.posteitaliane.gdc.gadc.model.Operator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@Theme("gdc")
@SpringBootApplication
class GestioneAccessiDatacenterApplication : AppShellConfigurator {

	@Bean
	fun operator() = GAFaker().ga().operator()

}

fun main(args: Array<String>) {
	runApplication<GestioneAccessiDatacenterApplication>(*args)
}
