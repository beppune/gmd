package it.posteitaliane.gdc.gadc

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@Theme("gdc")
@SpringBootApplication
class GestioneAccessiDatacenterApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<GestioneAccessiDatacenterApplication>(*args)
}
