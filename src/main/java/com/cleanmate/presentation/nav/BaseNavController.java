package com.cleanmate.presentation.nav;

public abstract class BaseNavController {

    public void navDashboard()    { ViewRouter.get().navigate(Route.DASHBOARD); }
    public void navCalendar()     { ViewRouter.get().navigate(Route.CALENDAR); }
    public void navAddCleaning()  { ViewRouter.get().navigate(Route.ADD_CLEANING); }
    public void navCustomers()    { ViewRouter.get().navigate(Route.CUSTOMERS); }
    public void navApartments()    { ViewRouter.get().navigate(Route.APARTMENTS); }
    public void navEditApartment() { ViewRouter.get().navigate(Route.EDIT_APARTMENT); }
    public void navEditCustomer()  { ViewRouter.get().navigate(Route.EDIT_CUSTOMER); }
    public void navEmployees()     { ViewRouter.get().navigate(Route.EMPLOYEES); }
    public void navAddEmployee()   { ViewRouter.get().navigate(Route.ADD_EMPLOYEE); }
    public void navMySchedule()   { ViewRouter.get().navigate(Route.MY_SCHEDULE); }
    public void navChecklist()    { ViewRouter.get().navigate(Route.CHECKLIST); }
    public void navCleaningDetail(){ ViewRouter.get().navigate(Route.CLEANING_DETAIL); }
    public void navPortal()       { ViewRouter.get().navigate(Route.PORTAL); }
    public void navHistory()      { ViewRouter.get().navigate(Route.EMPLOYEE_HISTORY); }
    public void navProfile()      { ViewRouter.get().navigate(Route.EMPLOYEE_PROFILE); }
    public void navProperties()   { ViewRouter.get().navigate(Route.MY_PROPERTIES); }
    public void navInvoices()     { ViewRouter.get().navigate(Route.INVOICES); }
    public void navLogout()       { ViewRouter.get().navigate(Route.LOGIN); }
    public void navBack()         { ViewRouter.get().back(); }
}
