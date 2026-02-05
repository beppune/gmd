package it.posteitaliane.gdc.gmd.services

import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.OrderLine
import it.posteitaliane.gdc.gmd.model.Storage
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
            dc = dcs.findByShortName(rs.getString("short"))!!,
            pos = rs.getString("pos"),
            amount = rs.getInt("amount"),
            sn = rs.getString("sn"),
            pt = rs.getString("pt")
        )
    }

    private val itemMapper = RowMapper { rs, _ ->
        rs.getString("name")
    }

    private val QUERY_ALL = "SELECT item,dc AS short,dcs.fullname AS dc,pos,amount,sn,pt FROM STORAGE JOIN DCS ON dc=shortname "

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

    fun buildQuery(
        q:String,
        offset: Int,
        limit: Int,
        positions: List<String>,
        ascending: Boolean = true,
        sortKey: String? = null,
        dcs: List<String>,
        showOthers: Boolean,
        items: MutableSet<String>,
        pt:String?=null,
        sn:String?=null,
        ): String {

        var query = q;
        var queryparts = mutableListOf<String>()

        if( positions.isNotEmpty() ) {
            positions.joinToString(
                prefix = " pos IN (",
                postfix = ") ",
                separator = ",",
                transform = { "'$it'" }
            ).also(queryparts::add)
        }

        if( dcs.isNotEmpty()) {
            dcs.joinToString(
                prefix = " dc IN (",
                postfix = ") ",
                separator = ",",
                transform = { "'$it'" }
            ).also(queryparts::add)
        }

        if (!showOthers) {
            queryparts.add(" active IS TRUE ")
        }


        if( items.isNotEmpty() ) {
            items.joinToString(
                prefix = " item IN (",
                postfix = ") ",
                separator = ",",
                transform = { "'$it'" }
            ).also(queryparts::add)
        }

        if ( pt.isNullOrBlank().not() ) {
            queryparts.add(" pt LIKE '$pt%' ")
        }

        if ( sn.isNullOrBlank().not() ) {
            queryparts.add(" sn LIKE '$sn%' ")
        }

        if (queryparts.isNotEmpty()) {
            query += " WHERE " + queryparts.joinToString(separator = " AND ")
        }

        if (sortKey.isNullOrEmpty().not()) {
            var direction = if( ascending ) {
                "ASC"
            } else {
                "DESC"
            }
            query += " ORDER BY $sortKey $direction "
        }

        query += " LIMIT $limit OFFSET $offset "

        return query
    }

    fun find(
        offset: Int = 0,
        limit: Int = 1000,
        positions: List<String>,
        ascending: Boolean = true,
        sortKey: String? = null,
        dcs: List<String>,
        showOthers: Boolean,
        items: MutableSet<String>,
        sn: String?,
        pt: String?
    ) : List<Storage> {
        var query = buildQuery(
            q = QUERY_ALL,
            offset = offset,
            limit = limit,
            positions = positions,
            ascending = ascending,
            sortKey = sortKey,
            dcs = dcs,
            showOthers = showOthers,
            items = items,
            sn = sn,
            pt = pt,
            )

        return db.query(query, storageMapper)
    }

    fun count(
        offset: Int = 0,
        limit: Int = 1000,
        positions: List<String>,
        ascending: Boolean = true,
        sortKey: String? = null,
        dcs: List<String>,
        showOthers: Boolean,
        items: MutableSet<String>,
        sn: String?,
        pt: String?
    ) : Int {
        var query = buildQuery(
            q = "SELECT COUNT(*) FROM STORAGE JOIN DCS ON dc=shortname ",
            offset = offset,
            limit = limit,
            positions = positions,
            ascending = ascending,
            sortKey = sortKey,
            dcs = dcs,
            showOthers = showOthers,
            items = items,
            sn = sn,
            pt = pt,
        )

        return db.queryForObject(query, Int::class.java)!!
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

    fun findPosFromStorage(dcs: List<String>? = null): List<String> {
        var query = "SELECT DISTINCT pos FROM STORAGE WHERE TRUE "

        if(!dcs.isNullOrEmpty()) {
            query += dcs.joinToString(
                separator = ",",
                prefix = " AND dc in (",
                postfix = ") ",
                transform = {"'${it}'"}
            )
        }

        query += " ORDER BY pos"

        return db.queryForList(query, String::class.java)
    }

}