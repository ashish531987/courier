package com.krupatek.courier.view.rate;

import com.krupatek.courier.model.PlaceGeneration;
import com.krupatek.courier.model.State;
import com.krupatek.courier.model.Zones;
import com.krupatek.courier.service.PlaceGenerationService;
import com.krupatek.courier.service.StateService;
import com.krupatek.courier.service.ZonesService;
import com.krupatek.courier.utils.ViewUtils;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class PlaceGenerationForm extends Div {
    public PlaceGenerationForm(ZonesService zonesService,
                               PlaceGenerationService placeGenerationService,
                               StateService stateService,
                               PlaceGeneration placeGeneration) {
        super();

        boolean isNewPlaceGeneration = placeGeneration.getPlaceId() == null ||  placeGeneration.getCityName() == null;

        Dialog dialog = new Dialog();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setPadding(true);
        horizontalLayout.setMargin(false);

        Binder<PlaceGeneration> binder = new Binder<>(PlaceGeneration.class);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("25em", 2));
        formLayout.setMaxWidth("25em");

        formLayout.add(ViewUtils.addCloseButton(dialog), 2);

        H4 title = new H4();
        title.setSizeFull();
        title.setText("Place Generation");
        formLayout.add(title, 2);


        TextField cityTxt = new TextField();
        cityTxt.setLabel("City : ");
        cityTxt.setValueChangeMode(ValueChangeMode.EAGER);
        formLayout.add(cityTxt, 2);

        if(isNewPlaceGeneration) {
            binder.forField(cityTxt).asRequired("City name is mandatory").bind(str -> "", PlaceGeneration::setCityName);
        } else{
            binder.bind(cityTxt, PlaceGeneration::getCityName, PlaceGeneration::setCityName);
        }
        cityTxt.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue().length() > 3) {
                String cityName = e.getValue();

                // TODO
                // PlaceGeneration - cityName
                Optional<PlaceGeneration> placeGenerationOptional = placeGenerationService.findByCityName(cityName);
                if (placeGenerationOptional.isPresent()) {
                    // Show error
                    cityTxt.setInvalid(true);
                    cityTxt.setErrorMessage("City Already Exists !!");

                } else {
                    cityTxt.setInvalid(false);
                }
            }
        });
//        cityTxt.setValue((placeGeneration.getCityName()));

        ComboBox<String> stateSelect = new ComboBox<>();
        stateSelect.setWidth("50");
        stateSelect.setLabel("State :");
        formLayout.add(stateSelect, 2);

        TreeSet<String> stateSource = stateService.findAll().parallelStream().map(State::getStateName).collect(Collectors.toCollection(TreeSet::new));
        stateSelect.setItems(stateSource);
        binder.bind(stateSelect, PlaceGeneration::getStateName, PlaceGeneration::setStateName);
        //stateSelect.setValue(stateSource.first());


        ComboBox<String> placeCodeSelect = new ComboBox<>();
        placeCodeSelect.setWidth("50");
        placeCodeSelect.setLabel("Place code :");
        formLayout.add(placeCodeSelect, 2);

        TreeSet<String> placeCodeSource = zonesService.findAll().parallelStream().map(Zones::getZoneCode).collect(Collectors.toCollection(TreeSet::new));
        placeCodeSelect.setItems(placeCodeSource);
        binder.bind(placeCodeSelect, PlaceGeneration::getPlaceCode, PlaceGeneration::setPlaceCode);

        Button save = new Button("Save",
                event -> {
                    try {
                        binder.writeBean(placeGeneration);

                        if(isNewPlaceGeneration) {
                            placeGeneration.setPlaceId((int) placeGenerationService.nextPlaceId());
                        }
                        placeGeneration.setCityName(cityTxt.getValue());
                        placeGeneration.setStateName(stateSelect.getValue());
                        placeGeneration.setPlaceCode(placeCodeSelect.getValue());

                        Notification.show("PlaceGeneration : ");

                        placeGenerationService.saveAndFlush(placeGeneration);
                        Notification.show("Details saved successfully");


                    } catch (ValidationException e) {
                        Notification.show("Details could not be saved");

                    }

                });

        Button reset = new Button("Reset",
                event -> binder.readBean(placeGeneration));

        Button cancel = new Button("Cancel", event -> dialog.close());


        HorizontalLayout actions = new HorizontalLayout();
        actions.setAlignItems(HorizontalLayout.Alignment.END);

        if(!isNewPlaceGeneration){
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
                    placeGenerationService.delete(placeGeneration);
                    confirmDialog.close();
                    dialog.close();
                    Notification.show("Place Generation deleted successfully.");
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
        }

        horizontalLayout.add(formLayout);

        dialog.add(horizontalLayout);
        dialog.open();
        binder.readBean(placeGeneration);
    }
}
