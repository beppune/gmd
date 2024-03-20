package it.posteitaliane.gdc.gadc.views.storage

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.StorageService
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class StorageProvider(private val service:StorageService) : AbstractBackEndDataProvider<Storage, String>() {
    override fun fetchFromBackEnd(query: Query<Storage, String>?): Stream<Storage> {
        if( query == null ) return service.findAll().stream()

        val filter:String? = query.filter.getOrNull()

        return service.find(
            offset = query.offset,
            limit = query.limit,
            searchKey = filter
        ).stream()
    }

    override fun sizeInBackEnd(query: Query<Storage, String>?): Int {
        return fetchFromBackEnd(query).count().toInt()
    }
}