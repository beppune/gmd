package it.posteitaliane.gdc.gmd.sqlfun

infix fun String.AS(s:String): String {
    return "$this AS $s"
}

infix fun String.ON( p:Pair<String,String> ): String {
    return "$this ON(${p.first} = ${p.second})"
}
