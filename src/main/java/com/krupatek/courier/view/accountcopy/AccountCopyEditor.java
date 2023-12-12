package com.krupatek.courier.view.accountcopy;

import com.krupatek.courier.Constants;
import com.krupatek.courier.model.AccountCopy;
import com.krupatek.courier.service.*;
import com.krupatek.courier.utils.DateUtils;
import com.krupatek.courier.utils.NumberUtils;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.data.domain.Page;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@SpringComponent
@UIScope
public class AccountCopyEditor extends Div {
    private String docNoFilter = "";
    private String clientNameFilter = "";
    private Date dateFilter;
    private final int PAGE_SIZE = 1000;
    private TextField totalDocNoTF;
    private TextField grossTotalTF;

    public AccountCopyEditor(
            AccountCopyService accountCopyService,
            ClientService clientService,
            RateMasterService rateMasterService,
            RateIntMasterService rateIntMasterService,
            PlaceGenerationService placeGenerationService,
            NetworkService networkService,
            DateUtils dateUtils,
            NumberUtils numberUtils,
            CourierService courierService,
            BillingService billingService) {
        super();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setMargin(false);
        verticalLayout.setPadding(false);

        H4 title = new H4();
        title.setSizeFull();
        title.setText("Edit Account Copy");

        TextField docNo = new TextField();
        docNo.setPlaceholder("Filter by Doc No");
        docNo.setValueChangeMode(ValueChangeMode.LAZY);
        docNo.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);

        TextField podDate = new TextField();
        podDate.setPlaceholder("Filter by Date");
        podDate.setValueChangeMode(ValueChangeMode.LAZY);
        podDate.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);

        TextField clientName = new TextField();
        clientName.setPlaceholder("Filter by Client Name");
        clientName.setValueChangeMode(ValueChangeMode.LAZY);
        clientName.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);

        Grid<AccountCopy> accountCopyGrid = new Grid<>(AccountCopy.class, false);
        accountCopyGrid.setPageSize(PAGE_SIZE);

        accountCopyGrid.addColumn(AccountCopy::getDocNo).setKey("srNo").setHeader(new Html("<b>Sr No</b>")).setWidth("8%").setFlexGrow(0);;
        accountCopyGrid.addColumn(AccountCopy::getDocNo).setKey("docNo");
        accountCopyGrid.addColumn(accountCopy -> dateUtils.ddmmyyFormat(accountCopy.getPodDate())).setKey("podDate");
        accountCopyGrid.addColumn(AccountCopy::getClientName).setKey("clientName");
        accountCopyGrid.addColumn(AccountCopy::getDestination).setKey("destination");
        accountCopyGrid.addColumn(AccountCopy::getWeight).setKey("weight");
        accountCopyGrid.addColumn(AccountCopy::getOtherCharges).setKey("otherCharges");
        accountCopyGrid.addColumn(AccountCopy::getRate).setKey("rate");
        accountCopyGrid.addColumn(AccountCopy::getdP).setKey("dP");
        accountCopyGrid.addColumn(AccountCopy::getMode).setKey("mode");

        accountCopyGrid.getColumnByKey("docNo").setHeader(new Html("<b>Doc No</b>")).setWidth("10%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("podDate").setHeader(new Html("<b>POD Date</b>")).setWidth("10%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("clientName").setHeader(new Html("<b>Client Name</b>")).setWidth("25%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("destination").setHeader(new Html("<b>Destination</b>")).setWidth("10%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("weight").setHeader(new Html("<b>Weight</b>")).setWidth("8%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("otherCharges").setHeader(new Html("<b>Other Charges</b>")).setWidth("8%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("rate").setHeader(new Html("<b>Rate</b>")).setWidth("10%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("dP").setHeader(new Html("<b>D/P</b>")).setWidth("6%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("mode").setHeader(new Html("<b>Mode</b>")).setWidth("6%").setFlexGrow(0);

        accountCopyGrid.getColumnByKey("srNo").getElement().executeJs("this.renderer = function(root, column, rowData) {root.textContent = rowData.index + 1}");

        accountCopyGrid.getColumnByKey("docNo").setTextAlign(ColumnTextAlign.END);
        accountCopyGrid.getColumnByKey("weight").setTextAlign(ColumnTextAlign.END);
        accountCopyGrid.getColumnByKey("otherCharges").setTextAlign(ColumnTextAlign.END);
        accountCopyGrid.getColumnByKey("rate").setTextAlign(ColumnTextAlign.END);

        HeaderRow hr = accountCopyGrid.prependHeaderRow();
        hr.getCell(accountCopyGrid.getColumnByKey("docNo")).setComponent(docNo);
        hr.getCell(accountCopyGrid.getColumnByKey("podDate")).setComponent(podDate);
        hr.getCell(accountCopyGrid.getColumnByKey("clientName")).setComponent(clientName);

        accountCopyGrid.setColumnReorderingAllowed(false);

        CallbackDataProvider.FetchCallback<AccountCopy, Void> accountCopyAccountCopyFilterFetchCallback = query -> {
            // The index of the first item to load
            int offset = query.getOffset();

            // The number of items to load
            int limit = query.getLimit();

            Logger.getLogger(AccountCopyEditor.class.getName()).info("Original offset : " + offset + ", limit :" + limit);

            offset = offset / PAGE_SIZE;
            limit = Math.min(limit, PAGE_SIZE);

            Logger.getLogger(AccountCopyEditor.class.getName()).info("Corrected offset : " + offset + ", limit :" + limit);
            Page<AccountCopy> accountCopies = dateFilter != null ?
                    accountCopyService
                            .findByDocNoStartsWithAndClientNameStartsWithAndPodDate(offset, limit, docNoFilter, clientNameFilter, dateFilter) :
                    accountCopyService
                            .findByDocNoStartsWithAndClientNameStartsWith(offset, limit, docNoFilter, clientNameFilter);
            Logger.getLogger(AccountCopyEditor.class.getName()).info("Total pages : " + accountCopies.getTotalElements());
            return accountCopies.stream();
        };
//        DataProvider<AccountCopy, Void> dataProvider =
//                DataProvider.fromFilteringCallbacks(
//                        // First callback fetches items based on a query
//                        accountCopyAccountCopyFilterFetchCallback,
//                        // Second callback fetches the number of items
//                        // for a query
//                        query -> {
//                            String docNoFilter = accountCopyFilter.getDocNoFilter();
//                            String clientNameFilter = accountCopyFilter.getClientNameFilter();
//                            Date dateFilter = accountCopyFilter.getDateFilter();
//                            Integer totalDocNo = Math.toIntExact(
//                                    dateFilter != null ?
//                                            accountCopyService.countByDocNoStartsWithAndClientNameStartsWithAndPodDate(docNoFilter, clientNameFilter, dateFilter) :
//                                            accountCopyService.countByDocNoStartsWithAndClientNameStartsWith(docNoFilter, clientNameFilter));
//                            totalDocNoTF.setValue(String.valueOf(totalDocNo));
//
//
//                            try {
//                                Integer totalRate = Math.toIntExact(
//                                        dateFilter != null ?
//                                                accountCopyService.totalByDocNoStartsWithAndClientNameStartsWithAndPodDate(docNoFilter, clientNameFilter, dateFilter) :
//                                                accountCopyService.totalByDocNoStartsWithAndClientNameStartsWith(docNoFilter, clientNameFilter));
//                                grossTotalTF.setValue(String.format("%.02f", totalRate.floatValue()));
//                            } catch (AopInvocationException e){
//                                grossTotalTF.setValue(String.format("%.02f", 0f));
//                            }
//                            return totalDocNo;
//                        });
//
//        filter = new AccountCopyFilter();
//        ConfigurableFilterDataProvider<AccountCopy, Void, AccountCopyFilter> wrapper =
//                dataProvider.withConfigurableFilter();
//        wrapper.setFilter(filter);
//        accountCopyGrid.setDataProvider(wrapper);
        accountCopyGrid.setItems(accountCopyAccountCopyFilterFetchCallback);
        docNo.addValueChangeListener(event -> {
            docNoFilter = event.getValue();
            accountCopyGrid.setItems(accountCopyAccountCopyFilterFetchCallback);
        });

        clientName.addValueChangeListener(event -> {
            clientNameFilter = event.getValue();
            accountCopyGrid.setItems(accountCopyAccountCopyFilterFetchCallback);
        });

        podDate.addValueChangeListener(event -> {
            try {
                dateFilter = new SimpleDateFormat("dd/MM/yy").parse(event.getValue());
                accountCopyGrid.setItems(accountCopyAccountCopyFilterFetchCallback);
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            wrapper.refreshAll();
        });

        accountCopyGrid.asSingleSelect().addValueChangeListener(gridAccountCopyComponentValueChangeEvent -> {
            accountCopyGrid.select(gridAccountCopyComponentValueChangeEvent.getValue());
        });

        accountCopyGrid.addSelectionListener(selectionEvent -> {
            if(selectionEvent.isFromClient() && selectionEvent.getFirstSelectedItem().isPresent()) {
            AccountCopyForm accountCopyForm = new AccountCopyForm(
                    accountCopyService,
                    clientService,
                    rateMasterService,
                    rateIntMasterService,
                    placeGenerationService,
                    networkService,
                    dateUtils,
                    numberUtils,
                    selectionEvent.getFirstSelectedItem().get(),
                    courierService,
                    billingService);
                add(accountCopyForm);
            }
        });

        // Panel 1
        HorizontalLayout invoiceDateHorizontalLayout = new HorizontalLayout();
        invoiceDateHorizontalLayout.setAlignItems(HorizontalLayout.Alignment.END);
        invoiceDateHorizontalLayout.setMargin(false);
        invoiceDateHorizontalLayout.setPadding(false);
        invoiceDateHorizontalLayout.setWidth("100%");

        Button addNewBtn = new Button("New Account Copy", VaadinIcon.PLUS.create());
        addNewBtn.setWidth("15%");
        addNewBtn.addClickListener(e -> add(new AccountCopyForm(
                accountCopyService,
                clientService,
                rateMasterService,
                rateIntMasterService,
                placeGenerationService,
                networkService,
                dateUtils,
                numberUtils,
                new AccountCopy(),
                courierService,
                billingService)));

        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshBtn.setWidth("12.5%");
        refreshBtn.addClickListener( e -> {
            accountCopyGrid.setItems(accountCopyAccountCopyFilterFetchCallback);
        });

        Button resetBtn = new Button("Reset");
        resetBtn.setWidth("12.5%");
        resetBtn.addClickListener( e -> {
            clientNameFilter = "";
            docNoFilter = "";
            dateFilter = null;

            docNo.setValue("");
            podDate.setValue("");
            clientName.setValue("");

            accountCopyGrid.setItems(accountCopyAccountCopyFilterFetchCallback);
        });

        invoiceDateHorizontalLayout.add(addNewBtn, refreshBtn, resetBtn);

        // Footer
        Label totalDocNoLbl = new Label();
        totalDocNoLbl.setWidth("5%");
        totalDocNoLbl.addClassName("bold-label");
        totalDocNoLbl.setText("Total : ");

        totalDocNoTF = new TextField();
        totalDocNoTF.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        totalDocNoTF.setWidth("5%");
        totalDocNoTF.addClassName("bold-label");
        totalDocNoTF.setReadOnly(true);

        Label leftEmptyLabelGrossTotalFooterHLayout = new Label();
        leftEmptyLabelGrossTotalFooterHLayout.setWidth("53%");

        Label grossTotalLbl = new Label();
        grossTotalLbl.setText("Amount : ");
        grossTotalLbl.setWidth("11%");

        grossTotalTF = new TextField();
        grossTotalTF.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        grossTotalTF.setWidth("10%");
        grossTotalTF.setReadOnly(true);

        Label rightEmptyLabelGrossTotalFooterHLayout = new Label();
        rightEmptyLabelGrossTotalFooterHLayout.setWidth("16%");


        HorizontalLayout grossTotalFooterHLayout = new HorizontalLayout();
        grossTotalFooterHLayout.setMargin(false);
        grossTotalFooterHLayout.setPadding(false);
        grossTotalFooterHLayout.setSpacing(false);
        grossTotalFooterHLayout.setWidth("100%");


        grossTotalFooterHLayout.add(totalDocNoLbl, totalDocNoTF, leftEmptyLabelGrossTotalFooterHLayout, grossTotalLbl, grossTotalTF, rightEmptyLabelGrossTotalFooterHLayout);


        verticalLayout.add(title, invoiceDateHorizontalLayout, accountCopyGrid, grossTotalFooterHLayout);

        add(verticalLayout);
    }

}
