package com.krupatek.courier.view.placegeneration;

import com.krupatek.courier.Constants;
import com.krupatek.courier.model.PlaceGeneration;
import com.krupatek.courier.service.PlaceGenerationService;
import com.krupatek.courier.service.StateService;
import com.krupatek.courier.service.ZonesService;
import com.krupatek.courier.view.accountcopy.AccountCopyEditor;
import com.krupatek.courier.view.clientprofile.ClientProfileEditor;
import com.krupatek.courier.view.rate.PlaceGenerationForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.data.domain.Page;

import java.util.logging.Logger;

public class PlaceGenerationEditor extends Div {
    private final int PAGE_SIZE = 500;
    private String placeNameFilter = "";

    public PlaceGenerationEditor(ZonesService zonesService,
                                 PlaceGenerationService placeGenerationService,
                                 StateService stateService){
        super();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(false);
        verticalLayout.setPadding(false);

        H4 title = new H4();
        title.setSizeFull();
        title.setText("Edit Client Profile");

        TextField clientName = new TextField();
        clientName.setPlaceholder("Filter by Client Name");
        clientName.setValueChangeMode(ValueChangeMode.LAZY);
        clientName.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);

        Grid<PlaceGeneration> placeGenerationGrid = new Grid<>(PlaceGeneration.class);
        placeGenerationGrid.setPageSize(PAGE_SIZE);
        placeGenerationGrid.setColumns("cityName", "stateName", "placeCode");

        placeGenerationGrid.getColumnByKey("cityName").setWidth("30%").setFlexGrow(0);
        placeGenerationGrid.getColumnByKey("stateName").setWidth("30%").setFlexGrow(0);
        placeGenerationGrid.getColumnByKey("placeCode").setWidth("30%").setFlexGrow(0);

        HeaderRow hr = placeGenerationGrid.prependHeaderRow();
        hr.getCell(placeGenerationGrid.getColumnByKey("cityName")).setComponent(clientName);

        placeGenerationGrid.setColumnReorderingAllowed(false);
        CallbackDataProvider.FetchCallback<PlaceGeneration, Void> placeGenerationVoidFetchCallback = query -> {
            // The index of the first item to load
            int offset = query.getOffset();

            // The number of items to load
            int limit = query.getLimit();

            Logger.getLogger(PlaceGenerationEditor.class.getName()).info("offset : "+offset+", limit :"+limit);

            offset = offset/PAGE_SIZE;
            limit = PAGE_SIZE;

            Logger.getLogger(AccountCopyEditor.class.getName()).info("Corrected offset : " + offset + ", limit :" + limit);

            Logger.getLogger(ClientProfileEditor.class.getName()).info("Filter is "+placeNameFilter);

            Page<PlaceGeneration> placeGenerations = placeGenerationService
                    .findByCityNameStartsWithOrderByCityName(offset, limit, placeNameFilter);
            Logger.getLogger(ClientProfileEditor.class.getName()).info("pages: "+placeGenerations.getNumber());
            Logger.getLogger(ClientProfileEditor.class.getName()).info("numberOfElements : "+placeGenerations.getNumberOfElements());
            Logger.getLogger(ClientProfileEditor.class.getName()).info("size : "+placeGenerations.getSize());
            Logger.getLogger(ClientProfileEditor.class.getName()).info("totalElements : "+placeGenerations.getTotalElements());
            Logger.getLogger(ClientProfileEditor.class.getName()).info("totalPages : "+placeGenerations.getTotalPages());
            return placeGenerations.stream();
        };
        placeGenerationGrid.setItems(placeGenerationVoidFetchCallback);
        clientName.addValueChangeListener(event -> {
            placeNameFilter = event.getValue();
            placeGenerationGrid.setItems(placeGenerationVoidFetchCallback);
        });

        placeGenerationGrid.addItemClickListener(listener -> {
            // TODO Connect this bean with Editor in @PlaceGenerationForm.class
            PlaceGenerationForm placeGenerationForm =  new PlaceGenerationForm(
                    zonesService,
                    placeGenerationService,
                    stateService,
                    listener.getItem());
            add(placeGenerationForm);
        });

        Button addNewBtn = new Button("New Client", VaadinIcon.PLUS.create());
        addNewBtn.setWidth("12.5%");
        addNewBtn.addClickListener(e -> add(new PlaceGenerationForm(zonesService,
                placeGenerationService,
                stateService,
                new PlaceGeneration())));

        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshBtn.setWidth("12.5%");
        refreshBtn.addClickListener( e -> {
            placeNameFilter = "";
            placeGenerationGrid.setItems(placeGenerationVoidFetchCallback);
        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidth("100%");
        actions.setAlignItems(HorizontalLayout.Alignment.END);
        actions.add(addNewBtn, refreshBtn);

        verticalLayout.add(title , actions, placeGenerationGrid);

        add(verticalLayout);
    }
}
