package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import it.posteitaliane.gdc.gadc.model.Storage
import org.slf4j.Logger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate

@Service
class StorageService(
    private val db:JdbcTemplate,
    private val trs:TransactionTemplate,

    private val dcs:DatacenterService,

    private val logger:Logger
) {

    private val storageMapper = RowMapper { rs, _ ->
        Storage(
            item = rs.getString("item"),
            dc = dcs.findByShortName(rs.getString("dc"))!!,
            pos = rs.getString("pos"),
            amount = rs.getInt("amount"),
            sn = rs.getString("sn"),
            pt = rs.getString("pt")
        )
    }

    private val itemMapper = RowMapper { rs, _ ->
        rs.getString("name")
    }

    private val QUERY_ALL = "SELECT item,dc,pos,amount,sn,pt FROM STORAGE"

    private val QUERY_ITEMS = "SELECT name FROM ITEMS ORDER BY name";

    private val QUERY_FOR_COUNT = "SELECT item,dc,pos,amount,sn,pt FROM STORAGE" +
            " WHERE item = ? AND dc = ? AND pos = ?"


    fun findAll() : List<Storage> {
        return db.query(QUERY_ALL, storageMapper)
    }

    fun findAllItems() : List<String> {
        return db.query(QUERY_ITEMS, itemMapper)
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

    fun find(offset: Int=0, limit: Int=1000, searchKey: String?=null, ascending: Boolean=true, sortKey: String?=null, dcsKey:List<String>?=null) : List<Storage> {
        var query = QUERY_ALL

        query += " WHERE TRUE "

        if(dcsKey!=null && dcsKey.isNotEmpty()) {
            val arg = dcsKey.joinToString(
                prefix = "(",
                postfix = ")",
                separator = ",",
                transform = { "'$it'" }
            )
            query += " AND dc IN ${arg} "
        }

        if ( searchKey != null ) {
            query += " AND item LIKE '$searchKey%' "
        }

        if( sortKey != null ) {
            query += " ORDER BY $sortKey "

            if( !ascending ) {
                query += " DESC "
            }
        }

        query += " LIMIT $limit "

        query += " OFFSET $offset"

        logger.debug(query)
        return db.query(query, storageMapper)
    }


    private val QUERY_FOR_SN = "$QUERY_ALL WHERE sn = ?"
    fun findBySn(sn:String?): Storage? {
        if(sn.isNullOrEmpty()) return null
        val list = db.query(QUERY_FOR_SN, storageMapper, sn)
        if( list.size == 0 ) return null
        return list[0]
    }


    private val QUERY_FOR_PT = "$QUERY_ALL WHERE pt = ?"
    fun findByPt(pt:String?): Storage? {
        if(pt.isNullOrEmpty()) return null
        val list = db.query(QUERY_FOR_PT, storageMapper, pt)
        if( list.size == 0 ) return null
        return list[0]
    }

    private val CREATE_ITEM_SQL = "INSERT INTO ITEMS(name) VALUES(?)"
    fun addItem(i:String) : Result<String> = trs.execute {

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
    fun updateStorage(line:OrderLine) : Result<Storage> = trs.execute {

        try {

            val s = if(line.isUnique.not())
                findForCount(line.item, line.order.dc.short, line.position)
            else findByPt(line.pt) ?: findBySn(line.sn)

            when(line.order.type) {
                Order.Type.INBOUND -> {
                    if (s == null) {
                        db.update(
                            CREATE_STORAGE_SQL,
                            line.item, line.order.dc.short, line.position, line.amount, line.sn?.uppercase(), line.pt
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

            //return@execute Result(null,"error")
        } catch (ex:TransactionException) {
            it.setRollbackOnly()
            println("StorageService::updateStorage: ${ex.message}")
            return@execute Result(null, "StorageService::addItem: ${ex.message}")
        } catch (ex:RuntimeException) {
            it.setRollbackOnly()
            println("StorageService::updateStorage: ${ex.message}")
            return@execute Result(null, "StorageService::addItem: ${ex.message}")
        }
    }!!

}