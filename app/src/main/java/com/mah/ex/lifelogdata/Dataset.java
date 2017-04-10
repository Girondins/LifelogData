package com.mah.ex.lifelogdata;

import org.joda.time.DateTime;

/**
 * Created by Girondins on 2017-04-08.
 */

public class Dataset {
    private int steps,com,bros;
            double cals;

    public Dataset(int steps,int com, int bros,double cals){
        this.steps = steps;
        this.com = com;
        this.bros = bros;
        this.cals = cals;
    }

    public int getSteps(){
        return this.steps;
    }

    public int getCom() {
        return this.com;
    }

    public int getBros() {
        return this.bros;
    }

    public int getCals() {
        return (int) this.cals;
    }
}
