package com.cleanmate.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record Cleaning(
        String id,
        LocalDate date,
        LocalTime checkOut,
        LocalTime checkIn,
        String property,
        String customer,
        String employee,
        String status,
        int qcRating,
        String qcNote
) {
    public boolean occursOn(LocalDate d) { return d.equals(date); }

    public static Cleaning of(LocalDate date, LocalTime checkOut, LocalTime checkIn,
                               String property, String customer, String employee, String status) {
        return new Cleaning(UUID.randomUUID().toString(), date, checkOut, checkIn,
                property, customer, employee, status, 0, "");
    }

    public Cleaning withEmployee(String emp, String newStatus) {
        return new Cleaning(id, date, checkOut, checkIn, property, customer, emp, newStatus, qcRating, qcNote);
    }

    public Cleaning withStatus(String st) {
        return new Cleaning(id, date, checkOut, checkIn, property, customer, employee, st, qcRating, qcNote);
    }

    public Cleaning withQc(int rating, String note) {
        return new Cleaning(id, date, checkOut, checkIn, property, customer, employee, status, rating, note);
    }
}
