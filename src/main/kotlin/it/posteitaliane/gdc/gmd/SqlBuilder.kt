package it.posteitaliane.gdc.gmd


class SelectBuilder: JoinBuilder() {

    fun select(vararg cols:String): JoinBuilder {
        cols.forEach(columns::add)
        return this
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






