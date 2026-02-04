package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Storage
import it.posteitaliane.gdc.gmd.services.DatacenterService
import it.posteitaliane.gdc.gmd.views.storage.StorageFilter
import org.slf4j.LoggerFactory
import java.time.LocalDate

class OperatorFilterForm<ModelType>(
    private var provider: ConfigurableFilterDataProvider<ModelType, Void, StorageFilter>,
    private val dcs: DatacenterService,
): VerticalLayout() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var filter = StorageFilter()

    private val defaultAmend:(StorageFilter) -> Unit = {
        provider.setFilter(it)
        logger.info(it.toString())
    }

    /* UI */
    private val rowOne = FlexLayout()

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
        }

        rowOne.add(fromField, toField)

    }

    /* Datacenters */
    private lateinit var dcsFeld: MultiSelectComboBox<Datacenter>

    fun makeDcs() {
        dcsFeld = MultiSelectComboBox<Datacenter>().apply {
            setItems(dcs.findAll())
            itemLabelGenerator = ItemLabelGenerator { it.fullName.replace("DC ","") }

            addValueChangeListener {
                filter.dcs.clear()
                filter.dcs.addAll(it.value.map(Datacenter::short))
                defaultAmend.invoke(filter)
            }
        }

        rowOne.add(dcsFeld)
    }

    init {
        add( rowOne )
    }
}