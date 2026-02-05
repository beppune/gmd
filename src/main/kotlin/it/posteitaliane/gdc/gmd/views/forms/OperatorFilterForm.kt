package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Operator
import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.Supplier
import it.posteitaliane.gdc.gmd.services.DatacenterService
import it.posteitaliane.gdc.gmd.services.OperatorService
import it.posteitaliane.gdc.gmd.services.StorageService
import it.posteitaliane.gdc.gmd.services.SupplierService
import it.posteitaliane.gdc.gmd.views.storage.StorageFilter

class OperatorFilterForm<ModelType>(
    private var provider: ConfigurableFilterDataProvider<ModelType, Void, StorageFilter>,
    private val dcs: DatacenterService,
    private val ss: StorageService,
    private val sups: SupplierService,
    private val ops: OperatorService,
): VerticalLayout() {

    private var filter = StorageFilter()

    private val defaultAmend:(StorageFilter) -> Unit = {
        provider.setFilter(it)
    }

    /* UI */
    private val rowOne = FlexLayout()
    private val rowTwo = FlexLayout()
    private val rowThree = FlexLayout()
    private val rowFour = FlexLayout()

    /* Date filter */
    private lateinit var fromField: DatePicker;
    private lateinit var toField: DatePicker;

    fun makeDate() {
        fromField = DatePicker().apply {
            placeholder = "DAL"
            isRequired = false
            addValueChangeListener {
                if (fromField.value != null) {
                    filter.from = it.value
                    defaultAmend.invoke(filter)
                }
            }

            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }

        toField = DatePicker().apply {
            placeholder = "AL"
            isRequired = false
            addValueChangeListener {
                if (fromField.value != null) {
                    filter.to = it.value
                    defaultAmend.invoke(filter)
                }
            }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }
        rowOne.add(fromField, toField)

    }

    /* Datacenters */
    private lateinit var dcsField: CheckboxGroup<Datacenter>

    fun makeDcs() {
        dcsField = CheckboxGroup<Datacenter>().apply {
            setItems(dcs.findAll())
            itemLabelGenerator = ItemLabelGenerator { it.fullName.replace("DC ","") }

            addValueChangeListener {
                filter.dcs.clear()
                filter.dcs.addAll(it.value.map(Datacenter::short))
                defaultAmend.invoke(filter)
            }
        }

        rowOne.add(dcsField)
    }

    /* Other Datacenters */
    private var others = HorizontalLayout()
    private lateinit var showOthersField: Checkbox
    private lateinit var othersDcsField: CheckboxGroup<Datacenter>

    fun makeOthers() {
        showOthersField = Checkbox().apply {
            label = "MOSTRA TUTTI"
            addValueChangeListener {
                othersDcsField.isVisible = it.value
                filter.showOthers = it.value
                defaultAmend.invoke(filter)
            }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }

        othersDcsField = CheckboxGroup<Datacenter>().apply {
            isVisible = false
            setItems(dcs.findOthers())
            itemLabelGenerator = ItemLabelGenerator { it.fullName.replace("DC ","") }
            addValueChangeListener {
                filter.others.clear()
                filter.others.addAll(it.value.map(Datacenter::short))
                defaultAmend.invoke(filter)
            }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }

        rowTwo.add(showOthersField, othersDcsField)
    }

    /* Item, Position, s/n, pt */
    private lateinit var itemFields: MultiSelectComboBox<String>
    private lateinit var positionFields: MultiSelectComboBox<String>
    private lateinit var snField: TextField
    private lateinit var ptField: TextField

    fun makeItems() {
        itemFields = MultiSelectComboBox<String>().apply {
            setItems( ss.findAllItems() )
            placeholder = "MERCE"
            addValueChangeListener {
                filter.items.clear()
                filter.items = it.value
                defaultAmend.invoke(filter)
            }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }

        positionFields = MultiSelectComboBox<String>().apply {
            placeholder = "MERCE"
            addValueChangeListener {
                filter.items.clear()
                filter.items = it.value
                defaultAmend.invoke(filter)
            }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }

        snField = TextField().apply {
            placeholder = "S/N"
            addValueChangeListener {
                filter.sn = it.value
                defaultAmend.invoke(filter)
            }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }

        ptField = TextField().apply {
            placeholder = "PT"
            addValueChangeListener {
                filter.pt = it.value
                defaultAmend.invoke(filter)
            }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
        }

        rowThree.add(itemFields, positionFields, snField, ptField)
    }

    /* Oder */
    private lateinit var typeField: MultiSelectComboBox<Order.Type>
    private lateinit var subjectField: MultiSelectComboBox<Order.Subject>
    private lateinit var satusField: MultiSelectComboBox<Order.Status>
    private lateinit var supsField: MultiSelectComboBox<Supplier>
    private lateinit var usersFiels: MultiSelectComboBox<Operator>

    fun makeOrder() {
        typeField = MultiSelectComboBox<Order.Type>().apply{
            placeholder = "ORDINE"
            setItems(Order.Type.entries)
            itemLabelGenerator = ItemLabelGenerator { it.name }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
            addValueChangeListener {
                filter.type = it.value as Order.Type?
                defaultAmend.invoke(filter)
            }
        }

        subjectField = MultiSelectComboBox<Order.Subject>().apply{
            placeholder = "TIPO"
            setItems(Order.Subject.entries)
            itemLabelGenerator = ItemLabelGenerator { it.name }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
            addValueChangeListener {
                filter.subject = it.value as Order.Subject?
                defaultAmend.invoke(filter)
            }
        }

        satusField = MultiSelectComboBox<Order.Status>().apply{
            placeholder = "STATO"
            setItems(Order.Status.entries)
            itemLabelGenerator = ItemLabelGenerator { it.name }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
            addValueChangeListener {
                filter.status = it.value.firstOrNull()
                defaultAmend.invoke(filter)
            }
        }

        supsField = MultiSelectComboBox<Supplier>().apply{
            placeholder = "FORNITORE"
            setItems(sups.findAll())
            itemLabelGenerator = ItemLabelGenerator { it.name }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
            addValueChangeListener {
                filter.supplier = it.value.firstOrNull()
                defaultAmend.invoke(filter)
            }
        }

        usersFiels = MultiSelectComboBox<Operator>().apply{
            placeholder = "OPERATORI"
            setItems( ops.findAll() )
            itemLabelGenerator = ItemLabelGenerator { "${it.lastName} ${it.firstName}" }
            addClassName(LumoUtility.Margin.Right.MEDIUM)
            addValueChangeListener {
                filter.operators.clear()
                filter.operators.addAll( it.value.map(Operator::username) )
                defaultAmend.invoke(filter)
            }
        }

        rowFour.add(typeField, subjectField, satusField, supsField, usersFiels)
    }

    init {
        add( rowOne )
        add( rowTwo )
        add( rowThree )
        add( rowFour )
    }
}