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
            items = filter.items,
            dcs = dcs.toList(),
            positions = filter.positions.toList(),
            ascending = asc,
            showOthers = filter.showOthers,
            sn = filter.sn,
            pt = filter.pt,
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Storage, StorageFilter>): Int {
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

        return service.count(
            offset = query.offset,
            limit = query.limit,
            sortKey = sort,
            items = filter.items,
            dcs = dcs.toList(),
            positions = filter.positions.toList(),
            ascending = asc,
            showOthers = filter.showOthers,
            sn = filter.sn,
            pt = filter.pt,
        )
    }
}