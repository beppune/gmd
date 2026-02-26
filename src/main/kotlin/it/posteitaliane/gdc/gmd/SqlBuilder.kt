package it.posteitaliane.gdc.gmd


class SelectBuilder: JoinBuilder() {

    fun select(vararg cols:String): JoinBuilder {
        cols.forEach(columns::add)
        return this
    }

    override fun build(pretty: Boolean, builder: StringBuilder): String {

        if(columns.isEmpty()) throw RuntimeException("Empty Columns")

        val top_separator = if (pretty.not()) " " else "\n    "
        columns.joinToString(
            separator = ", ",
            prefix = "SELECT$top_separator",
        ).also(builder::append)

        super.build(pretty, builder)

        return builder.toString()
    }
}

open class JoinBuilder {

    protected val columns: MutableList<String> = mutableListOf()
    protected val joins: MutableList<String> = mutableListOf()

    fun from(table: String): JoinBuilder {
        joins.add(0, table)
        return this
    }

    fun join(table:String): JoinBuilder {
        joins.add(table)
        return this
    }

    open fun build(pretty: Boolean=false, builder: StringBuilder = StringBuilder()): String {

        if( joins.isEmpty() ) return builder.toString()
        val top_separator = if (pretty.not()) " " else "\n    "

        joins.joinToString(
            separator = " JOIN ",
            prefix = " FROM$top_separator",
        ).also(builder::append)

        return builder.toString()
    }
}

