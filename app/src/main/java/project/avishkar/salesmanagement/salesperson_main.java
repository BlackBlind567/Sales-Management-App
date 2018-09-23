package project.avishkar.salesmanagement;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alexzaitsev.meternumberpicker.MeterNumberPicker;
import com.alexzaitsev.meternumberpicker.MeterView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class salesperson_main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{


    private ProgressBar spinner;
    private DatabaseReference databaseReference;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList <InventoryItem> list;
    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesperson_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab=findViewById(R.id.fab_salesperson);
        spinner = (ProgressBar) findViewById(R.id.progressBar3);
        swipeRefreshLayout=findViewById(R.id.swiperefresh1);

        SessionManager sm = new SessionManager(getApplicationContext());
        HashMap<String, String> details = sm.getUserDetails();
        final String id = details.get("id");
        final String role = details.get("role");
        databaseReference = FirebaseDatabase.getInstance().getReference(role);
        mRecyclerView = findViewById(R.id.items_list1);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList(id,role,false);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> itemlist=new ArrayList<>();
                itemlist=getItemList(id,role);

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(salesperson_main.this);
                final View mView = getLayoutInflater().inflate(R.layout.activity_selling, null);
                final AutoCompleteTextView autoCompleteTextView;
                final MeterView numberPicker;
                Button ok = (Button) mView.findViewById(R.id.ok);
                autoCompleteTextView = mView.findViewById(R.id.autoCompleteTextView1);
                numberPicker=mView.findViewById(R.id.sold_number);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mView.getContext(),R.layout.support_simple_spinner_dropdown_item,itemlist);

                autoCompleteTextView.setThreshold(1);
                autoCompleteTextView.setAdapter(adapter);
                mBuilder.setView(mView);
                mBuilder.setTitle("Select the item to sell");
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                final ArrayList<String> finalItemlist = itemlist;
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do selling work here

                        int sold = numberPicker.getValue();
                        String itemName = autoCompleteTextView.getText().toString();

                        if(TextUtils.isEmpty(itemName))
                        {

                            Toast.makeText(getApplicationContext(),"Please fill all the details!", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            int i1;
                            for(i1 = 0; i1< finalItemlist.size(); i1++)
                            {
                                if(finalItemlist.get(i1).equals(itemName))
                                    break;
                            }
                            if(i1 == finalItemlist.size()){
                                Toast.makeText(getApplicationContext(),"Please select among available items only!", Toast.LENGTH_LONG).show();

                            }
                            else
                            {

                            }
                        }

                        dialog.dismiss();


                    }
                });
            }
        });



        spinner.setVisibility(View.VISIBLE);

        list = new ArrayList<>();
        databaseReference.child(id).child("Inventory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            InventoryItem it1 = snapshot.getValue(InventoryItem.class);
                            list.add(it1);
                        }
                        mAdapter=new SalespersonInventoryAdapter(getApplicationContext(),list);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        spinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headView = navigationView.getHeaderView(0);
        final TextView headerSalespersonName = headView.findViewById(R.id.SalespersonName);
        final TextView headerSalespersonEmail = headView.findViewById(R.id.SalespersonMail);
        final ImageView headerSalespersonImage = headView.findViewById(R.id.imageView);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Salesperson");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if(snapshot.getKey().equals(id)){
                        SalesPerson sm = snapshot.getValue(SalesPerson.class);
                        headerSalespersonName.setText(sm.getName());
                        headerSalespersonEmail.setText(sm.getEmailId());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<String> getItemList(String id, String role) {
        final ArrayList<String> itemlist=new ArrayList<>();
        databaseReference.child(id).child("Inventory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            InventoryItem it1 = snapshot.getValue(InventoryItem.class);
                            itemlist.add(it1.getItemName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        return itemlist;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            // exit dialog box
            new FancyAlertDialog.Builder(this)
                    .setTitle("Warning!!!")
                    .setBackgroundColor(Color.parseColor("#303F9F"))  //Don't pass R.color.colorvalue
                    .setMessage("Do you really want to Exit ?")
                    .setNegativeBtnText("No")
                    .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnText("Yes")
                    .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                    .setAnimation(Animation.POP)
                    .isCancellable(true)
                    .setIcon(R.drawable.ic_error_outline_black_24dp, Icon.Visible)
                    .OnPositiveClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {
                            salesperson_main.super.onBackPressed();
                        }
                    })
                    .OnNegativeClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {

                        }
                    })
                    .build();
            // super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.salesperson_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.dashboard) {

        } else if (id == R.id.my_account) {

            //show the salesperson's myaccount (used same class AccountManager for salesperson also)
            Intent intent = new Intent(salesperson_main.this,AccountManager.class);
            startActivity(intent);

        } else if (id == R.id.leaderboard) {

        } else if (id == R.id.statistics) {

        } else if (id == R.id.nav_share) {

            //Share app with others
            ApplicationInfo api = getApplicationContext().getApplicationInfo();
            String apkpath = api.sourceDir;
            Intent share_intent = new Intent(Intent.ACTION_SEND);
            share_intent.setType("application/vnd.android.package-archive");
            share_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkpath)));
            startActivity(Intent.createChooser(share_intent, "Share app using"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void updateList(String id, String role, final boolean spin)
    {
        if(spin==true)
        {
            spinner.setVisibility(View.VISIBLE);
        }

        final ArrayList<InventoryItem> list= new ArrayList<>();
        databaseReference.child(id).child("Inventory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            InventoryItem it1 = snapshot.getValue(InventoryItem.class);
                            list.add(it1);
                        }
                        mAdapter=new SalespersonInventoryAdapter(getApplicationContext(), list);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        if(spin==true)
                        spinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
