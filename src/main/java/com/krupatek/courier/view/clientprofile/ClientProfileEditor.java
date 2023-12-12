package com.krupatek.courier.view.clientprofile;

import com.krupatek.courier.Constants;
import com.krupatek.courier.model.Client;
import com.krupatek.courier.service.ClientService;
import com.krupatek.courier.view.accountcopy.AccountCopyEditor;
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

public class ClientProfileEditor extends Div {

    private final int PAGE_SIZE = 500;
    private String clientNameFilter = "";
    public  ClientProfileEditor(ClientService clientService){
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
        Grid<Client> clientGrid = new Grid<>(Client.class);
        clientGrid.setPageSize(PAGE_SIZE);
        clientGrid.setColumns("clientName", "city", "phone", "branch_name", "gstNo", "gstEnabled", "fsc", "enabled");

        clientGrid.getColumnByKey("clientName").setWidth("28%").setFlexGrow(0);
        clientGrid.getColumnByKey("city").setWidth("10%").setFlexGrow(0);
        clientGrid.getColumnByKey("phone").setWidth("8%").setFlexGrow(0);
        clientGrid.getColumnByKey("branch_name").setWidth("6%").setFlexGrow(0);
        clientGrid.getColumnByKey("gstNo").setWidth("13%").setFlexGrow(0);
        clientGrid.getColumnByKey("gstEnabled").setWidth("10%").setFlexGrow(0);
        clientGrid.getColumnByKey("fsc").setWidth("10%").setFlexGrow(0);
        clientGrid.getColumnByKey("enabled").setWidth("10%").setFlexGrow(0);

        HeaderRow hr = clientGrid.prependHeaderRow();
        hr.getCell(clientGrid.getColumnByKey("clientName")).setComponent(clientName);

        clientGrid.setColumnReorderingAllowed(false);

//        DataProvider<Client, String> dataProvider =
//                DataProvider.fromFilteringCallbacks(
//                        // First callback fetches items based on a query
//                        query -> {
//                            // The index of the first item to load
//                            int offset = query.getOffset();
//
//                            // The number of items to load
//                            int limit = query.getLimit();
//
//                            Logger.getLogger(ClientProfileEditor.class.getName()).info("offset : "+offset+", limit :"+limit);
//
//                            offset = offset/PAGE_SIZE;
//                            limit = PAGE_SIZE;
//
//                            Logger.getLogger(AccountCopyEditor.class.getName()).info("Corrected offset : " + offset + ", limit :" + limit);
//
//                            String clientNameFilter = query.getFilter().orElse("");
//
//                            Logger.getLogger(ClientProfileEditor.class.getName()).info("Filter is "+clientNameFilter);
//
//                            Page<Client> accountCopies = clientService
//                                    .findByClientNameStartsWithOrderByClientName(offset, limit, clientNameFilter);
//                            Logger.getLogger(ClientProfileEditor.class.getName()).info("pages: "+accountCopies.getNumber());
//                            Logger.getLogger(ClientProfileEditor.class.getName()).info("numberOfElements : "+accountCopies.getNumberOfElements());
//                            Logger.getLogger(ClientProfileEditor.class.getName()).info("size : "+accountCopies.getSize());
//                            Logger.getLogger(ClientProfileEditor.class.getName()).info("totalElements : "+accountCopies.getTotalElements());
//                            Logger.getLogger(ClientProfileEditor.class.getName()).info("totalPages : "+accountCopies.getTotalPages());
//                            return accountCopies.stream();
//                        },
//                        // Second callback fetches the number of items
//                        // for a query
//                        query -> {
//                            // The index of the first item to load
//                            int offset = query.getOffset();
//
//                            // The number of items to load
//                            int limit = query.getLimit();
//
//                            String clientNameFilter = query.getFilter().orElse("");
//
//                            return Math.toIntExact(clientService.countByClientNameStartsWith(clientNameFilter));
//                        });
//
//        ConfigurableFilterDataProvider<Client, Void, String> wrapper =
//                dataProvider.withConfigurableFilter();
//        wrapper.setFilter(clientNameFilter);
//        clientGrid.setItems(wrapper);

        CallbackDataProvider.FetchCallback<Client, Void> clientVoidFetchCallback = query -> {
                // The index of the first item to load
                int offset = query.getOffset();

                // The number of items to load
                int limit = query.getLimit();

                Logger.getLogger(ClientProfileEditor.class.getName()).info("offset : "+offset+", limit :"+limit);

                offset = offset/PAGE_SIZE;
                limit = PAGE_SIZE;

                Logger.getLogger(AccountCopyEditor.class.getName()).info("Corrected offset : " + offset + ", limit :" + limit);

                Logger.getLogger(ClientProfileEditor.class.getName()).info("Filter is "+clientNameFilter);

                Page<Client> accountCopies = clientService
                        .findByClientNameStartsWithOrderByClientName(offset, limit, clientNameFilter);
                Logger.getLogger(ClientProfileEditor.class.getName()).info("pages: "+accountCopies.getNumber());
                Logger.getLogger(ClientProfileEditor.class.getName()).info("numberOfElements : "+accountCopies.getNumberOfElements());
                Logger.getLogger(ClientProfileEditor.class.getName()).info("size : "+accountCopies.getSize());
                Logger.getLogger(ClientProfileEditor.class.getName()).info("totalElements : "+accountCopies.getTotalElements());
                Logger.getLogger(ClientProfileEditor.class.getName()).info("totalPages : "+accountCopies.getTotalPages());
                return accountCopies.stream();
        };
        clientGrid.setItems(clientVoidFetchCallback);
        clientName.addValueChangeListener(event -> {
            clientNameFilter = event.getValue();
            clientGrid.setItems(clientVoidFetchCallback);
        });


        clientGrid.addItemClickListener(listener -> {
            ClientProfileForm clientProfileForm =  new ClientProfileForm(
                    clientService,
                    listener.getItem());
            add(clientProfileForm);
        });


        Button addNewBtn = new Button("New Client", VaadinIcon.PLUS.create());
        addNewBtn.setWidth("12.5%");
        addNewBtn.addClickListener(e -> add(new ClientProfileForm(clientService, new Client())));

        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshBtn.setWidth("12.5%");
        refreshBtn.addClickListener( e -> {
            clientNameFilter = "";
            clientGrid.setItems(clientVoidFetchCallback);
        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidth("100%");
        actions.setAlignItems(HorizontalLayout.Alignment.END);
        actions.add(addNewBtn, refreshBtn);

        verticalLayout.add(title , actions, clientGrid);

        add(verticalLayout);
    }
}
