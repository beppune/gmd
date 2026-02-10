package it.posteitaliane.gdc.gmd.views.orders

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.services.OrderService
import it.posteitaliane.gdc.gmd.views.storage.StorageFilter
import org.slf4j.LoggerFactory
import java.util.stream.Stream

class OrdersProvider(
    private val service:OrderService
) : AbstractBackEndDataProvider<Order, StorageFilter>() {
    override fun fetchFromBackEnd(query: Query<Order, StorageFilter>): Stream<Order> {

        val filter = query.filter.get()
        var sort:String?=null
        var asc = true

        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc = false
            }
        }

        var dcs = filter.dcs.union(filter.others)

        LoggerFactory.getLogger(javaClass).info("Query $query")
        return service.find(query.offset, query.limit, filter.operators.toList(), dcs.toList(),
            filter.from, filter.to, filter.type?.name, filter.subject?.name, filter.status?.name,
            listOf(filter.ref as String), filter.items.toList(), filter.positions.toList(),
            filter.sn, filter.pt, sort, asc).stream()
    }

    override fun sizeInBackEnd(query: Query<Order, StorageFilter>): Int {
        val filter = query.filter.get()
        var sort:String?=null
        var asc = true

        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc = false
            }
        }

        var dcs = filter.dcs.union(filter.others)

        return service.count(query.offset, query.limit, filter.operators.toList(), dcs.toList(),
            filter.from, filter.to, filter.type?.name, filter.subject?.name, filter.status?.name,
            listOf(filter.ref as String), filter.items.toList(), filter.positions.toList(),
            filter.sn, filter.pt, sort, asc)
    }
}