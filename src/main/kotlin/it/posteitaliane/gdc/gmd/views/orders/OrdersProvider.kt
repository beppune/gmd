package it.posteitaliane.gdc.gmd.views.orders

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.services.OrderService
import it.posteitaliane.gdc.gmd.views.storage.StorageFilter
import org.slf4j.LoggerFactory
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

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

        var query = service.queryBuilder(
            q = "SELECT id,operator,datacenter,supplier,issued,type,subject,status,ref,remarks FROM ORDERS",
            offset = query.offset,
            limit = query.limit,
            operators = filter.operators.toList(),
            dcs = dcs.toList(),
            from = filter.from,
            to = filter.to,
            type = filter.type?.name,
            subject = filter.subject?.name,
            status = filter.status?.name,
            ref = listOf(filter.ref as String),
            items = filter.items.toList(),
            pos = filter.positions.toList(),
            sn = filter.sn,
            pt = filter.pt,
        )

        LoggerFactory.getLogger(javaClass).info("Query $query")
        return emptyList<Order>().stream()
    }

    override fun sizeInBackEnd(query: Query<Order, StorageFilter>?): Int {
        return 0
    }
}