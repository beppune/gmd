package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.textfield.TextFieldVariant
import com.vaadin.flow.data.binder.Binder
import java.util.stream.Stream

data class ShippingPresentation(
    var motive:String?=null,
    var hauler:String?=null,
    var address:String?=null,
    var numpack:Int?=null
)

class ShippingForm: FormLayout() {

    private val motiveField = TextField()
    private val haulerField = TextField()
    private val addressField = TextField()
    private val numpackField = IntegerField()

    val binder = Binder(ShippingPresentation::class.java)
    private var bean = ShippingPresentation()

    init {

        binder.bean = bean

        gui()

        bind()
    }

    fun validate() = binder.validate()

    fun compile(): ShippingPresentation? {
        if(validate().isOk){
            return ShippingPresentation(
                hauler = binder.bean.hauler,
                motive = binder.bean.motive,
                address = binder.bean.address,
                numpack = binder.bean.numpack
            )
        }

        return null
    }

    private fun bind() {
        binder.forField(motiveField)
            .asRequired("Campo obbligatorio")
            .bind({it.motive},{sp,value->sp.motive=value})

        binder.forField(haulerField)
            .asRequired("Campo obbligatorio")
            .bind({it.hauler},{sp,value->sp.hauler=value})

        binder.forField(addressField)
            .asRequired("Campo obbligatorio")
            .bind({it.address},{sp,value->sp.address=value})

        binder.forField(numpackField)
            .asRequired("Campo obbligatorio")
            .bind({it.numpack},{sp,value->sp.numpack=value})
    }

    private fun gui() {
        Stream.of(motiveField, haulerField, addressField, numpackField)
            .forEach {
                it.addThemeVariants(TextFieldVariant.LUMO_SMALL)
            }

        haulerField.placeholder = "TRASPORTATORE"
        motiveField.placeholder = "CAUSALE"
        addressField.placeholder = "INDIRIZZO DI DESTINAZIONE"
        numpackField.placeholder = "N. COLLI"

        numpackField.min = 1

        add(motiveField, haulerField)
        add(addressField, numpackField)
    }

    fun reset() {
        bean = ShippingPresentation()
        binder.bean = bean
    }


}