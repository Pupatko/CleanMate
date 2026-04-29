package com.cleanmate.service;

import com.cleanmate.repository.jdbc.JdbcApartmentRepository;
import com.cleanmate.repository.jdbc.JdbcCleaningRepository;
import com.cleanmate.repository.jdbc.JdbcCustomerRepository;
import com.cleanmate.repository.jdbc.JdbcEmployeeRepository;

public final class ServiceLocator {

    private static CleaningService  cleaningService;
    private static EmployeeService  employeeService;
    private static CustomerService  customerService;
    private static ApartmentService apartmentService;

    private ServiceLocator() {}

    public static void init() {
        cleaningService  = new CleaningService(new JdbcCleaningRepository());
        employeeService  = new EmployeeService(new JdbcEmployeeRepository());
        customerService  = new CustomerService(new JdbcCustomerRepository());
        apartmentService = new ApartmentService(new JdbcApartmentRepository());
    }

    public static CleaningService  cleanings()  { return cleaningService; }
    public static EmployeeService  employees()  { return employeeService; }
    public static CustomerService  customers()  { return customerService; }
    public static ApartmentService apartments() { return apartmentService; }
}
