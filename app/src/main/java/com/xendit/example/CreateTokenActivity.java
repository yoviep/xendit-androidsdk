package com.xendit.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.xendit.Models.Address;
import com.xendit.Models.BillingDetails;
import com.xendit.Models.Card;
import com.xendit.Models.Customer;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.TokenCallback;
import com.xendit.Xendit;
import com.xendit.example.models.TokenizationResponse;

import java.util.Calendar;

/**
 * Created by Sergey on 4/3/17.
 */

public class CreateTokenActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PUBLISHABLE_KEY = "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==";
    public static final String onBehalfOf = "";

    private EditText cardNumberEditText;
    private EditText expMonthEditText;
    private EditText expYearEditText;
    private EditText cvnEditText;
    private EditText amountEditText;
    private Button createTokenBtn;
    private CheckBox multipleUseCheckBox;
    private CheckBox shouldAuthenticateCheckBox;
    private TextView resultTextView;

    private boolean isMultipleUse;
    private boolean shouldAuthenticate;

    private static String tokenId;

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, CreateTokenActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_token);

        setActionBarTitle(getString(R.string.create_token));

        cardNumberEditText = (EditText) findViewById(R.id.cardNumberEditText_CreateTokenActivity);
        expMonthEditText = (EditText) findViewById(R.id.expMonthEditText_CreateTokenActivity);
        expYearEditText = (EditText) findViewById(R.id.expYearEditText_CreateTokenActivity);
        cvnEditText = (EditText) findViewById(R.id.cvnEditText_CreateTokenActivity);
        amountEditText = (EditText) findViewById(R.id.amountEditText_CreateTokenActivity);
        createTokenBtn = (Button) findViewById(R.id.createTokenBtn_CreateTokenActivity);
        multipleUseCheckBox = (CheckBox) findViewById(R.id.multipleUseCheckBox_CreateTokenActivity);
        shouldAuthenticateCheckBox = (CheckBox) findViewById(R.id.shouldAuthenticate_CreateTokenActivity);
        resultTextView = (TextView) findViewById(R.id.result_CreateTokenActivity);

        createTokenBtn.setOnClickListener(this);

        cardNumberEditText.setText(R.string.cardNumbTest);
        expMonthEditText.setText(R.string.expMonthTest);
        String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 1);
        expYearEditText.setText(year);
        cvnEditText.setText(R.string.cvnTest);
        amountEditText.setText(R.string.amountTest);
    }

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        final Xendit xendit = new Xendit(getApplicationContext(), PUBLISHABLE_KEY, this);

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        isMultipleUse = multipleUseCheckBox.isChecked();
        shouldAuthenticate = !shouldAuthenticateCheckBox.isChecked();

        Card card = new Card(cardNumberEditText.getText().toString(),
                expMonthEditText.getText().toString(),
                expYearEditText.getText().toString(),
                cvnEditText.getText().toString());

        Address billingAddress = new Address();
        billingAddress.setCountry("ID");
        billingAddress.setStreetLine1("Panglima Polim IV");
        billingAddress.setStreetLine2("Ruko Grand Panglima Polim, Blok E");
        billingAddress.setCity("Jakarta Selatan");
        billingAddress.setProvinceState("DKI Jakarta");
        billingAddress.setCategory("WORK");
        billingAddress.setPostalCode("123123");

        BillingDetails billingDetails = new BillingDetails();
        billingDetails.setMobileNumber("+6208123123123");
        billingDetails.setEmail("john@xendit.co");
        billingDetails.setGivenNames("John");
        billingDetails.setSurname("Hudson");
        billingDetails.setPhoneNumber("+6208123123123");
        billingDetails.setAddress(billingAddress);

        Address shippingAddress = billingAddress;
        Address[] customerAddress = { shippingAddress };

        Customer customer = new Customer();
        customer.setMobileNumber("+6208123123123");
        customer.setEmail("john@xendit.co");
        customer.setGivenNames("John");
        customer.setSurname("Hudson");
        customer.setPhoneNumber("+6208123123123");
        customer.setNationality("ID");
        customer.setDateOfBirth("1990-04-13");
        customer.setDescription("test user");
        customer.setAddresses(customerAddress);
        
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                progressBar.setVisibility(View.GONE);
                setTokenId(token.getId());
                Gson gson = new Gson();
                TokenizationResponse tokenizationResponse = new TokenizationResponse(token);
                String json = gson.toJson(tokenizationResponse);
                resultTextView.setText(json);
                Toast.makeText(CreateTokenActivity.this, "Status: " + token.getStatus(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(XenditError xenditError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateTokenActivity.this, xenditError.getErrorCode() + " " +
                        xenditError.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isMultipleUse) {
            xendit.createMultipleUseToken(card, onBehalfOf, billingDetails, customer, callback);
        } else {
            String amount = amountEditText.getText().toString();
            xendit.createSingleUseToken(card, amount, shouldAuthenticate, onBehalfOf, billingDetails, customer, "IDR", callback);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}