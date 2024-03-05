package com.example.bt_android_qt_3;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.widget.LinearLayout;
import android.content.Intent;
import java.util.List;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    private ListView folderListView;
    private Button upButton, selectButton, exitButton;
    private ArrayAdapter<String> adapter;
    private String currentFolderPath;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private MediaPlayer mediaPlayer;
    private LinearLayout buttonsLayout;
    private View activity2Container;
    private ArrayList<ListItem> itemsList = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        folderListView = findViewById(R.id.folderListView);
        upButton = findViewById(R.id.upButton);
        selectButton = findViewById(R.id.selectButton);
        exitButton = findViewById(R.id.exitButton);
        buttonsLayout = findViewById(R.id.buttonsLayout);
        activity2Container = findViewById(R.id.activity2Container);

        activity2Container.setVisibility(View.GONE);
        // Kiểm tra quyền khi chạy ứng dụng
        checkStoragePermission();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        folderListView.setAdapter(adapter);

        itemsList = new ArrayList<>(); // Initialize the items list

        // Get the external storage directory and display its contents
        currentFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        displayFolderContents(currentFolderPath);

        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < itemsList.size()) {
                    String selectedItem = itemsList.get(position).getName();
                    String selectedPath = currentFolderPath + File.separator + selectedItem;
                    File clickedFile = new File(selectedPath);

                    if (clickedFile.isDirectory()) {
                        // Clicked item is a folder, navigate into it
                        currentFolderPath = selectedPath;
                        displayFolderContents(currentFolderPath);
                    } else {
                        launchActivity2(clickedFile.getAbsolutePath());

                    }
                }
            }
        });

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate up to the parent folder
                File currentFolder = new File(currentFolderPath);
                String parentFolderPath = currentFolder.getParent();
                if (parentFolderPath != null) {
                    currentFolderPath = parentFolderPath;
                    displayFolderContents(currentFolderPath);
                }
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement your logic for selecting all music files in the current folder
                selectAllMusic();
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement your logic for exiting the application
                finish();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Set the visibility of elements based on your logic
        buttonsLayout.setVisibility(View.VISIBLE);
        activity2Container.setVisibility(View.GONE);
    }
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        PERMISSION_REQUEST_CODE);
            } else {
                // Permission granted, display content
                currentFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                displayFolderContents(currentFolderPath);
            }
        } else {
            // For devices with Android version lower than M, no need to check permissions
            currentFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            displayFolderContents(currentFolderPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, display content
                currentFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                displayFolderContents(currentFolderPath);
            } else {
                // Permission not granted, handle accordingly (e.g., show an error message)
                Toast.makeText(this, "Ứng dụng cần quyền để hoạt động.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayFolderContents(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            itemsList.clear();

            for (File file : files) {
                String itemName = file.getName();
                String itemPath = file.getAbsolutePath();

                if (file.isDirectory()) {
                    itemsList.add(new ListItem(itemName, itemPath, ItemType.FOLDER));
                } else {
                    // Kiểm tra nếu là file nhạc
                    if (isMusicFile(itemName)) {
                        itemsList.add(new ListItem(itemName, itemPath, ItemType.MUSIC_FILE));
                    } else {
                        itemsList.add(new ListItem(itemName, itemPath, ItemType.OTHER_FILE));
                    }
                }
            }

            CustomArrayAdapter customAdapter = new CustomArrayAdapter(this, itemsList);
            folderListView.setAdapter(customAdapter);
        }
    }
    private boolean isMusicFile(String fileName) {
        // Kiểm tra nếu là file nhạc dựa trên đuôi mở rộng (vd: .mp3, .wav, ...)
        // Bạn cần thêm các định dạng file nhạc mà bạn hỗ trợ vào đây
        return fileName.endsWith(".mp3") || fileName.endsWith(".wav");
    }
    private void launchActivity2(String filePath) {
        Intent intent = new Intent(MainActivity.this, Activity2.class);
        intent.putExtra("filePath", filePath);
        startActivity(intent);
    }



    private void selectAllMusic() {
        ArrayList<String> selectedMusicFiles = new ArrayList<>();

        for (ListItem item : itemsList) {
            if (item.getItemType() == ItemType.MUSIC_FILE) {
                selectedMusicFiles.add(item.getPath());
            }
        }

        if (!selectedMusicFiles.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, Activity2.class);
            intent.putStringArrayListExtra("selectedMusicFiles", selectedMusicFiles);
            intent.putExtra("currentFolderPath", currentFolderPath);
            startActivity(intent);

            StringBuilder selectedFilesMessage = new StringBuilder("Selected music files:\n");
            for (String filePath : selectedMusicFiles) {
                selectedFilesMessage.append(filePath).append("\n");
            }
            Toast.makeText(this, selectedFilesMessage.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No music files found in the current folder", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}