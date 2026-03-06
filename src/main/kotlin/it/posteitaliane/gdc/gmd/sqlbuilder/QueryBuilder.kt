package io.posteitaliane.gdc.gmd.sqlbuilder

interface SqlPart {
    fun build(): String
}

interface WhereBuilder: SqlPart {
    fun filter(sb: StringBuilder)
}

interface JoinBuilder: SqlPart {
    fun join(sb: StringBuilder)
    fun join(right:String): JoinBuilder

    fun where(vararg any: Any?): JoinBuilder
}

interface SelectBuilder: SqlPart {
    fun project(sb:StringBuilder)
    fun from(vararg any:Any?): JoinBuilder
}

class QueryBuilder(
    val projections: List<Projection>,
    val joins: MutableList<Join> = mutableListOf(),
    val filters: MutableList<Filter> = mutableListOf(),
    ) : SelectBuilder, JoinBuilder, WhereBuilder {

    override fun build(): String {
        val sb = StringBuilder()
        project(sb)
        join(sb)
        filter(sb)
        return sb.toString()
    }

    override fun project(sb: StringBuilder) {

        if (projections.isEmpty()) {
            sb.append("SELECT * ")
            return
        }

        projections.joinToString(
            prefix = "SELECT ",
            separator = ", ",
            postfix = " ",
            transform = Projection::build
        ).also(sb::append)

    }

    override fun from(vararg any:Any?): JoinBuilder {
        any.filterNotNull().map(::map2join).also(joins::addAll)
        return this
    }

    override fun join(sb: StringBuilder) {
        if( joins.isNotEmpty() ) {
            joins.joinToString(
                prefix = "FROM ",
                separator = " JOIN ",
                postfix = " ",
                transform = Join::build
            ).also(sb::append)
        }
    }

    override fun join(right: String): JoinBuilder {
        joins.add(TableName(right))
        return this
    }

    override fun filter(sb: StringBuilder) {
        if( filters.isNotEmpty() ) {
            filters.joinToString(
                prefix = "WHERE ",
                separator = " AND ",
                postfix = " ",
                transform = Filter::build
            ).also(sb::append)
        }
    }

    override fun where(vararg any: Any?): JoinBuilder {
        any.filterNotNull().map(::map2filter).also(filters::addAll)
        return this
    }

}

fun select(vararg columns: Any?): SelectBuilder {
    val list:List<Projection> = columns.filterNotNull()
        .map(::map2projections)
    return select(list)
}

fun select(columns: List<Projection>): SelectBuilder {
    return QueryBuilder( columns )
}