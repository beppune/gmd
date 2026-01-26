package it.posteitaliane.gdc.gadc.views.transactions

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Transaction
import it.posteitaliane.gdc.gadc.services.TransactionsService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class TransactionDataProvider(
    private val service:TransactionsService
) : AbstractBackEndDataProvider<Transaction, TransactionFilter>() {

    override fun fetchFromBackEnd(query: Query<Transaction, TransactionFilter?>): Stream<Transaction> {
        var asc: Boolean = true
        var sort: String? = null

        // sort and sort direction
        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted
            if( query.sortOrders.first().direction == SortDirection.DESCENDING) {
                asc = false
            }
        }

        return service.find(
            offset = query.offset,
            limit = query.limit,
            sortKey = sort,
            ascending = asc,
            searchFilter = query.filter.orElse(null)
        ).stream()

    }

    override fun sizeInBackEnd(query: Query<Transaction, TransactionFilter?>): Int {
        return service.count(
            offset = query.offset,
            limit = query.limit,
            searchFilter = query.filter.orElse(null)
        )
    }

}