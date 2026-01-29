package it.posteitaliane.gdc.gmd.services

data class Result<T>(
    val result:T?,
    val error:String?=null
) {
    fun isError() = error != null
}