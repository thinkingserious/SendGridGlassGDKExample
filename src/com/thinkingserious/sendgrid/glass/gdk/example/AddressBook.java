package com.thinkingserious.sendgrid.glass.gdk.example;

import java.util.HashMap;

/*
Since the voice recognition in Glass makes it hard to speak email addresses, we instead use nicknames
Once Contacts are implemented in the GDK, we can replace this class
*/
public class AddressBook {
    private HashMap<String, String> emails;

    // You may want to use some external source to populate this data
    public AddressBook(){
        emails = new HashMap<String, String>();
        emails.put("Elmer".toUpperCase(), "elmer.thomas@sendgrid.com");
        emails.put("OmniFocus".toUpperCase(), "send-to-omnifocus@omnigroup.com");
    }

    public HashMap<String, String> getEmails() {
        return emails;
    }

    public String getEmail(String name){
        String email = emails.get(name.toUpperCase());
        if( email != null ) {
            return email;
        } else {
            email = "Email not recognized, tap to reset.";
        }
        return email;
    }
}
