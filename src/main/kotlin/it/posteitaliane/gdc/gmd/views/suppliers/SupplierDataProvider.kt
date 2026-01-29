package it.posteitaliane.gdc.gmd.views.suppliers

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import it.posteitaliane.gdc.gmd.model.Supplier
import it.posteitaliane.gdc.gmd.services.SupplierService
import java.util.stream.Stream

class SupplierDataProvider(
    private val sups: SupplierService
) : AbstractBackEndDataProvider<Supplier, String>() {
    override fun fetchFromBackEnd(query: Query<Supplier, String>?): Stream<Supplier> {

        if(query == null) return sups.find().stream()

        val sort = query.sortOrders
        var filter:String? = null
        var search:String? = null
        var asc = true

        if(query.filter.isPresent) {
            filter = query.filter.get()
        }

        if( sort.size != 0 ) {
            search = sort.first().sorted

            if( sort.first().direction == SortDirection.DESCENDING) {
                asc = false
            }
        }

        return sups.find(
            offset = query.offset,
            limit = query.limit,
            searchKey = filter,
            ascending = asc,
            sortKey = search
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Supplier, String>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }

}