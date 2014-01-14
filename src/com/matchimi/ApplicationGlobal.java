package com.matchimi;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;

@ReportsCrashes(
    formKey = "", // This is required for backward compatibility but not used
    formUri = "http://www.bugsense.com/api/acra?api_key" + CommonUtilities.BUGSENSE_KEY
)

public class ApplicationGlobal extends Application {
	@Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}