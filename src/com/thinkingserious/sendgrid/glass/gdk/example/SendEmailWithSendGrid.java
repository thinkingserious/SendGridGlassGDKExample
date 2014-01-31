package com.thinkingserious.sendgrid.glass.gdk.example;

import java.util.Hashtable;

import android.os.AsyncTask;

import com.github.sendgrid.SendGrid;

// This is the magic that sends the email through SendGrid's web API.
public class SendEmailWithSendGrid extends AsyncTask<Hashtable<String,String>, Void, String> {
    Utils creds = new Utils();

	@Override
    protected String doInBackground(Hashtable<String,String>... params) {
            Hashtable<String,String> h = params[0];
            SendGrid sendgrid = new SendGrid(creds.getUsername(),creds.getPassword());
            sendgrid.setFrom(creds.getFromAddress());
            sendgrid.addTo(h.get("to"));
            sendgrid.setSubject(h.get("subject"));
            sendgrid.setText(h.get("text") + "\n\n#throughglass");
            String response = sendgrid.send();
            return response;
    }
}