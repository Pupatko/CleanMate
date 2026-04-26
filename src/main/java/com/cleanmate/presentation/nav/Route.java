package com.cleanmate.presentation.nav;

public enum Route {
    LOGIN           ("/fxml/LoginView.fxml",                "title.login"),
    DASHBOARD       ("/fxml/DashboardView.fxml",            "title.dashboard"),
    CALENDAR        ("/fxml/CleaningCalendarView.fxml",     "title.calendar"),
    ADD_CLEANING    ("/fxml/AddCleaningView.fxml",          "title.add.cleaning"),
    CLEANING_DETAIL ("/fxml/CleaningDetailView.fxml",       "title.cleaning.detail"),
    CUSTOMERS       ("/fxml/CustomerManagementView.fxml",   "title.customers"),
    APARTMENTS      ("/fxml/ApartmentManagementView.fxml",  "title.apartments"),
    EDIT_APARTMENT  ("/fxml/EditApartmentView.fxml",        "title.edit.apartment"),
    EDIT_CUSTOMER   ("/fxml/EditCustomerView.fxml",         "title.edit.customer"),
    OWNER_INVOICES  ("/fxml/CustomerInvoicesView.fxml",     "title.owner.invoices"),
    EMPLOYEES       ("/fxml/EmployeeManagementView.fxml",   "title.employees"),
    ADD_EMPLOYEE    ("/fxml/AddEmployeeView.fxml",          "title.add.employee"),
    MY_SCHEDULE     ("/fxml/MyScheduleView.fxml",           "title.my.schedule"),
    CHECKLIST       ("/fxml/ChecklistView.fxml",            "title.checklist"),
    EMPLOYEE_HISTORY("/fxml/EmployeeHistoryView.fxml",      "title.employee.history"),
    EMPLOYEE_PROFILE("/fxml/EmployeeProfileView.fxml",      "title.employee.profile"),
    PORTAL          ("/fxml/CustomerPortalView.fxml",       "title.portal"),
    MY_PROPERTIES   ("/fxml/MyPropertiesView.fxml",         "title.my.properties"),
    INVOICES        ("/fxml/InvoicesView.fxml",             "title.invoices");

    public final String fxml;
    public final String titleKey;

    Route(String fxml, String titleKey) {
        this.fxml = fxml;
        this.titleKey = titleKey;
    }
}
