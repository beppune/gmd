package it.posteitaliane.gdc.gadc.events

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentEvent
import it.posteitaliane.gdc.gadc.model.Order

class EditOrderEvent(component:Component, fromClient:Boolean, val o:Order) :
    ComponentEvent<Component>(component, fromClient)