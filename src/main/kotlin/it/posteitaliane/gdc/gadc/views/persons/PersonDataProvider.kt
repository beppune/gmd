package it.posteitaliane.gdc.gadc.views.persons

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Person
import it.posteitaliane.gdc.gadc.services.PersonService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class PersonDataProvider(private val service:PersonService) : AbstractBackEndDataProvider<Person, PersonFilter>() {

    override fun fetchFromBackEnd(query: Query<Person, PersonFilter>?): Stream<Person> {
        var stream = service.findAll().stream()


        // Filtering
        if (query != null) {
            stream = stream.filter {
                query.filter.getOrNull()?.test(it) ?: true
            }
        }

        // Sorting
        if( query != null && query.sortOrders.size > 0 ) {
            stream = stream.sorted(sortComparator(query.sortOrders) )
        }

        // Pagination

        return stream.skip(query?.offset?.toLong() ?: 0).limit(query?.limit?.toLong() ?: 100)

    }

    override fun sizeInBackEnd(query: Query<Person, PersonFilter>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }

    private fun sortComparator(sortOrders:List<QuerySortOrder>) : Comparator<Person> {
        return sortOrders.stream().map { sortOrder ->

            var comparator = personFieldComparator(sortOrder.sorted)

            if( sortOrder.direction == SortDirection.DESCENDING ) {
                comparator = comparator.reversed()
            }

            comparator
        }.reduce( Comparator<Person>::thenComparing ).orElse(Comparator { _, _ -> 0 })
    }

    private fun personFieldComparator(sorted:String) : Comparator<Person> {
        if( sorted == "lastName" ) {
            return Comparator.comparing { it.lastName }
        }

        if( sorted == "firstName" ) {
            return Comparator.comparing { it.firstName }
        }

        if( sorted == "uuid" ) {
            return Comparator.comparing { it.uuid }
        }

        return Comparator { _, _ -> 0 }
    }
}