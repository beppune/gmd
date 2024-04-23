package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
class LoginView(
    private val loginForm:LoginForm= LoginForm()
) : VerticalLayout(), BeforeEnterObserver {

    init {

        addClassNames("login-view")
        setSizeFull()
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        alignItems = FlexComponent.Alignment.CENTER

        loginForm.action = "login"

        add(H1("GESTIONE MAGAZZINO DATACENTER"))
        add(loginForm)

    }

    override fun beforeEnter(ev: BeforeEnterEvent) {
        if(ev.location.queryParameters.parameters.contains("error")) {
            loginForm.isError = true
        }
    }

}