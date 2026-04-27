package com.cleanmate.presentation.nav;

import com.cleanmate.presentation.util.ConfirmDialog;
import com.cleanmate.presentation.util.ToastManager;
import java.util.Locale;

public abstract class BaseNavController {

    public void navDashboard()     { ViewRouter.get().navigate(Route.DASHBOARD); }
    public void navCalendar()      { ViewRouter.get().navigate(Route.CALENDAR); }
    public void navAddCleaning()   { ViewRouter.get().navigate(Route.ADD_CLEANING); }
    public void navCustomers()     { ViewRouter.get().navigate(Route.CUSTOMERS); }
    public void navApartments()    { ViewRouter.get().navigate(Route.APARTMENTS); }
    public void navEditApartment() { ViewRouter.get().navigate(Route.EDIT_APARTMENT); }
    public void navEditCustomer()  { ViewRouter.get().navigate(Route.EDIT_CUSTOMER); }
    public void navOwnerInvoices() { ViewRouter.get().navigate(Route.OWNER_INVOICES); }
    public void navEmployees()     { ViewRouter.get().navigate(Route.EMPLOYEES); }
    public void navAddEmployee()   { ViewRouter.get().navigate(Route.ADD_EMPLOYEE); }
    public void navMySchedule()    { ViewRouter.get().navigate(Route.MY_SCHEDULE); }
    public void navChecklist()     { ViewRouter.get().navigate(Route.CHECKLIST); }
    public void navCleaningDetail(){ ViewRouter.get().navigate(Route.CLEANING_DETAIL); }
    public void navPortal()        { ViewRouter.get().navigate(Route.PORTAL); }
    public void navHistory()       { ViewRouter.get().navigate(Route.EMPLOYEE_HISTORY); }
    public void navProfile()       { ViewRouter.get().navigate(Route.EMPLOYEE_PROFILE); }
    public void navProperties()    { ViewRouter.get().navigate(Route.MY_PROPERTIES); }
    public void navInvoices()      { ViewRouter.get().navigate(Route.INVOICES); }
    public void navLogout() {
        if (ConfirmDialog.show("confirm.logout.header",
                LanguageManager.getBundle().getString("confirm.logout.content"))) {
            ViewRouter.get().navigate(Route.LOGIN);
        }
    }
    public void navBack()          { ViewRouter.get().back(); }

    protected void toast(String message, ToastManager.Type type) {
        ToastManager.show(ViewRouter.get().getStage(), message, type);
    }

    public void onLangSk() {
        LanguageManager.setLocale(Locale.forLanguageTag("sk"));
        ViewRouter.get().reload();
    }

    public void onLangEn() {
        LanguageManager.setLocale(Locale.ENGLISH);
        ViewRouter.get().reload();
    }
}
