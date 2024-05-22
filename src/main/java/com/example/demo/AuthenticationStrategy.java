package com.example.demo;

public interface AuthenticationStrategy {
    static int authenticate(String userName, String password) {
        return 0;
    }


    public class DatabaseAuthentication implements AuthenticationStrategy {
    static public int authenticate(String userName, String password) {
        int res = 0;
        res = Dbase.authenticateUser(Dbase.connect(),userName,password) ;
        return res ;
    }
}

public class EmailAuthAuthentication implements AuthenticationStrategy {
    static public int authenticate(String email, String password) {
        int res = 0;
        res = Dbase.authenticateUserEmail(Dbase.connect(),email,password) ;
        return res;
    }
}
}
