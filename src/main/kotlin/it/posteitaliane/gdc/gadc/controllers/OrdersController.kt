package it.posteitaliane.gdc.gadc.controllers

import com.vaadin.flow.spring.annotation.SpringComponent
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringComponent
@RequestMapping("api/orders")
class OrdersController(
    private val os:OrderService
) {

    @GetMapping
    fun findAll() : List<Order> = os.findAll(fetchLines = true)

}