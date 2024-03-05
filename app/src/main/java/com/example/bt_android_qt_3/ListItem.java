package com.example.bt_android_qt_3;

public class ListItem {
    private String name;
    private String path;
    private ItemType itemType;

    public ListItem(String name, String path, ItemType itemType) {
        this.name = name;
        this.path = path;
        this.itemType = itemType;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public int getIconResId() {
        switch (itemType) {
            case FOLDER:
                return R.drawable.ic_folder;
            case MUSIC_FILE:
                return R.drawable.default_album_image;
            default:
                return R.drawable.ic_launcher_background; // You can set a default icon here
        }
    }
}
