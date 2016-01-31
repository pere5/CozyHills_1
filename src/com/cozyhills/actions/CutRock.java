package com.cozyhills.actions;

import com.cozyhills.model.Person;
import com.cozyhills.model.Rock;

/**
 * Created by periks15 on 2016-01-28.
 */
public class CutRock implements Action {

    private int cut = 10;
    private Rock rock = null;

    public CutRock(Rock rock) {
        this.rock = rock;
    }

    @Override
    public boolean doIt(Person me) {
        cut--;
        return cut > 0;
    }
}
