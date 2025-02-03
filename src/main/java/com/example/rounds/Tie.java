package com.example.rounds;

import java.util.ArrayList;
import java.util.List;

import com.example.clubs.Club;

public abstract class Tie {
    protected List<Club> clubs1 = new ArrayList<>();
    protected List<Club> clubs2 = new ArrayList<>();

    public Tie(List<Club> clubs1, List<Club> clubs2) {
        this.clubs1 = clubs1;
        this.clubs2 = clubs2;
    }

    public abstract void play();
}