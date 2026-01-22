package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.OrderService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

class OrdersProvider(
    private val service:OrderService
) : AbstractBackEndDataProvider<Order, OrdersFilter>() {
    override fun fetchFromBackEnd(query: Query<Order, OrdersFilter>?): Stream<Order> {
        if( query == null ) return service.findAll().stream()

        val filter:String? = query.filter.getOrNull()?.searchKery
        var sort:String?=null
        var dcs:String?=null
        var asc = true

        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc = false
            }
        }

        if( query.filter.isPresent ) {
            val list = query.filter.get().dcs
            if(list.isNotEmpty() ) {
                dcs = list.map(Datacenter::short)
                    .joinToString(
                        separator = ",",
                        prefix = "(",
                        postfix = ")",
                        transform = { "'$it'" }
                    )
            }
        }

        return service.find(
            offset = query.offset,
            limit = query.limit,
            searchKey = filter,
            sortKey = sort,
            dcs = dcs,
            ascending = asc
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Order, OrdersFilter>?): Int {
        if( query == null ) return 0

        val filter:String? = query.filter.getOrNull()?.searchKery
        var sort:String?=null
        var dcs:String?=null
        var asc = true

        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc = false
            }
        }

        if( query.filter.isPresent ) {
            val list = query.filter.get().dcs
            if(list.isNotEmpty() ) {
                dcs = list.map(Datacenter::short)
                    .joinToString(
                        separator = ",",
                        prefix = "(",
                        postfix = ")",
                        transform = { "'$it'" }
                    )
            }
        }

        return service.count(
            offset = query.offset,
            limit = query.limit,
            searchKey = filter,
            sortKey = sort,
            dcs = dcs,
            ascending = asc
        )
    }
}