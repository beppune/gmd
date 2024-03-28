package it.posteitaliane.gdc.gadc

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.TransactionTemplate

@Theme("gdc")
@SpringBootApplication
class GestioneAccessiDatacenterApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<GestioneAccessiDatacenterApplication>(*args)
}
