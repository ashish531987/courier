package com.krupatek.courier;

import com.krupatek.courier.model.AccountCopy;
import com.krupatek.courier.model.PlaceGeneration;
import com.krupatek.courier.repository.CompanyRepository;
import com.krupatek.courier.repository.CountryRepository;
import com.krupatek.courier.repository.PlaceGenerationRepository;
import com.krupatek.courier.repository.StateRepository;
import com.krupatek.courier.service.*;
import com.krupatek.courier.utils.DateUtils;
import com.krupatek.courier.utils.NumberUtils;
import com.krupatek.courier.view.ClientBillPrintingForm;
import com.krupatek.courier.view.ClientBillRePrintingForm;
import com.krupatek.courier.view.CustomerBillingDetailsForm;
import com.krupatek.courier.view.SystemSettingsForm;
import com.krupatek.courier.view.accountcopy.AccountCopyEditor;
import com.krupatek.courier.view.accountcopy.AccountCopyForm;
import com.krupatek.courier.view.clientprofile.ClientProfileEditor;
import com.krupatek.courier.view.placegeneration.PlaceGenerationEditor;
import com.krupatek.courier.view.pod.PODEntryForm;
import com.krupatek.courier.view.rate.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.springframework.beans.factory.annotation.Autowired;

@Route
public class MainView extends VerticalLayout {
    @Autowired
    AccountCopyService accountCopyService;

    @Autowired
    ClientService clientService;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    PlaceGenerationRepository placeGenerationRepository;

    @Autowired
    CountryRepository countryRepository;


    @Autowired
    StateRepository stateRepository;

    @Autowired
    DestinationService destinationService;

    @Autowired
    RateMasterService rateMasterService;

    @Autowired
    RateIntMasterService rateIntMasterService;

    @Autowired
    CourierService courierService;

    @Autowired
    PlaceGenerationService placeGenerationService;

    @Autowired
    NetworkService networkService;

    @Autowired
    DateUtils dateUtils;

    @Autowired
    NumberUtils numberUtils;

    @Autowired
    PODSummaryService podSummaryService;

    @Autowired
    BillingService billingService;

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    DailyReportService dailyReportService;

    @Autowired
    ZonesService zonesService;

    @Autowired
    CountryService countryService;

    @Autowired
    StateService stateService;

    public MainView(@Autowired MessageBean bean) {
        MenuBar menuBar = new MenuBar();
        Div component = new Div();
        component.setWidthFull();
        component.setHeightFull();

        menuBar.setOpenOnHover(true);
        Text selected = new Text("");
        Div message = new Div(new Text("Selected: "), selected);

        MenuItem masters = menuBar.addItem("Masters");
        MenuItem billingDetails = menuBar.addItem("Billing Details");

        SubMenu systemSettingsSubMenu = masters.getSubMenu();

        Component systemSettingsMenu = getMenuItemComponent(VaadinIcon.COG_O, "System Settings");

        systemSettingsSubMenu.addItem(systemSettingsMenu, e -> {
            component.removeAll();
            component.add(new SystemSettingsForm(companyRepository));
        }  );

        Component clientProfileMenu = getMenuItemComponent(VaadinIcon.CLIPBOARD_USER, "Client Profile");

        masters.getSubMenu().addItem(clientProfileMenu,
                e -> {
                    component.removeAll();
                    component.add(new ClientProfileEditor(clientService));
                });

        Component rateMasterMenu = getMenuItemComponent(VaadinIcon.MONEY_EXCHANGE, "Rate Master");
        MenuItem rateMaster = masters.getSubMenu().addItem(rateMasterMenu);
        rateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Create Zone"), e -> {
            component.removeAll();
            component.add(new CreateZoneForm(zonesService));
        });
        rateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Zone Generation"), e -> {
            component.removeAll();
            component.add(new ZoneGeneration(zonesService, placeGenerationService));
        });
        rateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Rate Entry"), e -> {
            component.removeAll();
            component.add(new RateEntryEditor(clientService, courierService, zonesService, rateMasterService));
        });

//        rateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Place Generation"), e -> {
//            component.removeAll();
//            component.add(new PlaceGenerationForm(zonesService, placeGenerationService, stateService, new PlaceGeneration()));
//
//        });

        rateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Place Generation"), e -> {
            component.removeAll();
            component.add(new PlaceGenerationEditor(zonesService, placeGenerationService, stateService));

        });


        Component intRateMasterMenu = getMenuItemComponent(VaadinIcon.DOLLAR, "International Rate Master");
        MenuItem intRateMaster = masters.getSubMenu().addItem(intRateMasterMenu);
        intRateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Add Country To Zones"), e -> {
            component.removeAll();
            component.add(new InternationalZoneGeneration(courierService, networkService, countryService));
        });
        intRateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Rate International Entry"), e -> {
            component.removeAll();
            component.add(new RateIntEntryEditor(clientService, courierService, networkService, rateIntMasterService));
        });
        intRateMaster.getSubMenu().addItem(getMenuItemComponent(null, "Country Generation"), e -> {
            component.removeAll();
            component.add(new CountryGenerationForm(countryRepository,countryService));
        });

        Component accountCopyMenu = getMenuItemComponent(VaadinIcon.FILE_ADD, "Account Copy");
        MenuItem accountCopyMenuItem = billingDetails.getSubMenu().addItem(accountCopyMenu);
        accountCopyMenuItem.getSubMenu().addItem(getMenuItemComponent(null, "Create Account Copy"), e -> {
            component.removeAll();
            component.add(new AccountCopyForm(
                    accountCopyService,
                    clientService,
                    rateMasterService ,
                    rateIntMasterService,
                    placeGenerationService,
                    networkService,
                    dateUtils,
                    numberUtils,
                    new AccountCopy(),
                    courierService,
                    billingService));
        } );
        accountCopyMenuItem.getSubMenu().addItem(getMenuItemComponent(null, "Edit Account Copy"), e -> {
            component.removeAll();
            component.add(new AccountCopyEditor(
                    accountCopyService,
                    clientService,
                    rateMasterService,
                    rateIntMasterService,
                    placeGenerationService,
                    networkService,
                    dateUtils,
                    numberUtils,
                    courierService,
                    billingService));
        } );
        Component clientBillingMenu = getMenuItemComponent(VaadinIcon.INVOICE, "Client Billing");

        MenuItem clientBillingCopyItem = billingDetails.getSubMenu().addItem(clientBillingMenu);
        clientBillingCopyItem.getSubMenu().addItem(getMenuItemComponent(null, "Client Bill Printing"), e -> {
            component.removeAll();
            component.add(new ClientBillPrintingForm(
                    accountCopyService,
                    clientService,
                    rateMasterService,
                    rateIntMasterService,
                    placeGenerationService,
                    networkService,
                    invoiceService,
                    companyRepository,
                    billingService,
                    dateUtils,
                    numberUtils,
                    courierService));

        });
        clientBillingCopyItem.getSubMenu().addItem(getMenuItemComponent(null, "Client Bill Reprinting"), e -> {
            component.removeAll();
            component.add(new ClientBillRePrintingForm(
                    billingService,
                    accountCopyService,
                    invoiceService,
                    clientService,
                    companyRepository,
                    dateUtils));
        });
        Component podSummaryMenu = getMenuItemComponent(VaadinIcon.LINE_BAR_CHART, "POD Summary / Daily Report");
        billingDetails.getSubMenu().addItem(podSummaryMenu, e -> {
            component.removeAll();
            component.add(new CustomerBillingDetailsForm(
                    accountCopyService,
                    clientService,
                    rateMasterService,
                    rateIntMasterService,
                    placeGenerationService,
                    networkService,
                    podSummaryService,
                    dailyReportService,
                    companyRepository,
                    dateUtils,
                    numberUtils,
                    courierService,
                    billingService));
        } );

        Component podEntryForm = getMenuItemComponent(VaadinIcon.EDIT, "POD Entry Form");
        billingDetails.getSubMenu().addItem(podEntryForm, e -> {
            component.removeAll();
            component.add(new PODEntryForm(accountCopyService, dateUtils, numberUtils));
        });

        add(menuBar);
        add(component);
    }

    private Component getMenuItemComponent(VaadinIcon icon , String action) {
        HorizontalLayout componentContainer = new HorizontalLayout();
        componentContainer.setHeight("30px");
        componentContainer.setAlignItems(Alignment.CENTER);

        if(icon != null) {
            Icon componentIC = new Icon(icon);
            componentIC.setSize("20px");
            componentContainer.add(componentIC);
        }
        H6 componentAction = new H6(action);
        componentAction.setHeight("30px");

        componentContainer.add(componentAction);
        return componentContainer;
    }
}
