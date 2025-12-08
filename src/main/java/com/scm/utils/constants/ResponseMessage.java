package com.scm.utils.constants;

public class ResponseMessage {
    //Response Code
    public static final String SUCCESS="0x0200";
    public static final String FAILED="0x0205";
    public static final String BAD_REQUEST="0x0400";
    public static final String UNAUTHORISED="0x0401";
    public static final String MISSING_PARAMETER="0x0403";
    public static final String NOT_FOUND="0x0404";
    public static final String SOMETHING_WENT_WRONG="0x0405";
    public static final String CONFLICT="0x0409";


    //Response key
    public static final String CODE="code";
    public static final String STATUS="status";
    public static final String DESCRIPTION="description";

    //Response Message Status
    public static final String STATUS_SUCCESS="Success";
    public static final String STATUS_FAILED="Failed";
    public static final String STATUS_PENDING="Pending";

    //Response Description
    public static final String REGISTERED_SUCCESSFULLY="User is registered successfully";
    public static final String DESC_SOMETHING_WENT_WRONG="Something Went Wrong, Try Again";

    //Roles
    public static final String ROLE_USER="ROLE_USER";

    // page size
    public static final int PAGE_SIZE=10;
}
