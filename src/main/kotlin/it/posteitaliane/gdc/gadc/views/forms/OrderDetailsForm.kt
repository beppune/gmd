package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Supplier

data class OrderDetails(
    var type: Order.Type?=null,
    var subject: Order.Subject?=null,
    var datacenter: Datacenter?=null,
    var supplier: Supplier?=null,

)

class TypeChangeEvent(component: OrderDetailsForm, val type: Order.Type)
    : ComponentEvent<OrderDetailsForm>(component, false)

class SubjectChangeEvent(component: OrderDetailsForm, val subject: Order.Subject)
    : ComponentEvent<OrderDetailsForm>(component, false)

class DcChangeEvent(component: OrderDetailsForm, val dc: Datacenter)
    : ComponentEvent<OrderDetailsForm>(component, false)

class OrderDetailsForm(
    private val dcs: List<Datacenter>,
    private val sups: List<Supplier>,
    private val firm: Supplier
): FormLayout() {

    private val typeField = Select<Order.Type>()
    private val subjectField = Select<Order.Subject>()
    private val dcField = Select<Datacenter>()
    private val supplierField = ComboBox<Supplier>()

    private var bean = OrderDetails()
    val binder = Binder(OrderDetails::class.java)


    init {

        binder.bean = bean

        gui()

        bind()
    }

    fun addTypeChangeListener(listener: ComponentEventListener<TypeChangeEvent>) =
        addListener(TypeChangeEvent::class.java, listener)

    fun addSubjectChangeListener(listener: ComponentEventListener<SubjectChangeEvent>) =
        addListener(SubjectChangeEvent::class.java, listener)

    fun addDcChangeListener(listener: ComponentEventListener<DcChangeEvent>) =
        addListener(DcChangeEvent::class.java, listener)

    fun compile(): OrderDetails? {
        if(validate().isOk) {
            return binder.bean
        }

        return null
    }

    fun validate() = binder.validate()

    private fun bind() {
        binder.forField(typeField)
            .asRequired()
            .bind({it.type},{odp,value->odp.type=value})

        binder.forField(subjectField)
            .asRequired()
            .bind({it.subject},{odp,value->odp.subject=value})

        binder.forField(dcField)
            .asRequired()
            .bind({it.datacenter},{odp,value->odp.datacenter=value})

        binder.forField(supplierField)
            .asRequired()
            .bind({it.supplier},{odp,value->odp.supplier=value})
    }

    private fun gui() {
        typeField.apply {
            placeholder = "OPERAZIONE"
            setItemLabelGenerator {
                when (it) {
                    Order.Type.INBOUND -> "CARICO"
                    Order.Type.OUTBOUND -> "SCARICO"
                }
            }

            setItems(Order.Type.entries)

            addValueChangeListener {
                if(it.value != null ) {
                    fireEvent(TypeChangeEvent(this@OrderDetailsForm, it.value))
                }
            }
        }

        subjectField.apply {
            placeholder = "TIPO"
            setItemLabelGenerator {
                when(it) {
                    Order.Subject.INTERNAL -> "INTERNO"
                    Order.Subject.SUPPLIER -> "FORNITORE"
                    Order.Subject.SUPPLIER_DC -> "MOVING"
                }
            }
            setItems(Order.Subject.INTERNAL, Order.Subject.SUPPLIER)

            addValueChangeListener {
                if(it.value!=Order.Subject.SUPPLIER) {
                    supplierField.value = firm
                    supplierField.isEnabled = false
                } else {
                    supplierField.clear()
                    supplierField.isEnabled = true
                }
            }
        }

        dcField.apply {
            placeholder = "DATACENTER"
            setItemLabelGenerator { "${it.short} - ${it.fullName}" }
            setItems(dcs)
        }

        supplierField.apply {
            placeholder = "FORNITORE"
            supplierField.setItems(sups)
            supplierField.setItemLabelGenerator { it.name.uppercase() }
        }

        add(typeField, subjectField)
        add(dcField, supplierField)
    }

    fun reset() {
        bean = OrderDetails()
        binder.bean = bean
        supplierField.clear()
        supplierField.isEnabled = true
    }

}