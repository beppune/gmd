package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.Supplier
import java.io.InputStream

data class OrderDetails(
    var type: Order.Type?=null,
    var subject: Order.Subject?=null,
    var datacenter: Datacenter?=null,
    var supplier: Supplier?=null,
    var pending: Boolean=false,
    var remarks: String?=null,
    var filename: String?=null,
)

class TypeChangeEvent(component: OrderDetailsForm, val type: Order.Type)
    : ComponentEvent<OrderDetailsForm>(component, false)

class SubjectChangeEvent(component: OrderDetailsForm, val subject: Order.Subject)
    : ComponentEvent<OrderDetailsForm>(component, false)

class DcChangeEvent(component: OrderDetailsForm, val dc: Datacenter)
    : ComponentEvent<OrderDetailsForm>(component, false)

class FileUploadEvent(component: OrderDetailsForm, val stream: InputStream)
    : ComponentEvent<OrderDetailsForm>(component, false)

class OrderDetailsForm(
    private val dcs: List<Datacenter>,
    private val sups: List<Supplier>,
    private val firm: Supplier,
): FormLayout() {

    private val typeField = Select<Order.Type>()
    private val subjectField = Select<Order.Subject>()
    private val dcField = Select<Datacenter>()
    private val supplierField = ComboBox<Supplier>()
    private val pendingField = Checkbox()
    private val realUpload = Upload(MemoryBuffer())
    private val fakeUpload = Button("UPLOAD SCAN")
    private val remarksField = TextArea()

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

    fun addFileUploadListener(listener: ComponentEventListener<FileUploadEvent>) =
        addListener(FileUploadEvent::class.java, listener)

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

        binder.forField(pendingField)
            .bind({it.pending},{odp,value->odp.pending=value})

        binder.forField(remarksField)
            .bind({it.remarks},{odp,value->odp.remarks=value})
    }

    private fun gui() {
        typeField.apply {
            placeholder = "OPERAZIONE"
            setItemLabelGenerator {
                when (it!!) {
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
                when(it!!) {
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

                fireEvent(SubjectChangeEvent(this@OrderDetailsForm, it.value))
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

        pendingField.apply {
            label = "IN ATTESA"
            setWidth(10.0f, Unit.REM)
        }

        realUpload.apply {
            addClassNames(
                LumoUtility.Padding.NONE,
                LumoUtility.Margin.NONE
            )

            setAcceptedFileTypes("application/pdf")

            addSucceededListener {
                val stream = (it.source.receiver as MemoryBuffer)
                    .inputStream


                Notification.show("Copy in Temporary")
                //files.copyTemp("username", stream)

                fakeUpload.text = it.fileName
                binder.bean.filename = it.fileName

                fireEvent(FileUploadEvent(this@OrderDetailsForm, stream))
            }

            width = "0px"
            height = "0px"

            maxFiles = 1
            maxFileSize = 1024 * 1024 *2
        }

        fakeUpload.apply {
            addThemeVariants(ButtonVariant.LUMO_SMALL)
            addClickListener {
                realUpload.clearFileList()
                if(realUpload.uploadButton is Button) {
                    (realUpload.uploadButton as Button).clickInClient()
                }
            }
            setWidth(20.0f, Unit.REM)
        }

        remarksField.apply {
            placeholder = "NOTE"
        }

        add(typeField, subjectField)
        add(dcField, supplierField)
        add(HorizontalLayout(pendingField, realUpload, fakeUpload), 2)
        add(remarksField, 2)
    }

    fun reset() {
        bean = OrderDetails()
        binder.bean = bean
        supplierField.clear()
        supplierField.isEnabled = true
        realUpload.clearFileList()
        fakeUpload.text = "UPLOAD SCAN"
    }

}