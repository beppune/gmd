package io.posteitaliane.gdc.gmd.sqlbuilder

interface Join {
    fun build(): String
}

class TableName(val name:String): Join {
    override fun build(): String = name
}

class ConditionalJoin(val join: Join, val pair: Pair<String,String>): Join {
    override fun build(): String {
        return "${join.build()} ON(${pair.first}=${pair.second})"
    }
}

fun map2join(any: Any): Join = when(any) {
    is String -> TableName(any)
    is Join -> any
    else -> throw JoinException("Unknown join source type: ${any.javaClass}")
}

infix fun String.ON( pair:Pair<String,String>) : ConditionalJoin {
    return ConditionalJoin(TableName(this), pair)
}