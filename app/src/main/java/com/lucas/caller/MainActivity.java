package com.lucas.caller;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    //拨号请求码
    public static final int REQUEST_CALL_PERMISSION = 10111;


    private Button button_dual_sim_settings;
    private Button button_interval_duration;
    private Button button_start_auto_dial;
    private Button button;
    private ImageView img;
    private ImageView img2;
    private RecyclerView recyclerView;
    private PhoneAdapter adapter;
    private List<PhoneBean> phoneList = new ArrayList<>();
    private int 间隔时间 = 3;
    private int 拨打卡号 = 0; // 0为sim1   1为sim2
    private int preSim = 0;

    Button uncalledButton;
    Button calledButton;
    Button button_add_from_clipboard;

    public int Position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        button_add_from_clipboard = findViewById(R.id.button_add_from_clipboard);
        button_dual_sim_settings = findViewById(R.id.button_dual_sim_settings);
        button_start_auto_dial = findViewById(R.id.button_start_auto_dial);
        button_interval_duration = findViewById(R.id.button_interval_duration);
        // 点击按钮触发文件选择操作
        button = findViewById(R.id.button_select_file);
        button.setOnClickListener(view -> pickExcelFile());
        img2 = findViewById(R.id.img2);
        img2.setOnClickListener(view -> {
            CustomDialog2 dialog = new CustomDialog2(this);
            dialog.show();
        });
        img = findViewById(R.id.img);
        img.setOnClickListener(view -> {
            CustomDialog dialog = new CustomDialog(this);
            dialog.show();
        });
        button_interval_duration.setOnClickListener(v -> {
            IntervalInputDialog dialog = new IntervalInputDialog(this);
            dialog.setOnIntervalSetListener(new IntervalInputDialog.OnIntervalSetListener() {
                @Override
                public void onIntervalSet(int interval) {
                    // 在这里处理用户输入的间隔参数
                    // interval 即为用户输入的间隔参数
                    间隔时间 = interval;
//                    ToastUtils.showShort("已设置间隔为" + interval + "秒");
                    button_interval_duration.setText("间隔时长（" + interval + "s）");

                }
            });
            dialog.show();

        });

        button_start_auto_dial.setOnClickListener(v -> {
            gotoCallPhone();

        });

//        设置拨打的sim卡
        button_dual_sim_settings.setOnClickListener(V->{
            if ( 拨打卡号 == 0) {
                拨打卡号 = 1;
                button_dual_sim_settings.setText("双卡设置（卡2）");
                return;
            }

            if ( 拨打卡号 == 1) {
                拨打卡号 = 2;
                button_dual_sim_settings.setText("双卡设置（卡12轮播）");
                return;
            }

            if ( 拨打卡号 == 2) {
                拨打卡号 = 0;
                button_dual_sim_settings.setText("双卡设置（卡1）");
                return;
            }

        });


        // 检查权限
        checkStoragePermission();
        checkReadPermission2(Manifest.permission.CALL_PHONE, REQUEST_CALL_PERMISSION);


        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhoneAdapter(phoneList);
        recyclerView.setAdapter(adapter);


        adapter.setOnPhoneItemClickListener(new PhoneAdapter.OnPhoneItemClickListener() {
            @Override
            public void onItemClick(int position) {

                ModifyPhoneInputDialog dialog = new ModifyPhoneInputDialog(MainActivity.this, phoneList.get(position).Phone);
                dialog.setOnIntervalSetListener(new ModifyPhoneInputDialog.OnModifySetListener() {
                    @Override
                    public void onModify(String newPhone) {
                        phoneList.get(position).Phone = newPhone;
                        adapter.notifyItemChanged(position);
                    }
                });
                dialog.show();


            }
        });


        uncalledButton = findViewById(R.id.uncalledButton);
        uncalledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Position = 0 ;
                updateList(false);
                calledButton.setAlpha(0.5f);
                uncalledButton.setAlpha(1f);
            }
        });

        calledButton = findViewById(R.id.calledButton);
        calledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Position = 1 ;
                updateList(true);
                calledButton.setAlpha(1f);
                uncalledButton.setAlpha(0.5f);
            }
        });



        // 导入剪切板
        button_add_from_clipboard.setOnClickListener(v->{
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
                CharSequence clipboardText = clipboardManager.getPrimaryClip().getItemAt(0).getText();
                if (!TextUtils.isEmpty(clipboardText)) {
                    List<PhoneBean> clipboardList = parseClipboardText(clipboardText.toString());

                    //添加到列表中
                    if (clipboardList != null && clipboardList.size() >0){
                        phoneList.addAll(0, clipboardList);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        });
    }


    private List<PhoneBean> parseClipboardText(String clipboardText) {
        List<PhoneBean> phoneList = new ArrayList<>();
        String[] lines = clipboardText.split("\n");
        for (String line : lines) {
            String[] parts = line.split(" ");
            if (parts.length == 2) {
                String name = parts[0];
                String phone = parts[1];
                PhoneBean phoneBean = new PhoneBean(name, phone);
                phoneList.add(phoneBean);
            }
        }
        return phoneList;
    }

    // 根据isCalled字段更新列表显示
    private void updateList(boolean isCalled) {
        List<PhoneBean> filteredList = new ArrayList<>();
        for (PhoneBean phone : phoneList) {
            if (phone.isCalled == isCalled) {
                filteredList.add(phone);
            }
        }
        adapter = new PhoneAdapter(filteredList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    // 检查存储权限
    private void checkStoragePermission() {
        // 如果Android版本在Marshmallow以上，需要动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                // 如果权限没有被授予，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                // 权限已经被授予
                // 在这里处理您的逻辑
            }
        } else {
            // 如果Android版本在Marshmallow以下，无需请求权限
            // 在这里处理您的逻辑
        }

    }


    // 启动文件选择器
    private void pickExcelFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.ms-excel");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Excel File"), PICK_FILE_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        readExcelFile(UriUtils.uri2File(uri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 读取Excel文件并将第一列和第二列的所有内容存储到两个列表中
    public void readExcelFile(File file) {
        List<PhoneBean> rawList = new ArrayList<>();

        try {
            // 创建工作簿对象
            Workbook workbook = Workbook.getWorkbook(file);

            // 获取第一个工作表
            Sheet sheet = workbook.getSheet(0);

            // 读取每一行，将第一列和第二列的值存储到列表中
            for (int i = 1; i < sheet.getRows(); i++) {
                Cell[] rowCells = sheet.getRow(i);
                if (rowCells.length >= 2) {
                    PhoneBean phoneBean = new PhoneBean(rowCells[0].getContents(), rowCells[1].getContents());
                    rawList.add(phoneBean);
                }
            }

            // 关闭工作簿
            workbook.close();

            phoneList.clear();
            phoneList.addAll(rawList);
            adapter.notifyDataSetChanged();
            rawList.clear();

            calledButton.setAlpha(0.5f);

        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }


    //打电话申请权限，
    public boolean checkReadPermission2(String string_permission, int request_code) {
        boolean flag = false;
//已有权限
        if (ContextCompat.checkSelfPermission(this, string_permission) == PackageManager.PERMISSION_GRANTED) {
            flag = true;
        } else {
//申请权限
            ActivityCompat.requestPermissions(this, new String[]{string_permission}, request_code);
        }
        return flag;
    }


    private final static String simSlotName[] = {
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx"};



    public void callPhone (int position) {
        if (拨打卡号 == 0) {
            callPhoneDetail(position, 0);
            preSim = 0;
            return;
        }

        if (拨打卡号 == 1) {
            callPhoneDetail(position, 1);
            preSim = 1;
            return;
        }

        if (拨打卡号 == 2) {
            if (preSim == 1){
                callPhoneDetail(position, 0);
                preSim = 0;
            }else {
                callPhoneDetail(position, 1);
                preSim = 1;
            }
            return;
        }
    }
    /**
     * 拨打电话（拨号权限自行处理）

     * @param simIndex ：sim卡的位置 0代表sim卡1，1代表sim卡2

     */

    public void callPhoneDetail(int position, int simIndex) {

        PhoneBean phoneBean = phoneList.get(position);
        phoneList.get(position).isCalled = true;
        String phoneNum = phoneBean.Phone;

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        //Add all slots here, according to device.. (different device require different key so put all together)
        for (String s : simSlotName)
            intent.putExtra(s, simIndex); //0 or 1 according to sim.......
        //works only for API >= 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(simIndex));
            } catch (Exception e) {
                e.printStackTrace();
                //writeLog("No Sim card? at slot " + simNumber+"\n\n"+e.getMessage(), this);
            }
        }
        startActivity(intent);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        // 处理事件
        button_dual_sim_settings.postDelayed(() -> {
            gotoCallPhone();
        }, 1000 * 间隔时间);

        button_dual_sim_settings.postDelayed(() -> {
            if (Position == 0) {
                updateList(false);
            }
            if (Position == 1) {
                updateList(true);
            }
        }, 250);
    }


    public void gotoCallPhone() {
        if (phoneList.size()<1) {
            ToastUtils.showShort("还未导入xls文件");
            return;
        }

        boolean allCalled = true;
        if (phoneList.size()>1) {
            for (PhoneBean phoneBean: phoneList) {
                if (!phoneBean.isCalled) {
                    allCalled = false;
                }
            }
            if (allCalled) {
                ToastUtils.showShort("列表已经全部拨打过电话！");
                return;
            }

        }


        for (int i = 0 ; i < phoneList.size(); i ++) {
            if (!phoneList.get(i).isCalled) {
                ToastUtils.showShort("开始拨打未拨打的电话号码");
                int finalI = i;
                button_dual_sim_settings.postDelayed(()->{
                    callPhone(finalI);
                }, 1000);

                break;
            }
        }
    }
}