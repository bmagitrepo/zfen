package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 30.04.15.
 */

public class Tax extends BaseItem implements Parcelable {
    private static Calendar calendar = Calendar.getInstance();

    @SerializedName("user_id")
    protected int userId;
    @SerializedName("country_id")
    protected int countryId = -1;
    @SerializedName("arrival")
    protected long arrival;
    @SerializedName("departure")
    protected long departure = -1;
    private transient String countryName;
    private transient String countryCode;
    private transient String countryFlag;
    private transient long displayArrival;
    private transient long displayDeparture;
    private transient int daysCount = -1;

    public Tax() {
    }

    public Tax(Tax tax) {
        this.id = tax.id;
        this.remoteId = tax.remoteId;
        this.status = tax.status;
        this.updateTime = tax.updateTime;
        this.userId = tax.userId;
        this.countryId = tax.countryId;
        this.arrival = tax.arrival;
        this.departure = tax.departure;
        this.countryName = tax.countryName;
        this.countryCode = tax.countryCode;
        this.countryFlag = tax.countryFlag;
        this.displayArrival = tax.displayArrival;
        this.displayDeparture = tax.displayDeparture;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public long getArrival() {
        return arrival;
    }

    public long getArrivalMS() {
        return getArrival() * 1000L;
    }

    public int getArrivalYear() {
        calendar.setTimeInMillis(getArrivalMS());
        return calendar.get(Calendar.YEAR);
    }

    public int getArrivalDay() {
        calendar.setTimeInMillis(getArrivalMS());
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    public void setArrival(long arrival) {
        this.arrival = arrival;
    }

    public long getDeparture() {
        return departure;
    }

    public boolean isAutoCheckIn() {
        return getDeparture() == -1;
    }

    /**
     * @return departure time in seconds, but in case of auto check in({@link #departure} = -1) it returns now
     */
    public long getDeparturewWithAuto() {
        if (!isAutoCheckIn()) {
            return getDeparture();
        } else {
            // now is the end of the trip for auto check in
            return Calendar.getInstance().getTimeInMillis() / 1000;
        }
    }

    public long getDepartureMS() {
        return getDeparturewWithAuto() * 1000L;
    }

    public int getDepartureYear() {
        calendar.setTimeInMillis(getDepartureMS());
        return calendar.get(Calendar.YEAR);
    }

    public int getDepartureDay() {
        calendar.setTimeInMillis(getDepartureMS());
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    public void setDeparture(long departure) {
        this.departure = departure;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryFlag() {
        return countryFlag;
    }

    public void setCountryFlag(String countryFlag) {
        this.countryFlag = countryFlag;
    }

    /**
     * contains filtered or split by years value
     *
     * @return
     * @see com.zoomlee.zoo.dao.TaxDaoHelper#getTaxInInterval(android.content.Context, long, long)
     */
    public long getDisplayArrival() {
        return displayArrival;
    }

    public long getDisplayArrivalMS() {
        return 1000L * getDisplayArrival();
    }

    public int getDisplayArrivalYear() {
        calendar.setTimeInMillis(getDisplayArrivalMS());
        return calendar.get(Calendar.YEAR);
    }

    public void setDisplayArrival(long displayArrival) {
        this.displayArrival = displayArrival;
    }

    /**
     * contains filtered or split by years value
     *
     * @return
     * @see com.zoomlee.zoo.dao.TaxDaoHelper#getTaxInInterval(android.content.Context, long, long)
     */
    public long getDisplayDeparture() {
        return displayDeparture;
    }

    public long getDisplayDepartureMS() {
        return 1000L * getDisplayDeparture();
    }

    public int getDisplayDepartureYear() {
        calendar.setTimeInMillis(getDisplayDepartureMS());
        return calendar.get(Calendar.YEAR);
    }

    public void setDisplayDeparture(long displayDeparture) {
        this.displayDeparture = displayDeparture;
    }

    public int getDaysCount() {
        if (daysCount == -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(getDisplayArrivalMS()));
            int startDay = calendar.get(Calendar.DAY_OF_YEAR) - 1;

            calendar.setTime(new Date(getDisplayDepartureMS()));
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);

            daysCount = endDay - startDay;
        }
        return daysCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tax tax = (Tax) o;

        if (remoteId != -1 && tax.remoteId != -1)
            return remoteId == tax.remoteId;

        return id == tax.id;
    }

    @Override
    public int hashCode() {
        return 31 * remoteId;
    }

    @Override
    public String toString() {
        return "Tax{" +
                "userId=" + userId +
                ", countryId=" + countryId +
                ", arrival=" + arrival +
                ", departure=" + departure +
                ", countryName='" + countryName + '\'' +
                ", displayArrival=" + displayArrival +
                ", displayDeparture=" + displayDeparture +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeInt(this.countryId);
        dest.writeLong(this.arrival);
        dest.writeLong(this.departure);
        dest.writeString(this.countryName);
        dest.writeLong(this.displayArrival);
        dest.writeLong(this.displayDeparture);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.status);
        dest.writeInt(this.updateTime);
    }

    private Tax(Parcel in) {
        this.userId = in.readInt();
        this.countryId = in.readInt();
        this.arrival = in.readLong();
        this.departure = in.readLong();
        this.countryName = in.readString();
        this.displayArrival = in.readLong();
        this.displayDeparture = in.readLong();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.status = in.readInt();
        this.updateTime = in.readInt();
    }

    public static final Parcelable.Creator<Tax> CREATOR = new Parcelable.Creator<Tax>() {
        public Tax createFromParcel(Parcel source) {
            return new Tax(source);
        }

        public Tax[] newArray(int size) {
            return new Tax[size];
        }
    };
}
