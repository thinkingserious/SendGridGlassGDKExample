package com.thinkingserious.sendgrid.glass.gdk.example;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.glass.app.Card;

// This class is invoked when the app starts, specifically the function onCreate
public class MainActivity extends Activity {
	
    private static final int SET_TO = 0;
    private static final int SET_SUBJECT = 1;
    private static final int SET_TEXT = 2;

    private static final String VOICE_TO = "Say the recipient's name";
    private static final String VOICE_SUBJECT = "Say the subject of your email";
    private static final String VOICE_TEXT = "Say the body of your email";

    private String to = null;
    private String subject = null;
    private String text = null;
    private String footnote = "SendGrid.com";

    private Boolean initialized = false;
    private Boolean email_sent = false;

    // When the GDK supports contacts, you can replace this class
    private AddressBook email = new AddressBook();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String body = "Who will receive this email?\n";
        HashMap map = email.getEmails();
        Iterator iter = map.keySet().iterator();

        // Get all the names in our address book, the value is the email which will be used later
        while(iter.hasNext()){
            String key = (String)iter.next();
            body += key + "\n";
        }
        body += "\n\nTap to begin.";

        setCard(body, footnote);
    }

    // This is the display the user will see in Glass
    private void setCard (String body, String footnote){
        Card card = new Card(this);
        card.setText(body);
        card.setFootnote(footnote);
        setContentView(card.toView());
    }

    // Before sending the email, verify what is being sent
    private String generatePreview(){
        String preview = "Message preview (Tap to Send or Edit):\n\n";
        preview += this.to + "\n";
        preview += this.subject + "\n\n";
        preview += this.text + "\n\n";
        return preview;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Provide a way to exit the program
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return false;
        }

        // If a user taps the track pad
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

            if (email_sent){
                this.finish();
                return true;
            }

            // On the first tap, we want to get the to email address
            if (!initialized) {
                Intent intent_to = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent_to.putExtra(RecognizerIntent.EXTRA_PROMPT, VOICE_TO);
                startActivityForResult(intent_to, SET_TO);
                return true;
            }
            if(this.subject == null){
                Intent intent_subject = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent_subject.putExtra(RecognizerIntent.EXTRA_PROMPT, VOICE_SUBJECT);
                startActivityForResult(intent_subject, SET_SUBJECT);
                return true;
            }
            if(this.text == null){
                Intent intent_text = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent_text.putExtra(RecognizerIntent.EXTRA_PROMPT, VOICE_TEXT);
                startActivityForResult(intent_text, SET_TEXT);
                return true;
            }
            // When all fields are defined, lets provide the option to preview, edit and send
            if((this.to != null) && (this.subject != null) && (this.text != null)){
                openOptionsMenu();
                return true;
            }
        }

        return false;
    }

    @Override
    // This menu will allow the user to edit the to, subject, text (body) fields and/or send the email
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.email, menu);
        return true;
    }
    
    @Override
    // Get the voice command inputs
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        // Setup the to email address
    	if ((requestCode == SET_TO) && (resultCode == RESULT_OK)) {
            String name = results.get(0).toString();
            String recipient = email.getEmail(name);
            this.to = recipient;
            if(!initialized){
                setCard("What is the subject? Tap to continue.", footnote);
                initialized = true;
                return;
            } else {
                setCard(generatePreview(), footnote);
                return;
            }
        }
        // Specify the subject
        if ((requestCode == SET_SUBJECT) && (resultCode == RESULT_OK)) {
            this.subject = results.get(0).toString();
            if( (this.to != null) && (this.text != null) ){
                setCard(generatePreview(), footnote);
            } else {
                setCard("What is the message? Tap to continue", footnote);
            }
        }
        // Specify the body of the email
        if ((requestCode == SET_TEXT) && (resultCode == RESULT_OK)) {
            this.text = results.get(0).toString();
            setCard(generatePreview(), footnote);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    // Implements the menu selections
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Sends the email via SendGrid
            case R.id.send:
                SendEmailWithSendGrid email = new SendEmailWithSendGrid();
                Hashtable<String,String> params = new Hashtable<String,String>();
                params.put("to", this.to);
                params.put("subject", this.subject);
                params.put("text", this.text);
                try {
                    String result = email.execute(params).get();
                    String display = null;
                    JSONObject jObject = new JSONObject(result);
                    result = jObject.getString("message");
                    if (result.equals("success")){
                        display = "Email sent successfully.";
                        this.email_sent = true;
                    } else {
                        display = "Error: " + result + "Please contact, elmer@sendgrid.com with this error message.";
                    }
                    setCard(display, footnote);
                } catch (InterruptedException e) {
                    setCard(e.toString(), footnote);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    setCard(e.toString(), footnote);
                } catch (JSONException e) {
                    setCard(e.toString(), footnote);
                    e.printStackTrace();
                }
                return true;

            // Capture who the email will be sent to
            case R.id.to:
                Intent intent_to = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent_to.putExtra(RecognizerIntent.EXTRA_PROMPT, VOICE_TO);
                startActivityForResult(intent_to, SET_TO);
                return true;

            // Capture the subject of the email
            case R.id.subject:
                Intent intent_subject = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent_subject.putExtra(RecognizerIntent.EXTRA_PROMPT, VOICE_SUBJECT);
                startActivityForResult(intent_subject, SET_SUBJECT);
                return true;

            // Capture the body of the email
            case R.id.text:
                Intent intent_text = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent_text.putExtra(RecognizerIntent.EXTRA_PROMPT, VOICE_TEXT);
                startActivityForResult(intent_text, SET_TEXT);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
