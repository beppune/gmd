package it.posteitaliane.gdc.gadc.views.persons

import it.posteitaliane.gdc.gadc.model.Person

class PersonFilter(var searchTerm:String="") {

    fun test(person:Person) : Boolean {
        if(searchTerm.isEmpty()) return true

        val matchesLastName = person.lastName.contains(searchTerm, true)
        val matchesFirstName = person.firstName.contains(searchTerm, true)
        val matchesId = person.uuid.toString().contains(searchTerm, true)

        return matchesId || matchesLastName || matchesFirstName
    }

}