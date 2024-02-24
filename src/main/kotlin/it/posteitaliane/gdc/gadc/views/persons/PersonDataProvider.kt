package it.posteitaliane.gdc.gadc.views.persons

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Person
import it.posteitaliane.gdc.gadc.services.PersonService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class PersonDataProvider(private val service:PersonService) : AbstractBackEndDataProvider<Person, PersonFilter>() {

    override fun fetchFromBackEnd(query: Query<Person, PersonFilter>?): Stream<Person> {
        if( query == null) return service.findAll().stream()

        var filter:String?= query.filter.getOrNull()?.searchTerm
        var sort:String?=null
        var asc:Boolean=true

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

    override fun sizeInBackEnd(query: Query<Person, PersonFilter>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }


}