package com.example.alvar.chatapp;

public class Constant {

    //Const for options intent in Alert Dialog (image, pdf, word)
    public static final String IMAGE_OPTION = "image/*";
    public static final String SELECT_IMAGE = "SELECT IMAGE";
    public static final String PDF_OPTION = "application/pdf";
    public static final String SELECT_PDF = "SELECT PDF FILE";
    public static final String WORD_DOCUMENT_OPTION = "application/msword";
    public static final String SELECT_WORD_DOCUMENT = "SELECT WORD DOCUMENT";
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 555;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9004;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    // File request const
    public static final int CHAT_IMAGE_MENU_REQUEST = 1;
    public static final int CHAT_PDF_MENU_REQUEST = 2;
    public static final int CHAT_DOCX_MENU_REQUEST = 3;
    //galley const
    public static final int GALLERY_REQUEST_NUMBER = 1;
    // intents
    public static final String CONTACT_ID = "contactID";
    public static final String CONTACT_NAME = "contactName";
    public static final String CONTACT_IMAGE = "contactImage";
    public static final String CHATROOM_ID = "chatroomID";
    public static final String DOCUMENT_ID = "documentID";
    //fragment location
    public static final String LOCATION_USER_LAT = "latitude1";
    public static final String LOCATION_USER_LON = "longitude1";
    public static final String LOCATION_CONTACT_LAT = "latitude2";
    public static final String LOCATION_CONTACT_LON = "longitude2";
    //gMaps
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    // read file permission
    public static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 20;
    //phone Login AND Register Activity
    public static final String DEFAULT_EMAIL = "email";
    public static final String DEFAULT_STATUS = "Hi there I am using ChatUp";
    public static final String DEFAULT_IMAGE = "image";
    public static final String DEFAULT_THUMBNAIL = "imgThumbnail";
    public static final String DEFAULT_PASSWORD = "null";

}
