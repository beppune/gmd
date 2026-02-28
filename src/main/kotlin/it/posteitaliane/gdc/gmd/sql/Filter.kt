package it.posteitaliane.gdc.gmd.sql

interface Filter

class LikeFilter(private val left:String, private val right:String) : Filter {
    override fun toString() = "$left LIKE '$right%'"
}

class InFilter(private val left:String, private val right:List<String>) : Filter {
    override fun toString(): String =
        right.joinToString(
            prefix = "$left IN (",
            postfix = ")",
            separator = ", ",
            transform = { "'$it'" }
        )
}

class NotFilter(private val f:Filter) : Filter {
    override fun toString(): String = "NOT ($f)"
}

fun NOT(f:Filter?) = f?.let { NotFilter(f) }