package io.posteitaliane.gdc.gmd.sqlbuilder

interface Projection {
    fun build(): String
}

class StringProjection(val value:String) : Projection {
    override fun build(): String = value
}

class QuotedProjection(val value: Projection) : Projection {
    override fun build(): String = "'${value.build()}'"
}

fun q(p: Projection): Projection = QuotedProjection(p)
fun q(s:Any): Projection = QuotedProjection(StringProjection(s.toString()))

class AliasProjection(val value: Projection, val alias:String) : Projection {
    override fun build(): String = "${value.build()} AS $alias"
}

infix fun String.AS(alias:String): Projection = AliasProjection(StringProjection(this), alias)
infix fun Projection.AS(alias:String): Projection = AliasProjection(this, alias)

class ConcatProjection(val projections: List<Projection>) : Projection {
    override fun build() =
        projections.joinToString(
            prefix = "CONCAT(",
            postfix = ")",
            separator = ", ",
            transform = Projection::build
        )
}

fun CONCAT(vararg cat: Any?): Projection {
    val list = cat.filterNotNull()
        .map(::map2projections)
    return ConcatProjection(list)
}

fun map2projections(any:Any): Projection = when (any) {
    is String -> StringProjection(any)
    is Int -> StringProjection(any.toString())
    is Float -> StringProjection(any.toString())
    is Double -> StringProjection(any.toString())
    is Projection -> any
    else -> throw ProjectionException("Illegal source type: ${any::class.java}")
}