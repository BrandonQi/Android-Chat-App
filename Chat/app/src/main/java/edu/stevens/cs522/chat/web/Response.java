package edu.stevens.cs522.chat.web;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;

import java.io.IOException;
import java.net.HttpURLConnection;

public abstract class Response implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Response createFromParcel(Parcel parcel) {
            ResponseType responseType = ResponseType.valueOf(parcel.readString());
            switch (responseType) {
                case REGISTER:
                    return new RegisterResponse(parcel);
                case SYNCHRONIZE:
                    return new SynchronizeResponse(parcel);
            }
            throw new IllegalArgumentException("Unknown request type: "
                    + responseType.name());
        }

        public Response[] newArray(int size) {
            return new Response[size];
        }
    };
    public int mHttpResponseCode = 0;
    public String mHttpResponseMessage = "";

    public Response(HttpURLConnection connection)
            throws IOException {
        mHttpResponseCode = connection.getResponseCode();
        mHttpResponseMessage = connection.getResponseMessage();
    }

    public Response(Parcel parcel) {
        mHttpResponseCode = parcel.readInt();
        mHttpResponseMessage = parcel.readString();
    }

    protected static void matchName(String name, JsonReader reader)
            throws IOException {
        String label = reader.nextName();
        if (!label.equals(name)) {
            throw new IOException("Error in Response Entity: expected "
                    + name + "encountered " + label);
        }
    }

    protected void parseResponse(JsonReader reader)
            throws IOException {
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mHttpResponseCode);
        parcel.writeString(mHttpResponseMessage);
    }

    public int describeContents() {
        return 0;
    }

    public static enum ResponseType {
        REGISTER,
        SYNCHRONIZE
    }

    public static class RegisterResponse extends Response {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public RegisterResponse createFromParcel(Parcel parcel) {
                parcel.readString();
                return new RegisterResponse(parcel);
            }

            public RegisterResponse[] newArray(int size) {
                return new RegisterResponse[size];
            }
        };
        public long mClientId;

        public RegisterResponse(HttpURLConnection connection, JsonReader reader)
                throws IOException {
            super(connection);
            parseResponse(reader);
        }

        public RegisterResponse(Parcel parcel) {
            super(parcel);
            mClientId = parcel.readLong();
        }

        @Override
        protected void parseResponse(JsonReader reader) throws IOException {
            reader.beginObject();
            matchName("id", reader);
            mClientId = reader.nextInt();

            reader.endObject();
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(ResponseType.REGISTER.name());
            super.writeToParcel(parcel, flags);
            parcel.writeLong(mClientId);
        }
    }


    public static class SynchronizeResponse extends Response {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public SynchronizeResponse createFromParcel(Parcel parcel) {
                parcel.readString();
                return new SynchronizeResponse(parcel);
            }

            public SynchronizeResponse[] newArray(int size) {
                return new SynchronizeResponse[size];
            }
        };
        HttpURLConnection mHttpURLConnection;
        public SynchronizeResponse(HttpURLConnection connection)
                throws IOException {
            super(connection);
            mHttpURLConnection = connection;
        }

        public SynchronizeResponse(Parcel parcel) {
            super(parcel);
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(ResponseType.SYNCHRONIZE.name());
            super.writeToParcel(parcel, flags);
        }
    }
}
