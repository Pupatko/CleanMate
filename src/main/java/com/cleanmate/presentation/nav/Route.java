package com.cleanmate.presentation.nav;

public enum Route {
    LOGIN           ("/fxml/LoginView.fxml",              "CleanMate – Prihlásenie"),
    DASHBOARD       ("/fxml/DashboardView.fxml",          "CleanMate – Dashboard"),
    CALENDAR        ("/fxml/CleaningCalendarView.fxml",   "CleanMate – Cleaning Calendar"),
    CLEANING_DETAIL ("/fxml/CleaningDetailView.fxml",     "CleanMate – Detail upratovania"),
    CUSTOMERS       ("/fxml/CustomerManagementView.fxml", "CleanMate – Zákazníci"),
    PLANS           ("/fxml/CleaningPlanBuilderView.fxml","CleanMate – Cleaning Plans"),
    EMPLOYEES       ("/fxml/EmployeeManagementView.fxml", "CleanMate – Zamestnanci"),
    MY_SCHEDULE     ("/fxml/MyScheduleView.fxml",         "CleanMate – Môj plán"),
    CHECKLIST       ("/fxml/ChecklistView.fxml",          "CleanMate – Checklist"),
    PORTAL          ("/fxml/CustomerPortalView.fxml",     "CleanMate – Prehľad pre zákazníka");

    public final String fxml;
    public final String title;

    Route(String fxml, String title) {
        this.fxml = fxml;
        this.title = title;
    }
}
