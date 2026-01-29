package it.posteitaliane.gdc.gmd

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

@Theme("gdc")
@SpringBootApplication
class GestioneAccessiDatacenterApplication : AppShellConfigurator {

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	fun logger(inj:InjectionPoint) = LoggerFactory.getLogger(inj.methodParameter!!.containingClass.canonicalName)

}

fun main(args: Array<String>) {
	runApplication<GestioneAccessiDatacenterApplication>(*args)
}
