package it.posteitaliane.gdc.gmd.views.storage

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gmd.model.Storage
import it.posteitaliane.gdc.gmd.services.StorageService
import org.slf4j.LoggerFactory
import java.util.stream.Stream

class StorageProvider(
    private val service:StorageService
) : AbstractBackEndDataProvider<Storage, StorageFilter>() {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun fetchFromBackEnd(query: Query<Storage, StorageFilter>): Stream<Storage> {

        val filter: StorageFilter = query.filter.orElse(StorageFilter())
        var sort:String?=null
        var asc=true

        // Sorting
        if( query.sortOrders.size > 0 ) {
            sort = query.sortOrders.first().sorted

            if( query.sortOrders.first().direction == SortDirection.DESCENDING ) {
                asc = false
            }
        }

        var dcs = filter.dcs.union(filter.others).toList()

        return service.find(
            offset = query.offset,
            limit = query.limit,
            sortKey = sort,
            searchKey = filter.key,
            dcs = dcs.toList(),
            ascending = asc,
            showOthers = filter.showOthers,
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Storage, StorageFilter>): Int {
        return fetchFromBackEnd(query).count().toInt()
    }
}