package it.posteitaliane.gdc.gmd.views.storage

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Storage
import it.posteitaliane.gdc.gmd.services.DatacenterService
import it.posteitaliane.gdc.gmd.services.StorageService
import it.posteitaliane.gdc.gmd.views.MainLayout
import jakarta.annotation.security.PermitAll
import org.slf4j.Logger

@PermitAll
@Route(value = "", layout = MainLayout::class)
class StorageView(
    ss:StorageService,
    dcs:DatacenterService,
    private val logger:Logger
) : VerticalLayout() {
    fun refresh() {
        grid.dataProvider.refreshAll()
    }

    private val provider: StorageProvider

    private val filterProvider:ConfigurableFilterDataProvider<Storage, Void, StorageFilter>

    val grid: Grid<Storage>

    private val itemField: MultiSelectComboBox<String>
    private val clearItemButton: Button

    private val positionField: MultiSelectComboBox<String>
    private val clearPosButton: Button

    private val searchField:TextField

    private val dcSelect: CheckboxGroup<Datacenter>

    private val otherDcSelect: CheckboxGroup<Datacenter>

    private var storageFilter: StorageFilter

    init {
        setHeightFull()
        provider = StorageProvider(ss)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Storage::class.java, false)
        grid.addColumn({"${it.dc.short} - ${it.dc.fullName}"}, "dc")
            .setHeader("Datacenter")
        val itemColumn = grid.addColumn("item")
            .setHeader("Merce")
        grid.addColumn("pos")
            .setHeader("Position")
        grid.addColumn("amount")
            .setHeader("Quantità")
        grid.addColumn("sn")
            .setHeader("S/N")
        grid.addColumn("pt")

        grid.setItems(filterProvider)

        grid.sort(mutableListOf(GridSortOrder(itemColumn, SortDirection.ASCENDING)))

        storageFilter = StorageFilter()

        itemField = MultiSelectComboBox<String>().apply {
            placeholder = "MERCE"
            isSelectedItemsOnTop = true
            setWidth(20F, Unit.REM)
            setItems(ss.findAllItems())
        }

        positionField = MultiSelectComboBox<String>().apply {
            placeholder = "POSIZIONE"

            val dcs = storageFilter.dcs.union(storageFilter.others).toList()

            setItems(ss.findItemsFromStorage(dcs = dcs))
        }

        searchField = TextField()
            .apply {
                prefixComponent = Icon(VaadinIcon.SEARCH)
                placeholder = "Cerca per nome merce"
                width = "50%"
                classNames.add("search")
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
            storageFilter.items.clear()
            storageFilter.items.addAll( event.value )
            filterProvider.setFilter(storageFilter)
        }

        positionField.addValueChangeListener { event ->
            storageFilter.positions.clear()
            storageFilter.positions.addAll( ss.findItemsFromStorage(event.value.toList()) )
            filterProvider.setFilter(storageFilter)
        }

        searchField.addKeyUpListener {
            if( it.key == Key.ENTER ) {
                storageFilter.key = searchField.value.lowercase().trim()
                filterProvider.setFilter(storageFilter)
            }

            if( it.key.toString() == "Escape" || it.key.toString() == "Delete" ) {
                searchField.clear()
                storageFilter.key = null
                filterProvider.setFilter(storageFilter)
            }
        }

        dcSelect.addValueChangeListener {
            storageFilter.dcs.clear()
            if(  it.value.size != 0 ) storageFilter.dcs.addAll(it.value.map(Datacenter::short))

            val dcs = storageFilter.dcs.union(
                if ( storageFilter.showOthers ) storageFilter.others.toList()
                    else emptySet()
            ).toList()
            positionField.setItems( ss.findItemsFromStorage(dcs) )
            filterProvider.setFilter(storageFilter)
        }

        otherDcSelect.addValueChangeListener {
            storageFilter.others.clear()
            if(  it.value.size != 0 ) storageFilter.others.addAll(it.value.map(Datacenter::short))

            val dcs = storageFilter.dcs.union(
                if ( storageFilter.showOthers ) storageFilter.others.toList()
                        else emptySet()
            ).toList()
            positionField.setItems( ss.findItemsFromStorage(dcs) )
            filterProvider.setFilter(storageFilter)
        }

        val showAllCheck= Checkbox().apply {
            label = "MOSTRA  TUTTI"
            addValueChangeListener { event ->
                otherDcSelect.isVisible = event.value

                storageFilter.showOthers = event.value
                filterProvider.setFilter(storageFilter)
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

        val flex = fun (c: Component, b:Button): FlexLayout {
            return FlexLayout(c, b)
        }

        add(HorizontalLayout(flex(itemField, clearItemButton), flex(positionField,clearPosButton), dcSelect).apply { setWidthFull() })
        add(HorizontalLayout(showAllCheck, otherDcSelect).apply { setWidthFull() })
        add(grid)
    }
}