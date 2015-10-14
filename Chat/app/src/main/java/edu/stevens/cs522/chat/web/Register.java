package edu.stevens.cs522.chat.web;

import android.net.Uri;
import android.os.Parcel;

import java.util.UUID;

public class Register extends Request {
    public String string;

    public static final Creator<Register> CREATOR = new Creator<Register>() {
        public Register createFromParcel(Parcel source) {
            return new Register(source);
        }
        public Register[] newArray(int size) {
            return new Register[size];
        }
    };

    public Register(String serverUrl, UUID registrationId, String username) {
        super(serverUrl, registrationId, 0);
        string = username;
    }

    public Register(Parcel parcel) {
        super(parcel);
        string = parcel.readString();
    }

    @Override
    public Uri getRequestUri() {
        return Uri.parse(mServerUrl
                + "?username=" + string
                + "&regid=" + mRegistrationId.toString());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(string);
    }
}
