package it.posteitaliane.gdc.gmd.sql

interface Projection

class SimpleProjection(private val s: String) : Projection {
    override fun toString() = s
}

fun String.q(): String {
    return "'${this}'"
}

fun String.dq(): String {
    return "\"${this}\""
}

fun String.toProjection() = SimpleProjection(this)

class ConcatProjection(vararg ps: SimpleProjection): Projection {
    private val list: List<Projection> = ps.toList()
    override fun toString(): String {
        val sb = StringBuilder()
        list.joinToString(
            prefix = "CONCAT(",
            separator = ", ",
            postfix = ")",
            transform = {
                when {
                    it.toString().isBlank() -> "' '"
                    else -> it.toString()
                }
            }
        ).also(sb::append)
        return sb.toString()
    }
}

class AsProjection(private val s: Projection, private val a:String) : Projection {
    override fun toString() = "$s AS $a"
}

fun CONCAT(vararg ps: String): ConcatProjection {
    val l = ps.map(String::toProjection)
    return ConcatProjection(*l.toTypedArray())
}

fun REPLACE(s:String, from:String, to:String) =
    SimpleProjection(
        "REPLACE($s, '$from', '$to')"
    )

infix fun Projection.AS(a:String) = AsProjection(this, a)

fun select(vararg pr: Any): JoinBuilder {
    val list = mutableListOf<String>()
    pr.forEach {
        when(it) {
            is String -> list.add(it.trim())
            is Projection -> list.add( it.toString() )
        }
    }

    return SelectBuilder(*list.toTypedArray())
}

class SelectBuilder(vararg cols:String): JoinBuilder() {

    init {
        cols.forEach(columns::add)
    }

    override fun build(pretty: Boolean, builder: StringBuilder): String {

        if(columns.isEmpty()) throw RuntimeException("Empty Columns")

        val top_separator = if (pretty.not()) " " else "\n    "
        val postfix_separator = if (pretty.not()) " " else "\n"

        columns.joinToString(
            separator = ", ",
            prefix = "SELECT$top_separator",
            postfix = postfix_separator
        ).also(builder::append)

        super.build(pretty, builder)

        return builder.toString()
    }
}

open class JoinBuilder: WhereBuilder() {

    override fun from(table: String): WhereBuilder {
        joins.add(0, table)
        return this
    }

    override fun join(table:String): WhereBuilder {
        joins.add(table)
        return this
    }

    override fun build(pretty: Boolean, builder: StringBuilder): String {

        if( joins.isEmpty() ) return builder.toString()
        val top_separator = if (pretty.not()) " " else "\n    "
        val postfix_separator = if (pretty.not()) " " else "\n"

        joins.joinToString(
            separator = " JOIN ",
            prefix = "FROM$top_separator",
            postfix = postfix_separator
        ).also(builder::append)

        super.build(pretty, builder)

        return builder.toString()
    }
}

open class WhereBuilder {

    protected val columns: MutableList<String> = mutableListOf()
    protected val joins: MutableList<String> = mutableListOf()
    protected val filters: MutableList<String> = mutableListOf()

    open fun from(table: String): WhereBuilder = (this as JoinBuilder).from(table)

    open fun join(table:String): WhereBuilder = (this as JoinBuilder).join(table)

    open fun build(pretty: Boolean=false, builder: StringBuilder = StringBuilder()): String {
        if( filters.isEmpty() ) return builder.toString()

        val top_separator = if (pretty.not()) " " else "\n    "
        val postfix_separator = if (pretty.not()) " " else "\n"

        filters.joinToString(
            separator = " AND ",
            prefix = "WHERE$top_separator",
            postfix = postfix_separator
        ).also(builder::append)

        return builder.toString()
    }

    open fun where(vararg f: String?): WhereBuilder {
        f.filterNotNull().forEach(filters::add)
        return this
    }
}






