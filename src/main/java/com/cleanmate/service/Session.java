package com.cleanmate.service;

public final class Session {

    public enum Role { FIRMA, ZAMESTNANEC, ZAKAZNIK }

    private static Role   role;
    private static String displayName;
    private static String userId;

    private Session() {}

    public static void login(Role r, String name, String id) {
        role = r; displayName = name; userId = id;
    }

    public static void logout() {
        role = null; displayName = null; userId = null;
    }

    public static boolean isLoggedIn()    { return role != null; }
    public static Role    getRole()        { return role; }
    public static String  getDisplayName() { return displayName; }
    public static String  getUserId()      { return userId; }
}
