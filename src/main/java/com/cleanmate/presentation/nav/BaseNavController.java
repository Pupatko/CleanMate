package com.cleanmate.presentation.nav;

public abstract class BaseNavController {

    public void navDashboard()   { ViewRouter.get().navigate(Route.DASHBOARD); }
    public void navCalendar()    { ViewRouter.get().navigate(Route.CALENDAR); }
    public void navCustomers()   { ViewRouter.get().navigate(Route.CUSTOMERS); }
    public void navPlans()       { ViewRouter.get().navigate(Route.PLANS); }
    public void navEmployees()   { ViewRouter.get().navigate(Route.EMPLOYEES); }
    public void navMySchedule()  { ViewRouter.get().navigate(Route.MY_SCHEDULE); }
    public void navChecklist()   { ViewRouter.get().navigate(Route.CHECKLIST); }
    public void navCleaningDetail() { ViewRouter.get().navigate(Route.CLEANING_DETAIL); }
    public void navPortal()      { ViewRouter.get().navigate(Route.PORTAL); }
    public void navLogout()      { ViewRouter.get().navigate(Route.LOGIN); }
    public void navBack()        { ViewRouter.get().back(); }
}
