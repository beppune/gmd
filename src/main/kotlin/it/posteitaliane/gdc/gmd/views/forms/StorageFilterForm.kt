package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.Unit as VUnit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Storage
import it.posteitaliane.gdc.gmd.services.DatacenterService
import it.posteitaliane.gdc.gmd.services.StorageService
import it.posteitaliane.gdc.gmd.views.storage.StorageFilter

class StorageFilterForm(
    private var provider: ConfigurableFilterDataProvider<Storage, Void, StorageFilter>,
    private val dcs: DatacenterService,
    private val ss: StorageService
): VerticalLayout() {

    private val itemField: MultiSelectComboBox<String>
    private val clearItemButton: Button

    private val positionField: MultiSelectComboBox<String>
    private val clearPosButton: Button

    private val dcSelect: CheckboxGroup<Datacenter>

    private val otherDcSelect: CheckboxGroup<Datacenter>

    private var filter = StorageFilter()

    private val defaultCallBack: (StorageFilter) -> Unit = { provider.setFilter(filter) }
    var onItemChange: (StorageFilter) -> Unit = defaultCallBack
    var onPositionChange: (StorageFilter) -> Unit = defaultCallBack
    var onDcChange: (StorageFilter) -> Unit = defaultCallBack
    var onOtherChange: (StorageFilter) -> Unit = defaultCallBack
    var onShowAll: (StorageFilter) -> Unit = defaultCallBack

    init {
        itemField = MultiSelectComboBox<String>().apply {
            placeholder = "MERCE"
            isSelectedItemsOnTop = true
            setWidth(20F, VUnit.REM)
            setItems(ss.findAllItems())
        }

        positionField = MultiSelectComboBox<String>().apply {
            placeholder = "POSIZIONE"

            val dcs = filter.dcs.union(filter.others).toList()

            setItems(ss.findPosFromStorage(dcs = dcs))
        }

        dcSelect = CheckboxGroup<Datacenter>().apply {
            setItems(dcs.findAll())
            setItemLabelGenerator { it.short }
        }

        otherDcSelect = CheckboxGroup<Datacenter>().apply {
            isVisible = false
            setItems(dcs.findOthers())
            setItemLabelGenerator { it.short }
        }

        itemField.addValueChangeListener { event ->
            filter.items.clear()
            filter.items.addAll( event.value )

            onItemChange.invoke(filter)
        }

        positionField.addValueChangeListener { event ->
            filter.positions.clear()
            filter.positions.addAll( event.value )

            onPositionChange.invoke(filter)
        }

        dcSelect.addValueChangeListener {
            filter.dcs.clear()
            if(  it.value.size != 0 ) filter.dcs.addAll(it.value.map(Datacenter::short))

            val dcs = filter.dcs.union(
                if ( filter.showOthers ) filter.others.toList()
                else emptySet()
            ).toList()
            positionField.setItems( ss.findPosFromStorage(dcs) )

            onDcChange.invoke(filter)
        }

        otherDcSelect.addValueChangeListener {
            filter.others.clear()
            if(  it.value.size != 0 ) filter.others.addAll(it.value.map(Datacenter::short))

            val dcs = filter.dcs.union(
                if ( filter.showOthers ) filter.others.toList()
                else emptySet()
            ).toList()
            positionField.setItems( ss.findPosFromStorage(dcs) )

            onOtherChange.invoke(filter)
        }

        val showAllCheck= Checkbox().apply {
            label = "MOSTRA  TUTTI"
            addValueChangeListener { event ->
                otherDcSelect.isVisible = event.value

                filter.showOthers = event.value

                onShowAll.invoke(filter)
            }
        }

        clearItemButton = Button().apply {
            icon = Icon(VaadinIcon.CLOSE_CIRCLE)
            isVisible = false
            addClassNames(LumoUtility.Background.ERROR_50, LumoUtility.TextColor.WARNING_CONTRAST)
            addClickListener {
                itemField.clear()
            }

            itemField.addValueChangeListener { event ->
                this.isVisible = event.value.toList().isNotEmpty()
            }
        }

        clearPosButton = Button().apply {
            icon = Icon(VaadinIcon.CLOSE_CIRCLE)
            isVisible = false
            addClassNames(LumoUtility.Background.ERROR_50, LumoUtility.TextColor.WARNING_CONTRAST)
            addClickListener {
                positionField.clear()
            }

            positionField.addValueChangeListener { event ->
                this.isVisible = event.value.toList().isNotEmpty()
            }
        }

        add(HorizontalLayout(
            FlexLayout(itemField, clearItemButton),
            FlexLayout(positionField, clearPosButton),
            dcSelect
        ).apply { setWidthFull() })

        add(HorizontalLayout(
            FlexLayout(showAllCheck, otherDcSelect),
        ).apply { setWidthFull() })
    }
}