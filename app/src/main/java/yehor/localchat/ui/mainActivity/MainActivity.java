package yehor.localchat.ui.mainActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import io.reactivex.disposables.CompositeDisposable;
import yehor.localchat.CaptureActivityPortrait;
import yehor.localchat.R;
import yehor.localchat.data_base.Contact;
import yehor.localchat.data_base.DBProvider;
import yehor.localchat.model.MainInteractorRealize;
import yehor.localchat.presentation.MainPresenterRealize;
import yehor.localchat.ui.views_interfaces.MainView;


import static yehor.localchat.tags.Tags.IP;


public class MainActivity extends AppCompatActivity implements MainView {


    private Button mBtnSend, mBtnAdd, mBtnCopy, mBtnShare;

    private EditText mTextMessage;
    private Spinner mSpinnerIp;
    private TextView mTextView;
    private ArrayAdapter<Contact> adapter;
    private TextView mTextMyIP;

    ImageView mQrImage;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;


    private MainPresenterRealize presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mBtnSend = findViewById(R.id.button_send);
        mBtnCopy = findViewById(R.id.button_copy);
        mBtnShare = findViewById(R.id.button_share);
        mBtnAdd = findViewById(R.id.button_add);
        mTextView = findViewById(R.id.text_main);
        mTextMessage = findViewById(R.id.text_message);
        mTextMyIP = findViewById(R.id.my_ip);
        mSpinnerIp = findViewById(R.id.ip_spinner);


        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        mTextMyIP.append(ip);


        mBtnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ip", ip);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copy", Toast.LENGTH_LONG).show();
            }
        });


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);

        mBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRAlert(ip);
            }
        });

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> presenter.sendMessage(((Contact) mSpinnerIp.getSelectedItem()).getIp(), mTextMessage.getText().toString())).start();
            }
        });

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startContactAddAlert("");
            }
        });
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        presenter = new MainPresenterRealize(new MainInteractorRealize(DBProvider.getDatabase(getApplicationContext())), this);
        presenter.startReceive();
        presenter.getAllContacts();

    }

    private void startContactAddAlert(String ip) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        alertDialogBuilder.setCancelable(false);
        View v = inflater.inflate(R.layout.alert_dialog, null);

        EditText text_name = v.findViewById(R.id.text_name);
        EditText text_ip = v.findViewById(R.id.text_ip);
        Button button_add = v.findViewById(R.id.add_button);
        Button button_cancel = v.findViewById(R.id.cancel_button);

        text_ip.setText(ip);

        alertDialogBuilder.setView(v);

        this.runOnUiThread(() -> {
            AlertDialog alertDialog = alertDialogBuilder.create();

            button_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.insertContact(text_name.getText().toString(), text_ip.getText().toString());
                    alertDialog.cancel();
                }
            });

            button_cancel.setOnClickListener(view -> alertDialog.cancel());

            alertDialog.show();
        });
    }


    public void ScanButton(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                startContactAddAlert(intentResult.getContents());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void startQRAlert(String ip) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.qr_layout, null);

        mQrImage = v.findViewById(R.id.QR_Image);
        String inputValue = ip.trim();
        alertDialogBuilder.setView(v);

        this.runOnUiThread(() -> {
            AlertDialog alertDialog = alertDialogBuilder.create();

            if (inputValue.length() > 0) {
                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3 / 4;

                qrgEncoder = new QRGEncoder(
                        inputValue, null,
                        QRGContents.Type.TEXT,
                        smallerDimension);
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ip", ip);
                    clipboard.setPrimaryClip(clip);
                    bitmap = qrgEncoder.encodeAsBitmap();
                    mQrImage.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    Log.v("GenerateQRCode", e.toString());
                }
            } else {
            }
            alertDialog.show();
        });

    }

    @Override
    protected void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    private void init_spinner_dropdown(Spinner s, List<Contact> values) {
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
    }

    @Override
    public void initContactAdapter(List<Contact> contactsList) {
        init_spinner_dropdown(mSpinnerIp, contactsList);
    }

    @Override
    public void appendMessage(String s) {
        mTextView.append(s);

    }

    @Override
    public void addSpinnerItem(Contact contact) {
        adapter.add(contact);
    }

}