package com.krupatek.courier.view.accountcopy;

import com.krupatek.courier.Constants;
import com.krupatek.courier.model.*;
import com.krupatek.courier.service.*;
import com.krupatek.courier.utils.DateUtils;
import com.krupatek.courier.utils.NumberUtils;
import com.krupatek.courier.utils.RateUtils;
import com.krupatek.courier.utils.ViewUtils;
import com.krupatek.courier.view.HorizonDatePicker;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.Key.TAB;

@SpringComponent
@UIScope
public class AccountCopyForm extends Div {
    private boolean isDomestic = true; // False means International.
    private boolean isCashCustomer = false;

    public AccountCopyForm(
            AccountCopyService accountCopyService,
            ClientService clientService,
            RateMasterService rateMasterService,
            RateIntMasterService rateIntMasterService,
            PlaceGenerationService placeGenerationService,
            NetworkService networkService,
            DateUtils dateUtils,
            NumberUtils numberUtils,
            AccountCopy accountCopy,
            CourierService courierService,
            BillingService billingService) {
        super();

        boolean isNewAccountCopy = accountCopy.getDocNo() == null || accountCopy.getDocNo().isEmpty();

        Dialog dialog = new Dialog();


        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setPadding(true);
        horizontalLayout.setMargin(false);
        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("60em");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("5em", 1),
                new FormLayout.ResponsiveStep("5em", 2),
                new FormLayout.ResponsiveStep("5em", 3),
                new FormLayout.ResponsiveStep("5em", 4),
                new FormLayout.ResponsiveStep("5em", 5),
                new FormLayout.ResponsiveStep("5em", 6),
                new FormLayout.ResponsiveStep("5em", 7),
                new FormLayout.ResponsiveStep("5em", 8),
                new FormLayout.ResponsiveStep("5em", 9),
                new FormLayout.ResponsiveStep("5em", 10),
                new FormLayout.ResponsiveStep("5em", 11),
                new FormLayout.ResponsiveStep("5em", 12));

        H4 title = new H4();

        if(isNewAccountCopy){
            title.setText("Create New Account Copy");
        } else {
            title.setText("Edit Account Copy");
        }
        formLayout.add(ViewUtils.addCloseButton(dialog), 12);
        formLayout.add(title, 12);

        Binder<AccountCopy> binder = new Binder<>(AccountCopy.class);

        // Courier Name
        Select<String> courierSelect = new Select<>();
        courierSelect.setLabel("Courier Name : ");
        TreeSet<String> courierSource = courierService.findAll().parallelStream().map(Courier::getCourierName).collect(Collectors.toCollection(TreeSet::new));
        courierSelect.setItems(courierSource);
        binder.bind(courierSelect, AccountCopy::getToParty, AccountCopy::setToParty);
        formLayout.add(courierSelect, 3);
        formLayout.add(new Label(""), 9);

        // Doc Number
        TextField docNo = new TextField();
        docNo.setWidth("25%");
        docNo.setLabel("Doc No. : ");
        docNo.setValueChangeMode(ValueChangeMode.LAZY);
        docNo.setValueChangeTimeout(Constants.TEXT_FIELD_TIMEOUT);
        docNo.setAutoselect(true);
        binder.
                forField(docNo).asRequired("Every Account copy must have Doc no").
                bind(AccountCopy::getDocNo, AccountCopy::setDocNo);

        if(!isNewAccountCopy) {
            docNo.addValueChangeListener(e -> {
                if (e.getValue() != null && e.getValue().length() > 4) {
                    String newDocNo = e.getValue();
                    Optional<AccountCopy> newAccountCopy = accountCopyService.findOneByDocNo(newDocNo);
                    if (newAccountCopy.isPresent()) {
                        docNo.setInvalid(false);
                        binder.readBean(newAccountCopy.get());
                    } else {
                        docNo.setInvalid(true);
                        docNo.setErrorMessage("Account Copy doesn't exists with this Doc No.");
//                        docNo.focus();
                    }
                }
            });
        } else {
            docNo.addValueChangeListener(e -> {
                if (e.getValue() != null && e.getValue().length() > 4) {
                    String newDocNo = e.getValue();
                    Optional<AccountCopy> newAccountCopy = accountCopyService.findOneByDocNo(newDocNo);
                    if (newAccountCopy.isPresent()) {
                        docNo.setInvalid(true);
                        docNo.setErrorMessage("Account Copy already exists with this Doc No.");
//                        docNo.focus();
                    } else {
                        docNo.setInvalid(false);
//                        docNo.focus();
                    }
                }
            });
        }


        // Date
        LocalDate currentDate = dateUtils.asLocalDate(accountCopy.getPodDate());

        HorizontalLayout podDateLayout = new HorizontalLayout();
        podDateLayout.setWidth("25%");
        HorizonDatePicker podDate = new HorizonDatePicker();
        podDate.wrap(podDateLayout, currentDate, dateUtils, numberUtils);
        podDateLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

//        DatePicker podDate = new DatePicker();
//        podDate.setLabel("Date : ");
//        binder.bind(podDate,
//                    d -> dateUtils.asLocalDate(accountCopy.getPodDate()),
//                    (a, d) ->  a.setPodDate(dateUtils.asDate(d)));


        // Cash / Credit
        Select<String> cashCreditSelect = new Select<>();
        cashCreditSelect.setWidth("25%");
        cashCreditSelect.setLabel("Cash / Cr : ");
        cashCreditSelect.setItems("Cash", "Cr");
        binder.bind(cashCreditSelect, AccountCopy::getPodType, AccountCopy::setPodType);


        // Document / Parcel
        Select<String> selectDocumentOrParcelType = new Select<>();
        selectDocumentOrParcelType.setWidth("25%");
        selectDocumentOrParcelType.setLabel("Document / Parcel : ");
        selectDocumentOrParcelType.setItems("D", "P");
        binder.bind(selectDocumentOrParcelType, AccountCopy::getdP, AccountCopy::setdP);

        formLayout.add(docNo, 3);
        formLayout.add(podDateLayout, 3);
        formLayout.add(cashCreditSelect, 2);
        formLayout.add(selectDocumentOrParcelType, 2);

        // Client Name

        if(!isNewAccountCopy){
            isDomestic =  !accountCopy.getType().equals("Inter");
        }

        // Credit Client
        Select<String> clientsComboBox = new Select<>();
        clientsComboBox.setLabel("Client Name (Credit) : ");
        updateClientName(clientsComboBox, rateMasterService, rateIntMasterService);
        binder.bind(clientsComboBox, AccountCopy::getClientName, AccountCopy::setClientName);


        // Cash Client
        TextField cashClientTextField = new TextField();
        cashClientTextField.setLabel("Client Name (Cash): ");
        cashClientTextField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        cashClientTextField.setAutoselect(true);
        cashClientTextField.setEnabled(false);
        binder.bind(cashClientTextField, AccountCopy::getClientName, AccountCopy::setClientName);

        cashCreditSelect.addValueChangeListener(event -> {
            if(event.getValue().equalsIgnoreCase("Cash")){
                clientsComboBox.setEnabled(false);
                cashClientTextField.setEnabled(true);
                isCashCustomer = true;
            } else if (event.getValue().equalsIgnoreCase("Cr")){
                clientsComboBox.setEnabled(true);
                cashClientTextField.setEnabled(false);
                isCashCustomer = false;
            }
        });


        // Destination
        ComboBox<String> destinationComboBox = new ComboBox<>();
        destinationComboBox.setAllowCustomValue(true);
        destinationComboBox.setLabel("Destination : ");
        destinationComboBox.addCustomValueSetListener(event -> destinationComboBox.setValue(event.getDetail()));
        updateDestination(destinationComboBox, placeGenerationService, networkService);

        // Booking Type
        Select<String> bookingTypeSelect = new Select<>();
        bookingTypeSelect.setLabel("Booking Type : ");
        bookingTypeSelect.setItems("Dom", "Inter");
        bookingTypeSelect.setValue("Dom");
        binder.bind(bookingTypeSelect, AccountCopy::getType, AccountCopy::setType);
        if(isNewAccountCopy) {
            bookingTypeSelect.addValueChangeListener(e -> {
                isDomestic = !e.getValue().equals("Inter");
                updateClientName(clientsComboBox, rateMasterService, rateIntMasterService);
                updateDestination(destinationComboBox, placeGenerationService, networkService);
            });
        } else {
            bookingTypeSelect.setReadOnly(true);
        }

        formLayout.add(bookingTypeSelect, 2);
        // Second row
        formLayout.add(clientsComboBox, 5);
        formLayout.add(cashClientTextField, 4);
        formLayout.add(destinationComboBox, 3);

        // PinCode
        TextField pincode = new TextField();
        pincode.setLabel("Pincode : ");
        pincode.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        binder.bind(pincode,
                AccountCopy::getArea,
                AccountCopy::setArea);
        pincode.setAutoselect(true);

        // Receiver Name
        TextField receiverName = new TextField();
        receiverName.setLabel("Receiver Name : ");
        receiverName.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        binder.forField(receiverName).asRequired("Every Account copy must have receiver name").bind(AccountCopy::getReceiverName, AccountCopy::setReceiverName);
        receiverName.setAutoselect(true);

        binder.forField(destinationComboBox).asRequired("Every Account copy must have the destination").
        bind(AccountCopy::getDestination,  (e, r) -> {
            e.setDestination(r);
            e.setPlaceCode(r);
            try {
                if (isDomestic) {
                    Optional<PlaceGeneration> placeGeneration = placeGenerationService.findByCityName(accountCopy.getDestination());
                    placeGeneration.ifPresent(generation -> e.setStateCode(generation.getPlaceCode()));
                } else {
                    NetworkId networkId = new NetworkId();
                    networkId.setNetName(courierSelect.getValue());
                    networkId.setCountryName(r);

                    Optional<Network> network = networkService.findOne(networkId);
                    network.ifPresent(value -> e.setStateCode(value.getZoneName()));
                }
            } catch (Exception exception){
                // Ignore - Scenario custom City name
            }
        });

        formLayout.add(pincode, 2);
        formLayout.add(receiverName, 4);
//        formLayout.add(new Label(""), 1);


        // Mode
        Select<String> modeSelect = new Select<>();
        modeSelect.setLabel("Mode : ");
        modeSelect.setItems("S", "L", "A");
        binder.bind(modeSelect, AccountCopy::getMode, AccountCopy::setMode);

        // Weight
        TextField weight = new TextField();
        weight.setLabel("Weight : ");
        weight.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        binder.forField(weight).withConverter(
                new StringToDoubleConverter("Not a number")).bind(
                AccountCopy::getWeight,
                AccountCopy::setWeight);
        weight.setAutoselect(true);

        // Rate
        TextField rate = new TextField();
        rate.setLabel("Rate : ");
        rate.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        binder.forField(rate).withConverter(
                new StringToIntegerConverter("Not a number")).bind(
                AccountCopy::getRate,
                AccountCopy::setRate);
        rate.setAutoselect(true);

        formLayout.add(modeSelect, 2);
        formLayout.add(weight, 2);
        formLayout.add(rate, 2);
//        formLayout.add(new Label(""), 3);

        // Reset Destination
        Select<String> resetDestination = new Select<>();
        resetDestination.setLabel("Reset Destination after save: ");
        resetDestination.setItems("Yes", "No");
        resetDestination.setValue("Yes");
        resetDestination.setEnabled(isNewAccountCopy);

        courierSelect.addValueChangeListener(event -> {
            resetDestination.setValue("Yes");
        });


        Button save = new Button("Save",
                event -> {
                    try {
                        binder.writeBean(accountCopy);

                        // Set pod date
                        accountCopy.setPodDate(dateUtils.asDate(podDate.getCurrentDate()));

                        // Set client name
                        if(isCashCustomer){
                            accountCopy.setClientName(cashClientTextField.getValue());
                        } else {
                            accountCopy.setClientName(clientsComboBox.getValue());
                        }

                        accountCopyService.saveAndFlush(accountCopy);

                        if(!isNewAccountCopy && !(accountCopy.getBillNo() == null || accountCopy.getBillNo().isEmpty())){

                            // Update Gross and Net total

                            String billNo = accountCopy.getBillNo();
                            List<AccountCopy> accountCopyList = accountCopyService.findAllByBillNoOrderByPodDate(billNo);
                            Integer grossTotal = accountCopyList.parallelStream().map(AccountCopy::getRate).reduce(0, Math::addExact);

                            Optional<BillGeneration> billGeneration = billingService.findOne(billNo.trim());
                            if(billGeneration.isPresent()){
                                BillGeneration billToBeUpdated = billGeneration.get();
                                Double subTotal = grossTotal +
                                        billToBeUpdated.getFuelSurcharge() * grossTotal / 100.0;
                                double netTotal = subTotal +
                                        billToBeUpdated.getCgst() * subTotal / 100.0 +
                                        billToBeUpdated.getSgst() * subTotal / 100.0 +
                                        billToBeUpdated.getIgst() * subTotal / 100.0;
                                billToBeUpdated.setBillAmount(grossTotal);
                                billToBeUpdated.setNetAmount(Math.toIntExact((Math.round(netTotal))));
                                billToBeUpdated.setBalance(Math.toIntExact((Math.round(netTotal))));

                                billingService.saveAndFlush(billToBeUpdated);
                            }
                        }

                        if(isNewAccountCopy && resetDestination.getValue().equalsIgnoreCase("Yes")) {
                            destinationComboBox.setValue("");
                        }
                        Notification.show("Account copy updated successfully.");
                        if(isNewAccountCopy){
                            docNo.focus();
                        } else {
                            dialog.close();
                        }
                        // A real application would also save the updated person
                        // using the application's backend
                    } catch (ValidationException e) {
                        Notification.show("Account Copy could not be saved, " +
                                "please check error messages for each field.");
                    }
                });
        Button reset = new Button("Reset",
                event -> binder.readBean(accountCopy));

        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout();
        actions.setAlignItems(HorizontalLayout.Alignment.END);

        if(!isNewAccountCopy){
            Button delete = new Button("Delete", event -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setCloseOnEsc(false);
                confirmDialog.setCloseOnOutsideClick(false);

                VerticalLayout containerLayout = new VerticalLayout();
                containerLayout.setWidth("400px");
                containerLayout.setHeight("150px");

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

                confirmDialog.add(containerLayout);

                deleteBtn.addClickListener(deleteEvent -> {
                    accountCopyService.delete(accountCopy);
                    confirmDialog.close();
                    dialog.close();
                    Notification.show("Account copy deleted successfully.");
                });

                cancelBtn.addClickListener( cancelEvent -> {
                   confirmDialog.close();
                });

                confirmDialog.open();
            });
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

            actions.add(save, reset, cancel, delete);
            formLayout.add(actions, 6);
        } else {
            actions.add(save, reset, cancel);
            formLayout.add(actions, 4);
            formLayout.add(new Label(""), 5);
            formLayout.add(resetDestination, 3);
        }

        horizontalLayout.add(formLayout);
        dialog.add(horizontalLayout);

        dialog.open();

//        docNo.addKeyDownListener(Key.TAB, event ->
//                podDate.focus());


//        docNo.addKeyDownListener(Key.ENTER, event ->
//                podDate.focus());

//        podDate.addValueChangeListener(e -> cashCreditSelect.focus());
//        cashCreditSelect.addValueChangeListener(e -> selectDocumentOrParcelType.focus());
//        selectDocumentOrParcelType.addValueChangeListener(e -> bookingTypeSelect.focus());
//        bookingTypeSelect.addValueChangeListener(e -> clientsComboBox.focus());
//        clientsComboBox.addValueChangeListener(e -> destinationComboBox.focus());
//        destinationComboBox.addValueChangeListener(e -> pincode.focus());
//        pincode.addKeyDownListener(Key.ENTER, e -> receiverName.focus());
//        receiverName.addKeyDownListener(Key.ENTER, e -> courierSelect.focus());
//        courierSelect.addValueChangeListener(e -> modeSelect.focus());
//        modeSelect.addValueChangeListener(e -> weight.focus());
        weight.addKeyDownListener(TAB, e -> {
            Notification.show("Weight is "+weight.getValue());
            if(!isCashCustomer){
                calculateRate(rateMasterService, rateIntMasterService, accountCopy, binder, weight, rate, clientsComboBox.getValue());
            } else {
                rate.focus();
            }
        });
        weight.addKeyDownListener(Key.ENTER, e -> {
            if(!isCashCustomer) {
                calculateRate(rateMasterService, rateIntMasterService, accountCopy, binder, weight, rate, clientsComboBox.getValue());
            } else {
                rate.focus();
            }
        });
//        rate.addFocusListener(listener -> {
//            Notification.show("Weight is "+weight.getValue());
//            if(!isCashCustomer){
//                calculateRate(rateMasterService, rateIntMasterService, accountCopy, binder, weight, rate, clientsComboBox.getValue());
//            } else {
//                rate.focus();
//            }
//        });
//        rate.addKeyDownListener(Key.ENTER, e -> save.focus());

        docNo.focus();

        binder.readBean(accountCopy);
    }

    private void calculateRate(
            RateMasterService rateMasterService,
            RateIntMasterService rateIntMasterService,
            AccountCopy accountCopy,
            Binder<AccountCopy> binder,
            TextField weight,
            TextField rate,
            String clientName) {
        try {
            binder.writeBean(accountCopy);
            accountCopy.setClientName(clientName);

            Logger.getLogger(AccountCopyForm.class.getName()).info("AccountCopy : "+
                    accountCopy.getClientName()+", "+
                    accountCopy.getToParty()+", "+
                    accountCopy.getStateCode()+", "+
                    accountCopy.getdP()+", "+
                    accountCopy.getMode());
            if(isDomestic) {
                RateEntry rateEntry =
                        rateMasterService.findByClientNameAndCourierAndStateCodeAndPodTypeAndMode(
                                accountCopy.getClientName(),
                                accountCopy.getToParty(),
                                accountCopy.getStateCode(),
                                accountCopy.getdP(),
                                accountCopy.getMode()
                        );
                Logger.getLogger(AccountCopyForm.class.getName()).info("RateEntry : " + rateEntry);
                if(rateEntry != null) {
                    Double rateText = new RateUtils().charges(Double.valueOf(weight.getValue()), rateEntry.getFrom1(), rateEntry.getTo1(), rateEntry.getRate(), rateEntry.getAddWt(), (double) rateEntry.getAddRt());
                    rate.setValue(Integer.toString(rateText.intValue()));
                } else {
                    Notification.show("Rate not defined for client "+accountCopy.getClientName()+
                            ", to party :  "+accountCopy.getToParty()+", state code : "+accountCopy.getStateCode()+", d/p : "+accountCopy.getdP()+", mode : "+accountCopy.getMode());
                    rate.setValue("0");
                }
            } else {
                RateIntEntry rateIntEntry = rateIntMasterService.findByClientNameAndStateCodeAndPodTypeAndMode(
                  accountCopy.getClientName(),
                  accountCopy.getStateCode(),
                  accountCopy.getdP(),
                  accountCopy.getMode()
                );
                Logger.getLogger(AccountCopyForm.class.getName()).info("RateIntEntry : " + rateIntEntry);
                if(rateIntEntry != null) {
                    Double rateText = new RateUtils().charges(Double.valueOf(weight.getValue()), rateIntEntry.getFrom1(), rateIntEntry.getTo1(), rateIntEntry.getRate(), rateIntEntry.getAddWt(), (double) rateIntEntry.getAddRt());
                    rate.setValue(Integer.toString(rateText.intValue()));
                } else {
                    Notification.show("Rate not defined for client "+accountCopy.getClientName()+
                            ", to party :  "+accountCopy.getToParty()+", state code : "+accountCopy.getStateCode()+", d/p : "+accountCopy.getdP()+", mode : "+accountCopy.getMode());
                    rate.setValue("0");
                }
            }
            rate.focus();
        } catch (ValidationException ex) {
            Notification.show("Account Copy could not be saved, " +
                    "please check error messages for each field.");
        }
    }

    private void updateClientName(Select<String> clientComboBox, RateMasterService rateMasterService, RateIntMasterService rateIntMasterService){
        if(isDomestic){
            clientComboBox.setItems(rateMasterService.findEnabledDistinctClientName());
        } else {
            clientComboBox.setItems(rateIntMasterService.findEnabledDistinctClientName());
        }
    }
    private void updateDestination(ComboBox<String> destinationComboBox, PlaceGenerationService placeGenerationService, NetworkService networkService){
        if(isDomestic){
            destinationComboBox.setItems(
                    (ComboBox.ItemFilter<String>) (s, s2) -> s.toLowerCase().startsWith(s2.toLowerCase()),
                    placeGenerationService.findDistinctCityName());
        } else {
            destinationComboBox.setItems(
                    (ComboBox.ItemFilter<String>) (s, s2) -> s.toLowerCase().startsWith(s2.toLowerCase()),
                    networkService.findDistinctCountry());
        }
    }
}
