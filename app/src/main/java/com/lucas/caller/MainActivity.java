package com.lucas.caller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UriUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private RecyclerView recyclerView;
    private PhoneAdapter adapter;
    private List<PhoneBean> phoneList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 点击按钮触发文件选择操作
        findViewById(R.id.button_select_file).setOnClickListener(view -> pickExcelFile());

        // 检查权限
        checkStoragePermission();






        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhoneAdapter(phoneList);
        recyclerView.setAdapter(adapter);


        Button uncalledButton = findViewById(R.id.uncalledButton);
        uncalledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(false);
            }
        });

        Button calledButton = findViewById(R.id.calledButton);
        calledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(true);
            }
        });
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
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
            for (int i = 0; i < sheet.getRows(); i++) {
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

        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }


}