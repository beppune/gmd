package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import it.posteitaliane.gdc.gadc.model.Storage
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate

@Service
class StorageService(val db:JdbcTemplate, val dcs:DatacenterService, val tr:TransactionTemplate) {

    val storageMapper = RowMapper { rs, _ ->
        Storage(
            item = rs.getString("item"),
            dc = dcs.findByShortName(rs.getString("dc"))!!,
            pos = rs.getString("pos"),
            amount = rs.getInt("amount"),
            sn = rs.getString("sn"),
            pt = rs.getString("pt")
        )
    }

    private val QUERY_ALL = "SELECT item,dc,pos,amount,sn,pt FROM STORAGE"

    private val QUERY_FOR_COUNT = "SELECT item,dc,pos,amount,sn,pt FROM STORAGE" +
            " WHERE item = ? AND dc = ? AND pos = ?"


    fun findAll() : List<Storage> {
        return db.query(QUERY_ALL, storageMapper)
    }

    fun findForCount(item:String, dc:String, pos:String): Storage? {
        val one = db.query(
            QUERY_FOR_COUNT,
            storageMapper,
            item, dc, pos
        )

        if ( one.size == 1 ) return one.get(0)
        else return null
    }

    fun find(offset: Int, limit: Int, searchKey: String?, ascending: Boolean, sortKey: String?) : List<Storage> {
        var query = QUERY_ALL

        if ( searchKey != null ) {
            query += " WHERE "
            query += " item LIKE '$searchKey%'"
        }

        if( sortKey != null ) {
            query += " ORDER BY $sortKey "

            if( !ascending ) {
                query += " DESC "
            }
        }

        query += " LIMIT $limit "

        query += " OFFSET $offset"

        return db.query(query, storageMapper)
    }


    private val QUERY_FOR_SN = "$QUERY_ALL WHERE sn = ?"
    fun findBySn(sn:String?): Storage? {
        if(sn.isNullOrEmpty()) return null
        return db.queryForObject(QUERY_FOR_SN, storageMapper, sn)
    }


    private val QUERY_FOR_PT = "$QUERY_ALL WHERE pt = ?"
    fun findByPt(pt:String?): Storage? {
        if(pt.isNullOrEmpty()) return null
        return db.queryForObject(QUERY_FOR_PT, storageMapper, pt)
    }

    private val CREATE_ITEM_SQL = "INSERT INTO ITEMS(name) VALUES(?)"
    fun addItem(i:String) : Result<String> = tr.execute {

        try {

            db.update(CREATE_ITEM_SQL, i.uppercase())

            return@execute Result(i.uppercase())
        } catch (ex:TransactionException) {
            it.setRollbackOnly()
            println("StorageService::addItem: ${ex.message}")
            return@execute Result(null, "StorageService::addItem: ${ex.message}")
        }

    }!!


    private val CREATE_STORAGE_SQL = "INSERT INTO STORAGE(item,dc,pos,amount,sn,pt) VALUES(?,?,?,?,?,?)"
    private val UPDATE_STORAGE_SQL = "UPDATE STORAGE SET amount = ? " +
            " WHERE item = ? AND dc = ? AND pos = ?"
    private val DELETE_STORAGE_SQL = "DELETE FROM STORAGE WHERE item = ? AND dc = ? AND pos = ?"
    fun updateStorage(line:OrderLine) : Result<Storage> = tr.execute {

        try {

            val s = findForCount(line.item, line.order.dc.short, line.position)

            when(line.order.type) {
                Order.Type.INBOUND -> {
                    if (s == null) {
                        db.update(
                            CREATE_STORAGE_SQL,
                            line.item, line.order.dc.short, line.position, line.amount, line.sn, line.pt
                        )
                    } else {
                        s.amount += line.amount
                        db.update(
                            UPDATE_STORAGE_SQL,
                            s.amount, line.item, line.order.dc.short, line.position
                        )
                    }

                    return@execute Result(s)
                }
                Order.Type.OUTBOUND -> {
                    if( s == null || s.amount < line.amount ) {
                        return@execute Result(null, "Not available(amount: ${if(s==null) 0 else s.amount}): $line")
                    }



                    if(s.amount == line.amount) {
                        db.update(
                            DELETE_STORAGE_SQL,
                            line.item, line.order.dc.short, line.position
                        )
                        return@execute Result(s)
                    }

                    //if( s.amount > line.amount ) {

                    s.amount -= line.amount
                    db.update(
                        UPDATE_STORAGE_SQL,
                        s.amount, line.item, line.order.dc.short, line.position
                    )

                    return@execute Result(s)

                    //}
                }
            }

            return@execute Result(null,"error")
        } catch (ex:TransactionException) {
            it.setRollbackOnly()
            println("StorageService::updateStorage: ${ex.message}")
            return@execute Result(null, "StorageService::addItem: ${ex.message}")
        }
    }!!

}