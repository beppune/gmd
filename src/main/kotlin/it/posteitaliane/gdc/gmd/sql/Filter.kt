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
