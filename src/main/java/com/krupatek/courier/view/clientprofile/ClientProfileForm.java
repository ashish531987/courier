package com.krupatek.courier.view.clientprofile;

import com.krupatek.courier.model.Client;
import com.krupatek.courier.service.ClientService;
import com.krupatek.courier.utils.ViewUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.ArrayList;
import java.util.List;

public class ClientProfileForm extends Div {
    private Client client;

    public ClientProfileForm(ClientService clientService, Client client) {
        super();

        Dialog dialog = new Dialog();

        boolean isNewClientEntry= client.getClientCode() == null || client.getClientCode() == 0;
        if(isNewClientEntry){
            client.setClientCode((int) clientService.nextClientCode());
        }


        this.client = client;
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setPadding(true);
        horizontalLayout.setMargin(false);
        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("50em");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("25em", 2));

        Binder<Client> binder = new Binder<>();

        formLayout.add(ViewUtils.addCloseButton(dialog), 2);

        Label title = new Label();
        title.setSizeFull();
        title.setText("Client Details");
        formLayout.add(title, 2);


        // Client Code

        TextField clientCode = new TextField();
        clientCode.setLabel("Client Code : ");
        clientCode.setValueChangeMode(ValueChangeMode.EAGER);
        formLayout.add(clientCode, 1);
        binder.bind(clientCode,
                c -> c.getClientCode().toString(),
                (c, t) -> c.setClientCode(Integer.valueOf(t)));

        // Client Name

        List<Client> clientList = clientService.findAll();

        List<String> clientNameList = new ArrayList<>();
        clientList.forEach(c -> clientNameList.add(c.getClientName()));

        ComboBox<String> clientsComboBox = new ComboBox<>();
        clientsComboBox.setAllowCustomValue(true);
        clientsComboBox.setLabel("Client Name : ");
        clientsComboBox.setItems(clientNameList);
        clientsComboBox.setClearButtonVisible(true);
        formLayout.add(clientsComboBox, 1);
        clientsComboBox.addCustomValueSetListener(event -> clientsComboBox.setValue(event.getDetail()));
        binder.bind(clientsComboBox, Client::getClientName, Client::setClientName);

//        clientsComboBox.addCustomValueSetListener(
//                event -> {
//                    title.setText(event.getDetail());
//                    clientNameList.add(event.getDetail());
//                    clientsComboBox.clear();
//                    clientsComboBox.setItems(clientNameList);
//                    clientsComboBox.setValue(event.getDetail());
//                });

        clientsComboBox.addValueChangeListener(event -> {
            if(event.getValue() == null){
                title.setText("event.getValue()");
            } else {
                title.setText(event.getValue());
                List<Client> searchResult = clientService.findAllByClientName(event.getValue());
                if (searchResult.size() > 0) {
                    this.client = searchResult.get(0);
                    binder.readBean(this.client);
                }
            }
        });

        // Client Address Name

        TextField clientAddress = new TextField();
        clientAddress.setLabel("Client Address : ");
        clientAddress.setValueChangeMode(ValueChangeMode.EAGER);
        formLayout.add(clientAddress, 2);

        binder.bind(clientAddress,
                Client::getClientAddress1,
                Client::setClientAddress1);

        // City

        TextField city = new TextField();
        city.setLabel("City : ");
        city.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(city,
                Client::getCity,
                Client::setCity);

        // Phone Number
        TextField phoneNumber = new TextField();
        phoneNumber.setLabel("Phone Number : ");
        phoneNumber.setValueChangeMode(ValueChangeMode.EAGER);

        formLayout.add(city, phoneNumber);

        binder.bind(phoneNumber,
                Client::getPhone,
                Client::setPhone);

        // GST select
        Select<String> gstSelect = new Select<>();
        gstSelect.setLabel("GST : ");
        gstSelect.setItems("Yes", "No");
        gstSelect.setValue("Yes");
        formLayout.add(gstSelect, 1);
        binder.bind(gstSelect,
                Client::getGstEnabled,
                Client::setGstEnabled);

        // GSTIN Number
        TextField gstin = new TextField();
        gstin.setLabel("GSTIN : ");
        gstin.setValueChangeMode(ValueChangeMode.EAGER);
        formLayout.add(gstin, 1);
        binder.bind(gstin,
                Client::getGstNo,
                Client::setGstNo);


        // Fuel Surcharge
        Select<String> fuelSurchargeSelect = new Select<>();
        fuelSurchargeSelect.setLabel("Fuel Surcharge : ");
        fuelSurchargeSelect.setItems("Yes", "No");
        binder.bind(fuelSurchargeSelect, Client::getFsc, Client::setFsc);

        formLayout.add(fuelSurchargeSelect, 1);

        // Enabled
        Select<String> enabled = new Select<>();
        enabled.setLabel("Enabled : ");
        enabled.setItems("Yes", "No");
        binder.bind(enabled, Client::getEnabled, Client::setEnabled);

        formLayout.add(enabled, 1);

/*
        Button add = new Button("Add", event -> {
           this.client = new Client();
           binder.readBean(client);
        });
*/
        Button save = new Button("Save",
                event -> {
                    try {
                        binder.writeBean(this.client);
                        clientService.saveAndFlush(this.client);
                        Notification.show("Client profile updated successfully.");
                        dialog.close();
                        // A real application would also save the updated person
                        // using the application's backend
                    } catch (ValidationException e) {
                        Notification.show("Client could not be saved, " +
                                "please check error messages for each field.");
                    }
                });
        Button reset = new Button("Reset",
                event -> binder.readBean(this.client));

        Button cancel = new Button("Cancel", event -> dialog.close());

        Button delete = new Button("Delete", event -> {
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
                try {
                    binder.writeBean(this.client);
                    clientService.delete(client);
                    Notification.show("Client profile deleted successfully.");
                    confirmDialog.close();
                    dialog.close();
                } catch (ValidationException e) {
                    Notification.show("Client could not be deleted, " +
                            "please check error messages for each field.");
                }
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);


            cancelBtn.addClickListener( cancelEvent -> {
                confirmDialog.close();
            });

            confirmDialog.open();
        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.setAlignItems(HorizontalLayout.Alignment.END);
        if(isNewClientEntry) {
            actions.add(save, reset, cancel);
        } else {
            actions.add(save,reset, cancel, delete);
        }
        formLayout.add(actions);
        horizontalLayout.add(formLayout);
        dialog.add(horizontalLayout);

        clientCode.addKeyDownListener(Key.ENTER, event ->
                clientsComboBox.focus());
        clientAddress.addKeyDownListener(Key.ENTER, event ->
                city.focus());
        city.addKeyDownListener(Key.ENTER, event ->
                phoneNumber.focus());
        phoneNumber.addKeyDownListener(Key.ENTER, event ->
                gstSelect.focus());
        gstin.addKeyDownListener(Key.ENTER, event ->
                fuelSurchargeSelect.focus());

        dialog.open();

        clientCode.focus();
        binder.readBean(client);
    }
}
