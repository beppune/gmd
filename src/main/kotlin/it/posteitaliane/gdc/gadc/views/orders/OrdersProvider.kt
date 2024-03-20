package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.OrderService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class OrdersProvider(private val service:OrderService) : AbstractBackEndDataProvider<Order, String>() {
    override fun fetchFromBackEnd(query: Query<Order, String>?): Stream<Order> {
        if( query == null ) return service.findAll().stream()

        val filter:String? = query.filter.getOrNull()
        var sort:String?=null
        var asc = true

        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc = false
            }
        }

        return service.find(
            offset = query.offset,
            limit = query.limit,
            searchKey = filter,
            sortKey = sort,
            ascending = asc
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Order, String>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }
}