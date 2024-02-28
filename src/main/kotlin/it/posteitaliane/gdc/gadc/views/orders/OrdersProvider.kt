package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.OrderService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class OrdersProvider(private val service:OrderService) : AbstractBackEndDataProvider<Order, String>() {
    override fun fetchFromBackEnd(query: Query<Order, String>?): Stream<Order> {
        if( query == null ) return service.findAll().stream()

        val filter:String? = query.filter.getOrNull()
        

        return service.find(
            offset = query.offset,
            limit = query.limit,
            searchKey = filter
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Order, String>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }
}