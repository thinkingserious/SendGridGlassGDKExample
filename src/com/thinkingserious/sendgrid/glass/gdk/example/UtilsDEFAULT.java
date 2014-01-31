package com.thinkingserious.sendgrid.glass.gdk.example;

// Change the name of this file to Utils.java and rename the class to Utils.
// Drop in your own SendGrid credentials
public class UtilsDEFAULT {
    private static final String username = "Sendgrid-Name" ;
    private static final String password = "Sendgrid-Password" ;
    private static final String from_address = "From-Email-Address";

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFromAddress() {
        return from_address;
    }
}