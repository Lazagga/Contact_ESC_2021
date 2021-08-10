package com.example.contact_esc_2021;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Contact> datalist = new ArrayList<>();
    private ArrayList<Contact> filteredList = new ArrayList();
    private RecyclerView recyclerView;
    private Adapter adapter;
    private EditText search;
    private ImageButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.MainRecycler);
        search = findViewById(R.id.SearchTextView);
        add = findViewById(R.id.Add);

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                datalist.clear();
                //datalist = getContactList();
                adapter = new Adapter(MainActivity.this, datalist);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(adapter);

                search.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void afterTextChanged(Editable editable) {
                        String searchText = search.getText().toString();
                        searchFilter(searchText);
                    }
                });
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "권한 거부", Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("주소록을 불러오기 위해서는 주소록과 전화 접근 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
                .check();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        datalist = new ArrayList<>();
        datalist.clear();
        //datalist = getContactList();
        adapter = new Adapter(MainActivity.this, datalist);
        recyclerView.setAdapter(adapter);
    }

    public void searchFilter(String searchText) {
        filteredList.clear();
        for(int i = 0; i < datalist.size(); i++) {
            if(datalist.get(i).getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    datalist.get(i).getPhoneNumber().contains(searchText)){
                filteredList.add(datalist.get(i));
            }
        }
        adapter.filterList(filteredList);
        recyclerView.setAdapter(adapter);
    }
}