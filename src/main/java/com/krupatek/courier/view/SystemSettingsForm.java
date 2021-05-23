package com.krupatek.courier.view;

import com.krupatek.courier.model.Company;
import com.krupatek.courier.repository.CompanyRepository;
import com.krupatek.courier.utils.ViewUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;

public class SystemSettingsForm extends Div {
    public SystemSettingsForm(CompanyRepository companyRepository) {
        super();

        Dialog dialog = new Dialog();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setPadding(true);
        horizontalLayout.setMargin(false);

        Binder<Company> binder = new Binder<>();
        Company company = companyRepository.findAll().get(0);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("25em", 2));
        formLayout.setMaxWidth("50em");

        formLayout.add(ViewUtils.addCloseButton(dialog), 2);

        H4 title = new H4();
        title.setSizeFull();
        title.setText("Company Information");
        formLayout.add(title, 2);


        // Company Origin

        TextField origin = new TextField();
        origin.setLabel("Origin : ");
        origin.setValueChangeMode(ValueChangeMode.EAGER);
        formLayout.add(origin, 2);
        binder.bind(origin,
                Company::getOrigin,
                Company::setOrigin);
        origin.setValue(company.getOrigin());


        // Company Prefix

        TextField companyPrefix = new TextField();
        companyPrefix.setLabel("Company Prefix : ");
        companyPrefix.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(companyPrefix,
                Company::getCompanyPrefix,
                Company::setCompanyPrefix);
        formLayout.add(companyPrefix, 2);

        // Company Name

        TextField companyName = new TextField();
        companyName.setLabel("Company Name : ");
        companyName.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(companyName,
                Company::getCompanyName,
                Company::setCompanyName);
        formLayout.add(companyName, 2);

        // Company Address

        TextField companyAddress = new TextField();
        companyAddress.setLabel("Company Address : ");
        companyAddress.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(companyAddress,
                Company::getCompanyAddress1,
                Company::setCompanyAddress1);
        formLayout.add(companyAddress, 2);

        // City

        TextField city = new TextField();
        city.setLabel("City : ");
        city.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(city,
                Company::getCity,
                Company::setCity);

        // Phone Number
        TextField phoneNumber = new TextField();
        phoneNumber.setLabel("Phone Number : ");
        phoneNumber.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(phoneNumber,
                Company::getPhone,
                Company::setPhone);

        formLayout.add(city, phoneNumber);

        // GSTIN Number
        TextField gstin = new TextField();
        gstin.setLabel("GSTIN : ");
        gstin.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(gstin,
                Company::getGstin,
                Company::setGstin);
        formLayout.add(gstin, 2);

        // CGST %
        TextField cgst = new TextField();
        cgst.setLabel("CGST in % : ");
        cgst.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(cgst,
                c -> c.getCgst().toString(),
                (c, t) -> c.setCgst(Double.valueOf(t)));

        // SGST %
        TextField sgst = new TextField();
        sgst.setLabel("SGST in % : ");
        sgst.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(sgst,
                c -> c.getSgst().toString(),
                (c, t) -> c.setSgst(Double.valueOf(t)));

        // IGST %
        TextField igst = new TextField();
        igst.setLabel("IGST in % : ");
        igst.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(igst,
                c -> c.getIgst().toString(),
                (c, t) -> c.setIgst(Double.valueOf(t)));

        // Fuel Charge
        TextField fuelSurcharge = new TextField();
        fuelSurcharge.setLabel("Fuel Surcharge : ");
        fuelSurcharge.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(fuelSurcharge,
                c -> c.getFuelSurcharge().toString(),
                (c, t) -> c.setFuelSurcharge(Double.valueOf(t)));

        formLayout.add(
                cgst,
                sgst,
                igst,
                fuelSurcharge);


        Button save = new Button("Save",
                event -> {
                    try {
                        binder.writeBean(company);
                        companyRepository.saveAndFlush(company);
                        Notification.show("Company settings saved successfully.");
                        // A real application would also save the updated person
                        // using the application's backend
                    } catch (ValidationException e) {
                        Notification.show("Company settings could not be saved, " +
                                "please check error messages for each field.");
                    }
                });
        Button reset = new Button("Reset",
                event -> binder.readBean(company));

        Button cancel = new Button("Cancel", event -> dialog.close());

        origin.addKeyDownListener(Key.ENTER, event ->
                companyPrefix.focus());
        companyPrefix.addKeyDownListener(Key.ENTER, event ->
                companyName.focus());
        companyName.addKeyDownListener(Key.ENTER, event ->
                companyAddress.focus());
        companyAddress.addKeyDownListener(Key.ENTER, event ->
                city.focus());
        city.addKeyDownListener(Key.ENTER, event ->
                phoneNumber.focus());
        phoneNumber.addKeyDownListener(Key.ENTER, event ->
                gstin.focus());
        gstin.addKeyDownListener(Key.ENTER, event ->
                cgst.focus());
        cgst.addKeyDownListener(Key.ENTER, event ->
                sgst.focus());
        sgst.addKeyDownListener(Key.ENTER, event ->
                igst.focus());
        igst.addKeyDownListener(Key.ENTER, event ->
                fuelSurcharge.focus());
        fuelSurcharge.addKeyDownListener(Key.ENTER, event ->
                save.focus());

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset, cancel);
        formLayout.add(actions, 2);
        horizontalLayout.add(formLayout);
        dialog.add(horizontalLayout);
        origin.focus();
        dialog.open();
        binder.readBean(company);
    }
}
