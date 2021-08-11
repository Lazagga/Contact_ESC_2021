package com.example.contact_esc_2021;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.DialogTitle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

    private Context context;
    private ArrayList<Contact> datalist;

    public Adapter(Context context, ArrayList<Contact> datalist){
        this.context = context;
        this.datalist = datalist;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public Holder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull Adapter.Holder holder, int position) {

        Contact contact = datalist.get(position);
        holder.name.setText(contact.getName());
        holder.phoneNum.setText(contact.getPhoneNumber());

        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("전화/문자");
                builder.setMessage("전화를 걸지 문자를 보낼지 선택해 주십시오.");
                builder.setNeutralButton("전화", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String number = datalist.get(position).getPhoneNumber();
                        Uri U_number = Uri.parse("tel:"+number);
                        Intent call = new Intent(Intent.ACTION_CALL, U_number);
                        context.startActivity(call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                });
                builder.setPositiveButton("문자", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri U_sms = Uri.parse("tel:" + contact.getPhoneNumber());
                        Intent intent = new Intent(Intent.ACTION_VIEW, U_sms);
                        intent.putExtra("address", contact.getPhoneNumber());
                        intent.putExtra("smsBody", "");
                        intent.setType("vnd.android-dir/mms-sms");
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                });
                builder.create().show();
            }
        });

        holder.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("삭제");
                builder.setMessage("정말 이 연락처를 삭제하시겠습니까?");
                builder.setNegativeButton("아니오", null);
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteContactFromNumber(context.getContentResolver(), datalist.get(position).getPhoneNumber());
                        datalist.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, datalist.size());
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        protected ConstraintLayout itemContainer;
        protected TextView name;
        protected TextView phoneNum;
        protected Button call;

        public Holder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);

            itemContainer = itemView.findViewById(R.id.ItemContainer);
            name = itemView.findViewById(R.id.Name);
            phoneNum = itemView.findViewById(R.id.PhoneNumber);
            call = itemView.findViewById(R.id.call);
        }
    }

    public void filterList(ArrayList<Contact> filteredList) {
        datalist = filteredList;
        notifyDataSetChanged();
    }

    private static long getContactIDFromNumber(ContentResolver contactHelper, String number) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI., Uri.encode(number));

        String[] projection = {ContactsContract.PhoneLookup._ID};

        Cursor cursor = contactHelper.query(contactUri, projection, null, null, null);

        if(cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
        }
        else if(cursor != null) {
            cursor.close();
        }
        return -1;
    }

    public static void deleteContactFromNumber(ContentResolver contactHelper, String number) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String[] WhereArgs = new String[] {String.valueOf(getContactIDFromNumber(contactHelper, number))};

        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", WhereArgs).build());

        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
