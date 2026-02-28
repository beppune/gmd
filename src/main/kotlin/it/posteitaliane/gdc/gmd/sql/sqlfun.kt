package it.posteitaliane.gdc.gmd.sqlfun

import it.posteitaliane.gdc.gmd.sql.AsProjection
import it.posteitaliane.gdc.gmd.sql.Filter
import it.posteitaliane.gdc.gmd.sql.InFilter
import it.posteitaliane.gdc.gmd.sql.JoinBuilder
import it.posteitaliane.gdc.gmd.sql.LikeFilter
import it.posteitaliane.gdc.gmd.sql.SelectBuilder
import it.posteitaliane.gdc.gmd.sql.WhereBuilder
import it.posteitaliane.gdc.gmd.sql.toProjection

infix fun String.AS(s:String): AsProjection {
    return AsProjection( this.toProjection(), s )
}

infix fun String.ON( p:Pair<String,String> ): String {
    return "$this ON(${p.first} = ${p.second})"
}

infix fun String.LIKE( s:String? ): LikeFilter? {
    if (s == null) return null
    return LikeFilter(this, s)
}

infix fun String.IN(els:List<String>?): InFilter? {
    if(els.isNullOrEmpty()) return null
    return InFilter(this, els)
}

fun existsIn(table:String, vararg f: Filter?): WhereBuilder =
    SelectBuilder("1").from(table).where(*f)