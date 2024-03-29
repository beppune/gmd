package it.posteitaliane.gdc.gadc.views.transactions

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Transaction
import it.posteitaliane.gdc.gadc.services.TransactionsService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class TransactionDataProvider(val service:TransactionsService) : AbstractBackEndDataProvider<Transaction, TransactionFilter>() {

    override fun fetchFromBackEnd(query: Query<Transaction, TransactionFilter>?): Stream<Transaction> {
        var list = service.findAll()

        if (query == null) return list.stream()

        val filter = query.filter.getOrNull()

        if(filter!=null) {

            if( filter.dc != null ) {
                list = list.filter { tr ->
                    tr.dc.contains(filter.dc!!.short)
                }
            }

            if( filter.from != null ) {
                list = list.filter { tr ->
                    tr.timestamp.isAfter(filter.from)
                }
            }

            if( filter.to != null ) {
                list = list.filter { tr ->
                    tr.timestamp.isBefore(filter.to)
                }
            }
        }

        if( query.sortOrders.size > 0) {

            val key = query.sortOrders.first().sorted
            val desc = query.sortOrders.first().direction == SortDirection.DESCENDING

            var comparator = Comparator<Transaction> { left, right ->
                when(key) {
                    "operator" -> left.operator.compareTo(right.operator)
                    "item" -> left.item.compareTo(right.item)
                    "dc" -> left.dc.compareTo(right.dc)
                    "pos" -> left.pos.compareTo(right.pos)
                    "timestamp" -> left.timestamp.compareTo(right.timestamp)
                    else -> {0}
                }
            }

            if (desc) comparator = comparator.reversed()

            return list.sortedWith(comparator).stream()
        }

        return list.stream()
    }

    override fun sizeInBackEnd(query: Query<Transaction, TransactionFilter>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }

}