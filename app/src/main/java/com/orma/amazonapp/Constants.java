package com.orma.amazonapp;

import android.os.Environment;


public class Constants {

    public static String BASE_URL = "https://www.trovacontributi.it/app/";
    public static String BASE_SITE_URL = "https://www.trovacontributi.it/";

    public static String LINKEDIN_URL = "https://www.linkedin.com/company/7120114";
    public static String FACEBOOK_URL = "https://www.facebook.com/soulsrl/";
    public static String TWITTER_URL = "https://www.twitter.com/soul_srl";

    public static class DRAWER {
        public static final int AMAZON = 1;
        public static final int PRODOTTI_TRACCIATI = 2;
        public static final int OFFERTE = 3;
    }





    public static class VIEW {
        public static final int CALLS = 1;
        public static final int LEADS = 2;
        public static final int CALENDAR = 3;
        public static final int CUSTOMERS = 4;
        public static final int BANDI = 5;
    }

    public static class BANDI {
        public static final int NORMALI = 1;
        public static final int PRIMO_PIANO = 2;
    }

    public static class ACTIVITY_RESULTS {
        public static final int ESITO_CALL = 1;
        public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 2;
    }

    public static class SHARED {
        public final static String USER      = "s_user";
    }

    public static class PERMISSION {
        public final static int CALL = 1;
    }
}
