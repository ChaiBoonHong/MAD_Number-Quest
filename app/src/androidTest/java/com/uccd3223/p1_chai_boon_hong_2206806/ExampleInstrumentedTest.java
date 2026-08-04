package com.uccd3223.p1_chai_boon_hong_2206806;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        String packageName = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getPackageName();
        assertEquals("com.uccd3223.p1_chai_boon_hong_2206806", packageName);
    }
}
