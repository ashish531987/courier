package com.krupatek.courier.view;

import com.krupatek.courier.Constants;
import com.krupatek.courier.model.*;
import com.krupatek.courier.repository.CompanyRepository;
import com.krupatek.courier.service.AccountCopyService;
import com.krupatek.courier.service.BillingService;
import com.krupatek.courier.service.ClientService;
import com.krupatek.courier.service.InvoiceService;
import com.krupatek.courier.utils.DateUtils;
import com.krupatek.courier.view.accountcopy.AccountCopyEditor;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResource;
import org.springframework.data.domain.Page;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class ClientBillRePrintingForm extends Div {
    private ClientBillFilter filter;
    private final int PAGE_SIZE = 1000;

    public ClientBillRePrintingForm(
            BillingService billingService,
            AccountCopyService accountCopyService,
            InvoiceService invoiceService,
            ClientService clientService,
            CompanyRepository companyRepository,
            DateUtils dateUtils) {
        super();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(false);
        verticalLayout.setPadding(false);
        verticalLayout.setSizeFull();

        H4 title = new H4();
        title.setSizeFull();
        title.setText("Client Bill Reprinting");

        TextField billNo = new TextField();
        billNo.setPlaceholder("Filter by Bill No");
        billNo.setValueChangeMode(ValueChangeMode.LAZY);
        billNo.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);

        TextField billDate = new TextField();
        billDate.setPlaceholder("Filter by Invoice Date");
        billDate.setValueChangeMode(ValueChangeMode.LAZY);
        billDate.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);

        TextField clientName = new TextField();
        clientName.setPlaceholder("Filter by Client Name");
        clientName.setValueChangeMode(ValueChangeMode.LAZY);
        clientName.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);

        Grid<BillGeneration> clientBillGrid = new Grid<>(BillGeneration.class, false);
        clientBillGrid.setPageSize(PAGE_SIZE);

        clientBillGrid.addColumn(BillGeneration::getBillNo).setKey("srNo").setHeader(new Html("<b>Sr No</b>")).
                setTextAlign(ColumnTextAlign.END).setWidth("8%").setFlexGrow(0).getElement().executeJs("this.renderer = function(root, column, rowData) {root.textContent = rowData.index + 1}");;


        clientBillGrid.addColumn(BillGeneration::getBillNo).setKey("billNo").setHeader(new Html("<b>Bill No</b>")).setTextAlign(ColumnTextAlign.END).setWidth("14%").setFlexGrow(0);
        clientBillGrid.addColumn(billGeneration ->
                {
                    try {
                        return new SimpleDateFormat("dd/MM/yy").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(billGeneration.getBillDate()));
                    } catch (ParseException e) {
                        return billGeneration.getBillDate();
                    }
                }
        ).setKey("billDate").setHeader(new Html("<b>Bill Date</b>")).setWidth("14%").setFlexGrow(0);
        clientBillGrid.addColumn(BillGeneration::getClientName).setKey("clientName").setHeader(new Html("<b>Client Name</b>")).setWidth("22%").setFlexGrow(0);
        clientBillGrid.addColumn(BillGeneration::getBillAmount).setKey("billAmount").setHeader(new Html("<b>Gross Amount</b>")).setTextAlign(ColumnTextAlign.END).setWidth("16.5%").setFlexGrow(0);
        clientBillGrid.addColumn(BillGeneration::getNetAmount).setKey("netAmount").setHeader(new Html("<b>Net Amount</b>")).setTextAlign(ColumnTextAlign.END).setWidth("16.5%").setFlexGrow(0);
        clientBillGrid.addComponentColumn(item -> new Button("Delete", click -> {
            Dialog confirmDialog = new Dialog();
            confirmDialog.setCloseOnEsc(false);
            confirmDialog.setCloseOnOutsideClick(false);

            VerticalLayout containerLayout = new VerticalLayout();

            H5 confirmDelete = new H5("Confirm delete");
            containerLayout.add(confirmDelete);
            containerLayout.add(new Label("Are you sure you want to delete the item?"));

            HorizontalLayout buttonLayout = new HorizontalLayout();
            buttonLayout.setWidth("100%");

            Button deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteBtn.setWidth("30%");

            Label emptyLbl = new Label();
            emptyLbl.setWidth("40%");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setWidth("30%");
            buttonLayout.add(cancelBtn, emptyLbl, deleteBtn);

            containerLayout.add(buttonLayout);
            containerLayout.setWidth("400px");
            containerLayout.setHeight("150px");

            confirmDialog.add(containerLayout);

            deleteBtn.addClickListener(deleteEvent -> {
                billingService.delete(item);
                accountCopyService.resetBillNo(item.getBillNo());
                clientBillGrid.getDataProvider().refreshAll();
                confirmDialog.close();
                Notification.show("Bill no "+item.getBillNo()+" deleted successfully.");
            });

            cancelBtn.addClickListener( cancelEvent -> {
                confirmDialog.close();
            });

            confirmDialog.open();
        })).setWidth("5%");

        HeaderRow hr = clientBillGrid.prependHeaderRow();
        hr.getCell(clientBillGrid.getColumnByKey("billNo")).setComponent(billNo);
        hr.getCell(clientBillGrid.getColumnByKey("billDate")).setComponent(billDate);
        hr.getCell(clientBillGrid.getColumnByKey("clientName")).setComponent(clientName);

        clientBillGrid.setColumnReorderingAllowed(false);

        CallbackDataProvider.FetchCallback<BillGeneration, Void> billGenerationClientBillFilterFetchCallback = query -> {
            // The index of the first item to load
            int offset = query.getOffset();

            // The number of items to load
            int limit = query.getLimit();

            Logger.getLogger(AccountCopyEditor.class.getName()).info("Original offset : " + offset + ", limit :" + limit);

            offset = offset / PAGE_SIZE;
            limit = Math.min(limit, PAGE_SIZE);

            String billNoFilter = filter.getBillNoFilter();
            String invoiceDateFilter = filter.getInvoiceDateFilter();
            String clientNameFilter = filter.getClientNameFilter();


            Logger.getLogger(AccountCopyEditor.class.getName()).info("Corrected offset : " + offset + ", limit :" + limit);

            Page<BillGeneration> accountCopies = billingService
                    .findByAndBillNoStartsWithAndBillDateContainingAndClientNameStartsWith(offset, limit, billNoFilter, invoiceDateFilter, clientNameFilter);
            Logger.getLogger(AccountCopyEditor.class.getName()).info("Total pages : " + accountCopies.getTotalElements());
            return accountCopies.stream();
        };
//        DataProvider<BillGeneration, ClientBillFilter> dataProvider =
//                DataProvider.fromFilteringCallbacks(
//                        // First callback fetches items based on a query
//                        billGenerationClientBillFilterFetchCallback,
//                        // Second callback fetches the number of items
//                        // for a query
//                        query -> {
//                            ClientBillFilter clientBillFilter = query.getFilter().orElse(new ClientBillFilter());
//                            String billNoFilter = clientBillFilter.getBillNoFilter();
//                            String invoiceDateFilter = clientBillFilter.getInvoiceDateFilter();
//                            String clientNameFilter = clientBillFilter.getClientNameFilter();
//                            return Math.toIntExact(billingService.countByBillNoStartsWithAndBillDateContainingAndClientNameStartsWith(billNoFilter, invoiceDateFilter,clientNameFilter));
//                        });
//
        filter = new ClientBillFilter();
//        ConfigurableFilterDataProvider<BillGeneration, Void, ClientBillFilter> wrapper =
//                dataProvider.withConfigurableFilter();
//        wrapper.setFilter(filter);
//        clientBillGrid.setDataProvider(wrapper);
        clientBillGrid.setItems(billGenerationClientBillFilterFetchCallback);
        billNo.addValueChangeListener(event -> {
            filter.setBillNoFilter(event.getValue());
            clientBillGrid.setItems(billGenerationClientBillFilterFetchCallback);
//            wrapper.refreshAll();
        });

        billDate.addValueChangeListener(event -> {
            try {
                filter.setInvoiceDateFilter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("dd/MM/yy").parse(event.getValue())));
                clientBillGrid.setItems(billGenerationClientBillFilterFetchCallback);
            } catch (ParseException e) {
                filter.setInvoiceDateFilter(event.getValue());
                clientBillGrid.setItems(billGenerationClientBillFilterFetchCallback);
            }
//            wrapper.refreshAll();
        });

        clientName.addValueChangeListener(event -> {
            filter.setClientNameFilter(event.getValue());
            clientBillGrid.setItems(billGenerationClientBillFilterFetchCallback);
//            wrapper.refreshAll();
        });
        verticalLayout.add(title ,clientBillGrid);

        clientBillGrid.asSingleSelect().addValueChangeListener(gridBillGenerationComponentValueChangeEvent -> {
           clientBillGrid.select(gridBillGenerationComponentValueChangeEvent.getValue());
        });

        clientBillGrid.addSelectionListener(selectionEvent -> {
            if(selectionEvent.isFromClient() && selectionEvent.getFirstSelectedItem().isPresent()) {
                String invoiceNo = selectionEvent.getFirstSelectedItem().get().getBillNo();
                List<AccountCopy> accountCopies = accountCopyService.findAllByBillNoOrderByPodDate(invoiceNo + " ");
                Client client = clientService.findAllByClientName(selectionEvent.getFirstSelectedItem().get().getClientName()).get(0);
                Company company = companyRepository.findAll().get(0);


                try {
                    BillGeneration billGeneration = selectionEvent.getFirstSelectedItem().get();

                    String oldBillDate = billGeneration.getBillDate();
                    String oldStartDate = billGeneration.getStartDate();
                    String oldEndDate = billGeneration.getEndDate();

                    try {
                        String newBillDate = new SimpleDateFormat("dd-MMM-yyyy").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oldBillDate));
                        billGeneration.setBillDate(newBillDate);

                        String newStartDate = new SimpleDateFormat("dd-MMM-yyyy").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oldStartDate));
                        billGeneration.setStartDate(newStartDate);

                        String newEndDate = new SimpleDateFormat("dd-MMM-yyyy").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oldEndDate));
                        billGeneration.setEndDate(newEndDate);
                    } catch (Exception e){
                        String newBillDate = new SimpleDateFormat("dd-MMM-yyyy").format(new SimpleDateFormat("dd/MM/yy").parse(oldBillDate));
                        billGeneration.setBillDate(newBillDate);

                        String newStartDate = new SimpleDateFormat("dd-MMM-yyyy").format(new SimpleDateFormat("dd/MM/yy").parse(oldStartDate));
                        billGeneration.setStartDate(newStartDate);

                        String newEndDate = new SimpleDateFormat("dd-MMM-yyyy").format(new SimpleDateFormat("dd/MM/yy").parse(oldEndDate));
                        billGeneration.setEndDate(newEndDate);
                    }

                    File pdfFile = invoiceService.generateInvoiceFor(company, client, billGeneration, accountCopies, Locale.getDefault());

                    billGeneration.setBillDate(oldBillDate);
                    billGeneration.setStartDate(oldStartDate);
                    billGeneration.setEndDate(oldEndDate);

                    VerticalLayout embeddedPdfVLayout = new VerticalLayout();
                    embeddedPdfVLayout.setSizeFull();

                    StreamResource resource = new StreamResource("invoice.pdf", () -> {
                        try {
                            return new FileInputStream(pdfFile);
                        } catch (FileNotFoundException e1) {
                            return new ByteArrayInputStream(new byte[]{});
                        }
                    });
                    String width = "1300px";
                    String height = "500px";

                    HorizontalLayout buttonPanelReportPreview = new HorizontalLayout();

                    Label leftEmptyLbl = new Label();
                    leftEmptyLbl.setWidth("85%");

                    Anchor podSummaryDownloadLink = new Anchor(resource, "Download Invoice");
                    podSummaryDownloadLink.setWidth("10%");
                    podSummaryDownloadLink.getElement().setAttribute("download", true);
                    Button closeButton = new Button("", VaadinIcon.CLOSE.create());
                    closeButton.setWidth("5%");

                    buttonPanelReportPreview.add(leftEmptyLbl, podSummaryDownloadLink, closeButton);
                    buttonPanelReportPreview.setWidth(width);
                    buttonPanelReportPreview.setAlignItems(FlexComponent.Alignment.CENTER);

                    embeddedPdfVLayout.add(buttonPanelReportPreview);
                    embeddedPdfVLayout.add(new EmbeddedPdfDocument(resource, width, height));


                    Dialog dialog = new Dialog();
                    dialog.add(embeddedPdfVLayout);
                    closeButton.addClickListener(event -> {
                                dialog.close();
                            }
                    );
                    dialog.open();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });



        add(verticalLayout);
    }
}
