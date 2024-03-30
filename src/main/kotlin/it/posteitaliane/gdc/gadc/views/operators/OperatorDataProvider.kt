package it.posteitaliane.gdc.gadc.views.operators

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.services.OperatorService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class OperatorDataProvider(
    private val service:OperatorService
) : AbstractBackEndDataProvider<Operator, OperatorFilter>() {

    override fun fetchFromBackEnd(query: Query<Operator, OperatorFilter>?): Stream<Operator> {
        if( query == null) return service.findAll().stream()

        val filter:String?= query.filter.getOrNull()?.searchTerm
        var sort:String?=null
        var asc=true

        // Sorting
        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc=false
            }
        }

        // Pagination

        return service.find(
            offset = query.offset,
            limit = query.limit,
            ascending = asc,
            sortkey = sort,
            filter = filter
        ).stream()

    }

    override fun sizeInBackEnd(query: Query<Operator, OperatorFilter>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }


}