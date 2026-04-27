package com.cleanmate.service;

import com.cleanmate.repository.inmemory.InMemoryApartmentRepository;
import com.cleanmate.repository.inmemory.InMemoryCleaningRepository;
import com.cleanmate.repository.inmemory.InMemoryCustomerRepository;
import com.cleanmate.repository.inmemory.InMemoryEmployeeRepository;

public final class ServiceLocator {

    private static CleaningService  cleaningService;
    private static EmployeeService  employeeService;
    private static CustomerService  customerService;
    private static ApartmentService apartmentService;

    private ServiceLocator() {}

    public static void init() {
        cleaningService  = new CleaningService(new InMemoryCleaningRepository());
        employeeService  = new EmployeeService(new InMemoryEmployeeRepository());
        customerService  = new CustomerService(new InMemoryCustomerRepository());
        apartmentService = new ApartmentService(new InMemoryApartmentRepository());
    }

    public static CleaningService  cleanings()  { return cleaningService; }
    public static EmployeeService  employees()  { return employeeService; }
    public static CustomerService  customers()  { return customerService; }
    public static ApartmentService apartments() { return apartmentService; }
}
