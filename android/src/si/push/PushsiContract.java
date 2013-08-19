package si.push;

import android.provider.BaseColumns;


public final class PushsiContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PushsiContract() {}

    /* Inner class that defines the table contents */
    public static abstract class Alert implements BaseColumns {
        public static final String TABLE_NAME = "pushsi_alert";
        public static final String COLUMN_NAME_CREATED = "created";
    }

    /* Inner class that defines the table contents */
    public static abstract class AlertData implements BaseColumns {
        public static final String TABLE_NAME = "pushsi_alert_data";
        public static final String COLUMN_NAME_ALERT_ID = "alert_id";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VALUE = "value";
    }
}