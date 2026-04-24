package com.cleanmate.presentation.nav;

public enum Route {
    LOGIN           ("/fxml/LoginView.fxml",                "CleanMate – Prihlásenie"),
    DASHBOARD       ("/fxml/DashboardView.fxml",            "CleanMate – Dashboard"),
    CALENDAR        ("/fxml/CleaningCalendarView.fxml",     "CleanMate – Upratovania"),
    ADD_CLEANING    ("/fxml/AddCleaningView.fxml",          "CleanMate – Nové upratovanie"),
    CLEANING_DETAIL ("/fxml/CleaningDetailView.fxml",       "CleanMate – Detail upratovania"),
    CUSTOMERS       ("/fxml/CustomerManagementView.fxml",   "CleanMate – Zákazníci"),
    APARTMENTS      ("/fxml/ApartmentManagementView.fxml",  "CleanMate – Apartmány"),
    EDIT_APARTMENT  ("/fxml/EditApartmentView.fxml",        "CleanMate – Apartmán"),
    EDIT_CUSTOMER   ("/fxml/EditCustomerView.fxml",         "CleanMate – Zákazník"),
    OWNER_INVOICES  ("/fxml/CustomerInvoicesView.fxml",     "CleanMate – Fakturácia zákazníka"),
    EMPLOYEES       ("/fxml/EmployeeManagementView.fxml",   "CleanMate – Zamestnanci"),
    ADD_EMPLOYEE    ("/fxml/AddEmployeeView.fxml",          "CleanMate – Zamestnanec"),
    MY_SCHEDULE     ("/fxml/MyScheduleView.fxml",           "CleanMate – Môj plán"),
    CHECKLIST       ("/fxml/ChecklistView.fxml",            "CleanMate – Checklist"),
    EMPLOYEE_HISTORY("/fxml/EmployeeHistoryView.fxml",      "CleanMate – História úloh"),
    EMPLOYEE_PROFILE("/fxml/EmployeeProfileView.fxml",      "CleanMate – Môj profil"),
    PORTAL          ("/fxml/CustomerPortalView.fxml",       "CleanMate – Prehľad pre zákazníka"),
    MY_PROPERTIES   ("/fxml/MyPropertiesView.fxml",         "CleanMate – Moje apartmány"),
    INVOICES        ("/fxml/InvoicesView.fxml",             "CleanMate – Faktúry");

    public final String fxml;
    public final String title;

    Route(String fxml, String title) {
        this.fxml = fxml;
        this.title = title;
    }
}
