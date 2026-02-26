package it.posteitaliane.gdc.gmd.sqlfun

import it.posteitaliane.gdc.gmd.AsProjection
import it.posteitaliane.gdc.gmd.toProjection

infix fun String.AS(s:String): AsProjection {
    return AsProjection( this.toProjection(), s )
}

infix fun String.ON( p:Pair<String,String> ): String {
    return "$this ON(${p.first} = ${p.second})"
}

infix fun String.LIKE( s:String? ): String? {
    if (s == null) return null
    return "$this LIKE '$s%'"
}

infix fun String.IN( els:List<String>?): String? {
    if(els.isNullOrEmpty()) return null
    return els.joinToString(
        prefix = "$this IN (",
        postfix = ")",
        separator = ", ",
        transform = { "'$it'" }
    )
}
