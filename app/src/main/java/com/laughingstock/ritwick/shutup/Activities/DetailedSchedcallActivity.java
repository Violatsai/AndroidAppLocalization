package com.laughingstock.ritwick.shutup.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.*;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.laughingstock.ritwick.shutup.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DetailedSchedcallActivity extends AppCompatActivity
{
    ArrayAdapter<String> numspinneradapter;
    String number = "",name = "",photo = "",time="",date="",dialnumber="";
    long timeinmills;
    int repeatinterval=24;
    boolean ring=true,vibrate=true,repeatcall=false;
    ArrayList<String> diffnums;

    @BindView(R.id.schlistcontactphoto) CircleImageView photoimage;
    @BindView(R.id.cancelButton) ImageView cancelButton;
    @BindView(R.id.nametext) TextView nametext;
    @BindView(R.id.timetext) TextView timetext;
    @BindView(R.id.datetext) TextView datetext;
    @BindView(R.id.numtext)  TextView numtext;
    @BindView(R.id.repeatcalltext) TextView repeatcalltext;
    @BindView(R.id.ringcb) CheckBox ringcb;
    @BindView(R.id.vibratecb) CheckBox vibratecb;
    @BindView(R.id.repeatcallcb) CheckBox repeatcallcb;
    @BindView(R.id.numspinner) Spinner numspinner;
    @BindView(R.id.botcv) CardView botcv;
    @BindView(R.id.repeatcallsb) SeekBar repeatcallsb;
    @BindView(R.id.activity_detailed_schedcall) View root;

    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;

    boolean backpressedflag=false,edited=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_schedcall);
        ButterKnife.bind(this);

        Intent intent=getIntent();
        Bundle b=intent.getBundleExtra("sdatabundle");

        if(b!=null)
        {
            name=b.getString("name");
            diffnums=new ArrayList<>(b.getStringArrayList("numbers"));
            dialnumber=b.getString("dialnumber");
            photo=b.getString("photo");
            time=b.getString("time");
            date=b.getString("date");
            timeinmills=b.getLong("timeinmills");
            vibrate=b.getBoolean("vibrate");
            ring=b.getBoolean("ring");
            repeatcall=b.getBoolean("repeatcall");
            repeatinterval=b.getInt("repeatinterval");

            nametext.setText(name);
            photoimage.setImageURI(Uri.parse(photo));
            datetext.setText(getString(R.string.date)+date);
            datetext.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            datetext.setTextSize(16);
            timetext.setText(getString(R.string.time)+time);
            timetext.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            timetext.setTextSize(16);
            ringcb.setChecked(ring);
            vibratecb.setChecked(vibrate);
            repeatcallcb.setChecked(repeatcall);
            repeatcalltext.setText("Every "+repeatinterval+" hour"+((repeatinterval>1)?"s":""));

            numspinner.setVisibility(View.VISIBLE);
            numtext.setVisibility(View.VISIBLE);
            botcv.setVisibility(View.VISIBLE);
            datetext.setVisibility((repeatcall)?View.GONE:View.VISIBLE);
            repeatcalltext.setVisibility((repeatcall)?View.VISIBLE:View.GONE);
            repeatcallsb.setVisibility((repeatcall)?View.VISIBLE:View.GONE);

        }
        else
        {
            numspinner.setVisibility(View.GONE);
            numtext.setVisibility(View.GONE);
            botcv.setVisibility(View.GONE);
            repeatcallsb.setVisibility(View.GONE);
            repeatcalltext.setVisibility(View.GONE);
            diffnums=new ArrayList<>();

            Random r=new Random();
            String randimguri = "android.resource://"+getPackageName()+"/drawable/contactphoto"+(r.nextInt(5)+1);
            photo=randimguri;
            photoimage.setImageURI(Uri.parse(photo));
        }


        numspinneradapter = new ArrayAdapter<>(this, R.layout.spinner_item, diffnums);
        numspinneradapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        numspinner.setAdapter(numspinneradapter);
        numspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                dialnumber=diffnums.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {            }
        });

        vibratecb.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked)->vibrate=isChecked);

        ringcb.setOnCheckedChangeListener((CompoundButton buttonView,boolean isChecked)->ring=isChecked);

        repeatcallcb.setOnCheckedChangeListener((buttonView,isChecked)->
            {
                repeatcall=isChecked;
                datetext.setVisibility((repeatcall)?View.GONE:View.VISIBLE);
                repeatcalltext.setVisibility((repeatcall)?View.VISIBLE:View.GONE);
                repeatcallsb.setVisibility((repeatcall)?View.VISIBLE:View.GONE);
            }
        );

        View.OnLongClickListener l=new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                diffnums.clear();
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 1);
                return true;
            }
        };
        nametext.setOnLongClickListener(l);
        photoimage.setOnLongClickListener(l);

        repeatcallsb.setProgress(repeatinterval);
        repeatcallsb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if(progress<1)  progress=1;
                String temp;
                progress*=1.2;
                if(progress==1)
                    temp=getString(R.string.every) + " " + progress + " " + getString(R.string.hour);
                else
                    temp=getString(R.string.every) + " " + progress + " " + getString(R.string.hours);
                repeatcalltext.setText(temp);
                repeatinterval=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {    }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {       }
        });
    }

    public void donebuttonpressed(View v)
    {

        if((name.equals("")||time.equals("")||(date.equals("")) && !repeatcall))
        {
            Snackbar.make(root,getString(R.string.informationIncomplete),Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.iDontCare), (d)->
            {
                cancelButton.setVisibility(View.GONE);
                supportFinishAfterTransition();
            })
            .setActionTextColor(Color.parseColor("#2196F3"))
            .show();

        }
        else
        {
            decide(0);
            if(timeinmills/60000< System.currentTimeMillis()/60000 && !edited
                    && repeatcallcb.isChecked())
                decide(1);
            if(timeinmills/60000< System.currentTimeMillis()/60000 && !edited)
            {
                Toast.makeText(this, "Calling back in time is not yet possible.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent ri = new Intent();
            Bundle b = new Bundle();
            b.putString("name", name);
            b.putStringArrayList("numbers", diffnums);
            b.putString("dialnumber", dialnumber);
            b.putString("photo", photo);
            b.putString("time", time);
            b.putString("date", date);
            b.putLong("timeinmills", timeinmills);
            b.putBoolean("vibrate",vibrate);
            b.putBoolean("ring",ring);
            b.putBoolean("repeatcall",repeatcall);
            b.putInt("repeatinterval",repeatinterval);

            ri.putExtra("sdatabundle",b);
            setResult(RESULT_OK, ri);

            cancelButton.setVisibility(View.GONE);
            supportFinishAfterTransition();
        }
    }

    public void cancelbuttonpressed(View v)
    {
        cancelButton.setVisibility(View.GONE);
        supportFinishAfterTransition();
    }

    public void selectcontacttextclicked(View v)
    {
        if(nametext.getText().equals(getString(R.string.addContact)))
        {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == MainActivity.RESULT_OK)
            {

                Uri contactData = data.getData();

                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                if(cursor==null)    return;
                cursor.moveToFirst();
                String hasPhone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                if (Integer.parseInt(hasPhone) > 0)
                {
                    name="";
                    number="";
                    photo="";

                    Cursor phones = getContentResolver().query
                            (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                            + " = " + contactId, null, null);
                    while (phones!=null && phones.moveToNext())
                    {
                        String tempnum = phones.getString(phones.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[-() ]", "");

                        //number = number + (number.equals("") || number.contains(tempnum) ? "" : "###") + (number.contains(tempnum) ? "" : tempnum);

                        if(!diffnums.contains(tempnum))
                            diffnums.add(tempnum);

                        name = phones.getString(phones.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));//.replaceAll("[-() ]","");

                        photo = phones.getString(phones.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    }
                    phones.close();

                    Random r=new Random();
                    String randimguri = "android.resource://"+getPackageName()+"/drawable/contactphoto"+(r.nextInt(5)+1);
                    if(photo==null)
                        photo=randimguri;
                    photoimage.setImageURI(Uri.parse(photo));

                    nametext.setText(name);
                    numspinner.setVisibility(View.VISIBLE);
                    numtext.setVisibility(View.VISIBLE);
                    botcv.setVisibility(View.VISIBLE);
                    numspinneradapter.notifyDataSetChanged();
                    dialnumber=diffnums.get(0);
                    setTitle("Call "+name.split(" ")[0]);
                }
                else
                {
                    Toast.makeText(this, "This contact has no phone number", Toast.LENGTH_LONG).show();
                }
                cursor.close();


            }
        }
    }

    public void decide(int offset)
    {
        if(repeatcall)
        {
            int mYear,mMonth,mDay;
            Calendar mcurrentDate = Calendar.getInstance();
            mYear = mcurrentDate.get(Calendar.YEAR);
            mMonth = mcurrentDate.get(Calendar.MONTH);
            mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH)+offset;
            date=mDay+"/"+(++mMonth)+"/"+mYear;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        try {
            Date datentime = sdf.parse(time+" "+date);
            timeinmills = datentime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void settimetextclicked(View v)
    {
        int hour,minute;
        if(time.equals(""))
        {
            Calendar mcurrentTime = Calendar.getInstance();
            hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            minute = mcurrentTime.get(Calendar.MINUTE);
        }
        else
        {
            hour = Integer.parseInt(time.split(":")[0]);
            minute = Integer.parseInt(time.split(":")[1]);
        }

        timePickerDialog=new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker view, final int hourOfDay, final int minute)
            {
                time=hourOfDay+":"+minute;
                timetext.setText(getString(R.string.time)+time);
                timetext.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                timetext.setTextSize(16);
            }
        },hour,minute,false);
        timePickerDialog.show();

    }

    public void setdatetextclicked(View v)
    {
        int mYear,mMonth,mDay;
        if(date.equals(""))
        {
            Calendar mcurrentDate = Calendar.getInstance();
            mYear = mcurrentDate.get(Calendar.YEAR);
            mMonth = mcurrentDate.get(Calendar.MONTH);
            mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        }
        else
        {
            mYear = Integer.parseInt(date.split("/")[2]);
            mMonth = Integer.parseInt(date.split("/")[1]);
            mMonth--;
            mDay = Integer.parseInt(date.split("/")[0]);
        }
        datePickerDialog=new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
            {
                date=dayOfMonth+"/"+(++month)+"/"+year;
                month--;
                datetext.setText(getString(R.string.date)+date);
                datetext.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                datetext.setTextSize(16);

            }
        },mYear,mMonth,mDay);
        datePickerDialog.show();
    }

    @Override
    public void onBackPressed()
    {
        if(backpressedflag)
        {
            cancelButton.setVisibility(View.GONE);
            super.onBackPressed();
        }
        else
        {
            Snackbar.make(root,getString(R.string.pressBacktoCancel),Snackbar.LENGTH_LONG).show();
            backpressedflag=true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelButton.setVisibility(View.GONE);
                onBackPressed();
                break;
        }
        return true;
    }
}
