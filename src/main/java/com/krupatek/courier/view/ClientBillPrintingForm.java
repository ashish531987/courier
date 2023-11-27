package com.krupatek.courier.view;

import com.krupatek.courier.model.AccountCopy;
import com.krupatek.courier.model.BillGeneration;
import com.krupatek.courier.model.Client;
import com.krupatek.courier.model.Company;
import com.krupatek.courier.repository.CompanyRepository;
import com.krupatek.courier.service.*;
import com.krupatek.courier.utils.DateUtils;
import com.krupatek.courier.utils.NumberUtils;
import com.krupatek.courier.view.accountcopy.AccountCopyForm;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ClientBillPrintingForm extends Div {
    private String currentSelectedItem;
    private boolean isDomestic = true; // False means International.
    private boolean isGSTEnabled = true;
    private boolean isFuelSurchargeEnabled = true;
    private List<AccountCopy> allByClientNameAndPodDateBetween = new ArrayList<>();

    public ClientBillPrintingForm(
            AccountCopyService accountCopyService,
            ClientService clientService,
            RateMasterService rateMasterService,
            RateIntMasterService rateIntMasterService,
            PlaceGenerationService placeGenerationService,
            NetworkService networkService,
            InvoiceService invoiceService,
            CompanyRepository companyRepository,
            BillingService billingService,
            DateUtils dateUtils,
            NumberUtils numberUtils,
            CourierService courierService) {
        super();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(false);
        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        verticalLayout.setSizeFull();

        H4 title = new H4();
        title.setText("Bill Generation");
        title.setSizeFull();

        // Panel 1
        HorizontalLayout invoiceDateHorizontalLayout = new HorizontalLayout();
        invoiceDateHorizontalLayout.setAlignItems(HorizontalLayout.Alignment.END);
        invoiceDateHorizontalLayout.setMargin(false);
        invoiceDateHorizontalLayout.setPadding(false);
        invoiceDateHorizontalLayout.setWidth("100%");

        // Booking Type
        Select<String> bookingTypeSelect = new Select<>();
        bookingTypeSelect.setLabel("Booking Type : ");
        bookingTypeSelect.setItems("Dom", "Inter");
        bookingTypeSelect.setValue("Dom");
        bookingTypeSelect.setWidth("10%");

        // Client selection
        Select<String> clientSelect = new Select<>();
        clientSelect.setLabel("Select Client Name : ");
        clientSelect.setWidth("25%");

        List<Client> clientList = clientService.findAll();
        List<String> clientNameList = new ArrayList<>();
        clientList.forEach(c -> clientNameList.add(c.getClientName()));
        currentSelectedItem = clientNameList.get(0);

        clientSelect.setItems(clientNameList);
        clientSelect.setValue(currentSelectedItem);

        // Invoice Date

        LocalDate currentDate = LocalDate.now();
        LocalDate start = currentDate.withDayOfMonth(1);
        LocalDate end = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

        DatePicker invoiceDatePicker = new DatePicker(currentDate);
        invoiceDatePicker.setLabel("Invoice Date : ");
        invoiceDatePicker.setWidth("15%");

        // Last Month button
        Button lastMonthButton = new Button("Last Month");
        lastMonthButton.setWidth("12.5%");

        // Current Month button
        Button currentMonthButton = new Button("Current Month");
        currentMonthButton.setWidth("12.5%");

        // Refresh Button
        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.setWidth("12.5%");

        // Show Button
        Button showButton = new Button("Show");
        showButton.setWidth("12.5%");

        invoiceDateHorizontalLayout.add(bookingTypeSelect, clientSelect, invoiceDatePicker, lastMonthButton, currentMonthButton, refreshButton, showButton);


        // Panel 2


        HorizontalLayout dateHorizontalLayout = new HorizontalLayout();
        dateHorizontalLayout.setAlignItems(HorizontalLayout.Alignment.END);
        dateHorizontalLayout.setMargin(false);
        dateHorizontalLayout.setPadding(false);
        dateHorizontalLayout.setWidth("100%");


        DateFilter dateFilter = new DateFilter(start, end);

        Binder<DateFilter> binder = new Binder<>(DateFilter.class);


        // Start Date
        DatePicker startDatePicker = new DatePicker(start);
        startDatePicker.setLabel("From Date : ");
        binder.bind(startDatePicker, DateFilter::getStartDate, DateFilter::setStartDate);
        startDatePicker.setWidth("12.5%");
//        startDatePicker.setLocale(Locale.ENGLISH);

        // End Date
        DatePicker endDatePicker = new DatePicker(end);
        endDatePicker.setLabel("To Date : ");
        binder.bind(endDatePicker, DateFilter::getEndDate, DateFilter::setEndDate);
        endDatePicker.setWidth("12.5%");

        // CGST %
        TextField cgst = new TextField();
        cgst.setLabel("CGST in % : ");
        cgst.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        cgst.setValueChangeMode(ValueChangeMode.EAGER);
        cgst.setWidth("6%");

        // SGST %
        TextField sgst = new TextField();
        sgst.setLabel("SGST in % : ");
        sgst.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        sgst.setValueChangeMode(ValueChangeMode.EAGER);
        sgst.setWidth("6%");

        // IGST %
        TextField igst = new TextField();
        igst.setLabel("IGST in % : ");
        igst.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        igst.setValueChangeMode(ValueChangeMode.EAGER);
        igst.setWidth("6%");

        // Fuel Charge
        TextField fuelSurcharge = new TextField();
        fuelSurcharge.setLabel("Fuel Surcharge : ");
        fuelSurcharge.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        fuelSurcharge.setValueChangeMode(ValueChangeMode.EAGER);
        fuelSurcharge.setWidth("6%");

        HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate>> startDateChangeListener = event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (selectedDate != null) {
                dateFilter.setStartDate(selectedDate);
                endDatePicker.setMin(selectedDate.plusDays(1));
//                if (endDate == null) {
//                    endDatePicker.setOpened(true);
//                    showError("Select the ending date");
//                } else {
//                    showError(
//                            "Selected period:\nFrom " + selectedDate.toString()
//                                    + " to " + endDate.toString());
//                }
            } else {
                endDatePicker.setMin(null);
                showError("Select the starting date");
            }
        };
        startDatePicker.addValueChangeListener(startDateChangeListener);

        HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate>> endDateChangeListener = event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate startDate = startDatePicker.getValue();
            if (selectedDate != null) {
                dateFilter.setEndDate(selectedDate);
                startDatePicker.setMax(selectedDate.minusDays(1));
//                if (startDate != null) {
//                    showError(
//                            "Selected period:\nFrom " + startDate.toString()
//                                    + " to " + selectedDate.toString());
//                } else {
//                    showError("Select the starting date");
//                }
            } else {
                startDatePicker.setMax(null);
                if (startDate != null) {
                    showError("Select the ending date");
                } else {
                    showError("No date is selected");
                }
            }
        };
        endDatePicker.addValueChangeListener(endDateChangeListener);


        dateHorizontalLayout.add(startDatePicker, endDatePicker, cgst, sgst, igst, fuelSurcharge);

        Grid<AccountCopy> accountCopyGrid = new Grid<>(AccountCopy.class, false);
        accountCopyGrid.setWidthFull();

        accountCopyGrid.addColumn(accountCopy ->  (allByClientNameAndPodDateBetween.indexOf(accountCopy) + 1)).setHeader(new Html("<b>Sr No</b>")).setWidth("5%").setFlexGrow(0).setTextAlign(ColumnTextAlign.END);;
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
        accountCopyGrid.getColumnByKey("podDate").setHeader(new Html("<b>POD Date</b>")).setWidth("8%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("clientName").setHeader(new Html("<b>Client Name</b>")).setWidth("24%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("destination").setHeader(new Html("<b>Destination</b>")).setWidth("10%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("weight").setHeader(new Html("<b>Weight</b>")).setWidth("8%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("otherCharges").setHeader(new Html("<b>Other Charges</b>")).setWidth("11%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("rate").setHeader(new Html("<b>Rate</b>")).setWidth("10%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("dP").setHeader(new Html("<b>D/P</b>")).setWidth("6%").setFlexGrow(0);
        accountCopyGrid.getColumnByKey("mode").setHeader(new Html("<b>Mode</b>")).setWidth("8%").setFlexGrow(0);

        accountCopyGrid.getColumnByKey("docNo").setTextAlign(ColumnTextAlign.END);
        accountCopyGrid.getColumnByKey("weight").setTextAlign(ColumnTextAlign.END);
        accountCopyGrid.getColumnByKey("otherCharges").setTextAlign(ColumnTextAlign.END);
        accountCopyGrid.getColumnByKey("rate").setTextAlign(ColumnTextAlign.END);

        // Footer Gross Total

        Label totalDocNoLbl = new Label();
        totalDocNoLbl.setWidth("5%");
        totalDocNoLbl.addClassName("bold-label");
        totalDocNoLbl.setText("Total : ");

        TextField totalDocNoTF = new TextField();
        totalDocNoTF.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        totalDocNoTF.setWidth("5%");
        totalDocNoTF.addClassName("bold-label");
        totalDocNoTF.setReadOnly(true);

        Label leftEmptyLabelGrossTotalFooterHLayout = new Label();
        leftEmptyLabelGrossTotalFooterHLayout.setWidth("53%");

        Label grossTotalLbl = new Label();
        grossTotalLbl.setText("Gross Amount : ");
        grossTotalLbl.setWidth("11%");

        TextField grossTotalTF = new TextField();
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

        Label leftEmptyLabelNetTotalFooterHLayout = new Label();
        leftEmptyLabelNetTotalFooterHLayout.setWidth("63%");

        Label netTotalLbl = new Label();
        netTotalLbl.setText("Net Amount : ");
        netTotalLbl.setWidth("11%");

        TextField netTotalTF = new TextField();
        netTotalTF.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        netTotalTF.setWidth("10%");
        netTotalTF.setReadOnly(true);

        Label rightEmptyLabelNetTotalFooterHLayout = new Label();
        rightEmptyLabelNetTotalFooterHLayout.setWidth("16%");

        HorizontalLayout netTotalFooterHLayout = new HorizontalLayout();
        netTotalFooterHLayout.setMargin(false);
        netTotalFooterHLayout.setPadding(false);
        netTotalFooterHLayout.setSpacing(false);
        netTotalFooterHLayout.setWidth("100%");

        netTotalFooterHLayout.add(leftEmptyLabelNetTotalFooterHLayout, netTotalLbl, netTotalTF, rightEmptyLabelNetTotalFooterHLayout);

        accountCopyGrid.setColumnReorderingAllowed(false);

        Button previewInvoiceBtn = new Button("Preview Invoice", VaadinIcon.VIEWPORT.create());
        previewInvoiceBtn.setWidth("20%");
        previewInvoiceBtn.addClickListener( event -> {
            load(
                    accountCopyGrid,
                    accountCopyService,
                    dateFilter,
                    bookingTypeSelect,
                    grossTotalTF,
                    netTotalTF,
                    cgst,
                    sgst,
                    igst,
                    fuelSurcharge,
                    totalDocNoTF);
            handleInvoiceAction(
                    accountCopyService,
                    clientService,
                    invoiceService,
                    companyRepository,
                    billingService,
                    dateFilter,
                    invoiceDatePicker,
                    bookingTypeSelect,
                    startDatePicker,
                    endDatePicker,
                    cgst,
                    sgst,
                    igst,
                    fuelSurcharge,
                    grossTotalTF,
                    netTotalTF,
                    true);
        });

        Button generateInvoiceBtn = new Button("Generate Invoice", VaadinIcon.BAR_CHART.create());
        generateInvoiceBtn.setWidth("20%");
        generateInvoiceBtn.addClickListener( event -> {
            load(
                    accountCopyGrid,
                    accountCopyService,
                    dateFilter,
                    bookingTypeSelect,
                    grossTotalTF,
                    netTotalTF,
                    cgst,
                    sgst,
                    igst,
                    fuelSurcharge,
                    totalDocNoTF);
            handleInvoiceAction(
                    accountCopyService,
                    clientService,
                    invoiceService,
                    companyRepository,
                    billingService,
                    dateFilter,
                    invoiceDatePicker,
                    bookingTypeSelect,
                    startDatePicker,
                    endDatePicker,
                    cgst,
                    sgst,
                    igst,
                    fuelSurcharge,
                    grossTotalTF,
                    netTotalTF,
                    false);
        });


        dateHorizontalLayout.add(previewInvoiceBtn, generateInvoiceBtn);
        verticalLayout.add(title, invoiceDateHorizontalLayout, dateHorizontalLayout, accountCopyGrid, grossTotalFooterHLayout, netTotalFooterHLayout);
        add(verticalLayout);

        // Last Month
        lastMonthButton.addClickListener( c -> {
            LocalDate lastMonth = currentDate.minusMonths(1);
            dateFilter.setStartDate(lastMonth.withDayOfMonth(1));
            dateFilter.setEndDate(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
            binder.readBean(dateFilter);
            load(
                    accountCopyGrid,
                    accountCopyService,
                    dateFilter,
                    bookingTypeSelect,
                    grossTotalTF,
                    netTotalTF,
                    cgst,
                    sgst,
                    igst,
                    fuelSurcharge,
                    totalDocNoTF);
        });

        currentMonthButton.addClickListener( c -> {
            dateFilter.setStartDate(start);
            dateFilter.setEndDate(end);
            binder.readBean(dateFilter);
            load(
                    accountCopyGrid,
                    accountCopyService,
                    dateFilter,
                    bookingTypeSelect,
                    grossTotalTF,
                    netTotalTF,
                    cgst,
                    sgst,
                    igst,
                    fuelSurcharge,
                    totalDocNoTF);
        });

        refreshButton.addClickListener( c -> load(
                accountCopyGrid,
                accountCopyService,
                dateFilter,
                bookingTypeSelect,
                grossTotalTF,
                netTotalTF,
                cgst,
                sgst,
                igst,
                fuelSurcharge,
                totalDocNoTF));
        showButton.addClickListener( c -> load(
                accountCopyGrid,
                accountCopyService,
                dateFilter,
                bookingTypeSelect,
                grossTotalTF,
                netTotalTF,
                cgst,
                sgst,
                igst,
                fuelSurcharge,
                totalDocNoTF));

        bookingTypeSelect.addValueChangeListener(e -> {
            isDomestic = !e.getValue().equals("Inter");
            updateClientName(clientSelect, rateMasterService, rateIntMasterService);
        });

        clientSelect.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                currentSelectedItem = event.getValue();
                Company company = companyRepository.findAll().get(0);
                Client client = clientService.findAllByClientName(currentSelectedItem).get(0);
                isGSTEnabled = client.getGstEnabled().equalsIgnoreCase("Yes");
                isFuelSurchargeEnabled = client.getFsc().equalsIgnoreCase("Yes");
                updateTax(cgst, sgst, igst, fuelSurcharge, company);
                load(
                        accountCopyGrid,
                        accountCopyService,
                        dateFilter,
                        bookingTypeSelect,
                        grossTotalTF,
                        netTotalTF,
                        cgst,
                        sgst,
                        igst,
                        fuelSurcharge,
                        totalDocNoTF);
            }
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

        updateTax(cgst,sgst,igst, fuelSurcharge, companyRepository.findAll().get(0));
        binder.readBean(dateFilter);
    }

    private void handleInvoiceAction(
            AccountCopyService accountCopyService,
            ClientService clientService,
            InvoiceService invoiceService,
            CompanyRepository companyRepository,
            BillingService billingService,
            DateFilter dateFilter,
            DatePicker invoiceDatePicker,
            Select<String> bookingTypeSelect,
            DatePicker startDatePicker,
            DatePicker endDatePicker,
            TextField cgst,
            TextField sgst,
            TextField igst,
            TextField fuelSurcharge,
            TextField grossTotalTF,
            TextField netTotalTF,
            boolean onlyPreview) {
        String linkName = onlyPreview ? "Download Preview" : "Download Invoice";
        String fileName = onlyPreview ? "Invoice-Preview.pdf" : "Invoice.pdf";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");

        BillGeneration billGeneration = new BillGeneration();
        String billNo = billingService.nextBillNo();
        billGeneration.setBillNo(billNo);
        billGeneration.setStartDate(startDatePicker.getValue().format(formatter));
        billGeneration.setEndDate(endDatePicker.getValue().format(formatter));
        billGeneration.setBillDate(invoiceDatePicker.getValue().format(formatter));
        billGeneration.setBillYear(invoiceDatePicker.getValue().getYear());
        billGeneration.setBillAmount((int) Double.parseDouble(grossTotalTF.getValue()));
        billGeneration.setNetAmount((int) Double.parseDouble(netTotalTF.getValue()));
        billGeneration.setBalance((int) Double.parseDouble(netTotalTF.getValue()));
        billGeneration.setType(bookingTypeSelect.getValue());
        billGeneration.setClientName(currentSelectedItem);
        billGeneration.setBillStatus("C");
        billGeneration.setBillMonth(Integer.parseInt("" + invoiceDatePicker.getValue().getMonthValue() + invoiceDatePicker.getValue().getYear()));
        billGeneration.setCgst(Float.valueOf(cgst.getValue()));
        billGeneration.setSgst(Float.valueOf(sgst.getValue()));
        billGeneration.setIgst(Float.valueOf(igst.getValue()));
        billGeneration.setFuelSurcharge(Float.valueOf(fuelSurcharge.getValue()));
        String billSequence = billNo.split("/")[1]+String.format("%07d", Integer.parseInt(billNo.split("/")[2]));
        billGeneration.setBillSequence(billSequence);

        if (!onlyPreview) {
            billingService.saveAndFlush(billGeneration);

            accountCopyService.tagBillNo(
                    currentSelectedItem,
                    fromLocaleDate(dateFilter.getStartDate()),
                    fromLocaleDate(dateFilter.getEndDate()),
                    bookingTypeSelect.getValue(),
                    billGeneration.getBillNo() + " "
            );
        }

        Client client = clientService.findAllByClientName(currentSelectedItem).get(0);
        allByClientNameAndPodDateBetween.clear();
        allByClientNameAndPodDateBetween.addAll(accountCopyService.findAllByClientNameAndPodDateBetweenAndType(
                currentSelectedItem,
                fromLocaleDate(dateFilter.getStartDate()),
                fromLocaleDate(dateFilter.getEndDate()),
                bookingTypeSelect.getValue()));

        Company company = companyRepository.findAll().get(0);

        try {
            formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            billGeneration.setStartDate(startDatePicker.getValue().format(formatter));
            billGeneration.setEndDate(endDatePicker.getValue().format(formatter));
            billGeneration.setBillDate(invoiceDatePicker.getValue().format(formatter));


            File pdfFile = invoiceService.generateInvoiceFor(
                    company,
                    client,
                    billGeneration,
                    allByClientNameAndPodDateBetween,
                    Locale.getDefault());
            StreamResource streamResource = new StreamResource(fileName, () -> {
                try {
                    return new FileInputStream(pdfFile);
                } catch (IOException e1) {
                    return new ByteArrayInputStream(new byte[]{});
                }
            });
            Anchor anchor = new Anchor(streamResource, linkName);
            anchor.setWidth("10%");
            anchor.getElement().setAttribute("download", true);

            VerticalLayout embeddedPdfVLayout = new VerticalLayout();
            embeddedPdfVLayout.setSizeFull();

            String width = "1300px";
            String height = "500px";

            HorizontalLayout buttonPanelReportPreview = new HorizontalLayout();

            Label leftEmptyLbl = new Label();
            leftEmptyLbl.setWidth("85%");
            Button closeButton = new Button("", VaadinIcon.CLOSE.create());
            closeButton.setWidth("5%");

            buttonPanelReportPreview.add(leftEmptyLbl, anchor, closeButton);
            buttonPanelReportPreview.setWidth(width);
            buttonPanelReportPreview.setAlignItems(FlexComponent.Alignment.CENTER);

            embeddedPdfVLayout.add(buttonPanelReportPreview);
            embeddedPdfVLayout.add(new EmbeddedPdfDocument(streamResource, width, height));


            Dialog dialog = new Dialog();
            dialog.add(embeddedPdfVLayout);
            closeButton.addClickListener( event -> {
                        dialog.close();
                    }
            );
            dialog.open();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateTax(TextField cgst, TextField sgst, TextField igst, TextField fuelSurchage, Company company) {
        if(isGSTEnabled){

//            if(isDomestic){
                // DISABLE IGST
                igst.setValue("0"); igst.setEnabled(false);

                // ENABLE domestic
                cgst.setEnabled(true); cgst.setValue(String.valueOf(company.getCgst()));
                sgst.setEnabled(true); sgst.setValue(String.valueOf(company.getSgst()));

//            } else {
//                // DISABLE domestic
//                cgst.setValue("0"); cgst.setEnabled(false);
//                sgst.setValue("0"); sgst.setEnabled(false);
//
//                // ENABLE IGST
//                igst.setEnabled(true); igst.setValue(String.valueOf(company.getIgst()));
//            }
        } else {
            // Disable all
            cgst.setValue("0"); cgst.setEnabled(false);
            sgst.setValue("0"); sgst.setEnabled(false);
            igst.setValue("0"); igst.setEnabled(false);
        }
        if(isFuelSurchargeEnabled){
            // ENABLE Fuel Surcharge
            fuelSurchage.setEnabled(true); fuelSurchage.setValue(String.valueOf(company.getFuelSurcharge()));

        } else {
            // DISABLE Fuel Surcharge
            fuelSurchage.setValue("0"); fuelSurchage.setEnabled(false);

        }
    }

    private void updateClientName(Select<String> clientSelect, RateMasterService rateMasterService, RateIntMasterService rateIntMasterService){
        if(isDomestic){
            Set<String> distinctClientName = rateMasterService.findEnabledDistinctClientName();
            clientSelect.setItems(distinctClientName);
            currentSelectedItem = distinctClientName.iterator().next();
            clientSelect.setValue(currentSelectedItem);
        } else {
            Set<String> distinctClientName = rateIntMasterService.findEnabledDistinctClientName();
            clientSelect.setItems(distinctClientName);
            currentSelectedItem = distinctClientName.iterator().next();
            clientSelect.setValue(currentSelectedItem);
        }
    }

    private void load(
            Grid<AccountCopy> accountCopyGrid,
            AccountCopyService accountCopyService,
            DateFilter dateFilter,
            Select<String> bookingTypeSelect,
            TextField grossTotalTF,
            TextField netTotalTF,
            TextField cgst,
            TextField sgst,
            TextField igst,
            TextField fuelSurcharge,
            TextField totalDocNo){
        allByClientNameAndPodDateBetween.clear();
        allByClientNameAndPodDateBetween.addAll(accountCopyService.findAllByClientNameAndPodDateBetweenAndType(
                currentSelectedItem,
                fromLocaleDate(dateFilter.getStartDate()),
                fromLocaleDate(dateFilter.getEndDate()),
                bookingTypeSelect.getValue()));
        accountCopyGrid.setItems(
                allByClientNameAndPodDateBetween);
        Integer grossTotal = allByClientNameAndPodDateBetween.parallelStream().map(AccountCopy::getRate).reduce(0, Math::addExact);
        grossTotalTF.setValue(String.format("%.02f", grossTotal.floatValue()));
        long subTotal = (long) (grossTotal +
                Float.parseFloat(fuelSurcharge.getValue()) * grossTotal / 100.0);
        Double netTotal = subTotal +
                Float.parseFloat(cgst.getValue()) * subTotal / 100.0 +
                Float.parseFloat(sgst.getValue()) * subTotal / 100.0 +
                Float.parseFloat(igst.getValue()) * subTotal / 100.0;
        netTotalTF.setValue(String.format("%.02f", netTotal.floatValue()));
        totalDocNo.setValue(String.valueOf(allByClientNameAndPodDateBetween.size()));
    }

    private void showError(String error){
        Notification.show(error);
    }

    private Date fromLocaleDate(LocalDate localDate){
        return new Date(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private boolean keyEquals(Key key1, Key key2) {
        return key1.getKeys().get(0).equals(key2.getKeys().get(0));
    }
}
class BillingFilter extends DateFilter{
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public BillingFilter(LocalDate startDate, LocalDate endDate, String type) {
        super(startDate, endDate);
        this.type = type;
    }


}
