package it.posteitaliane.gdc.gadc.views.storage

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.StorageService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class StorageProvider(
    private val service:StorageService
) : AbstractBackEndDataProvider<Storage, StorageFilter>() {
    override fun fetchFromBackEnd(query: Query<Storage, StorageFilter>?): Stream<Storage> {
        if( query == null ) return service.findAll().stream()

        val filter: StorageFilter? = query.filter.getOrNull()
        var sort:String?=null
        var asc=true

        // Sorting
        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc = false
            }
        }

        return service.find(
            offset = query.offset,
            limit = query.limit,
            sortKey = sort,
            searchKey = filter?.key,
            dcsKey = filter?.dcs?.map(Datacenter::short),
            ascending = asc
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Storage, StorageFilter>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }
}