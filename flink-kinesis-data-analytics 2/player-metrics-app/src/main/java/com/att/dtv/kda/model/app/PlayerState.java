package com.att.dtv.kda.model.app;

public enum PlayerState {
    unknown(0), play(1), pause(2), interrupt(3), resume(4), complete(5), buffer_start(6),
    buffer_complete(7), seek_start(8), seek_complete(9), ad_start(10), ad_end(11), play_back_init(12), preparing(13),
    ad_break_start(14), ad_break_end(15);

    final int val;

    PlayerState(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static PlayerState fromVal(int val) {
        switch (val) {
            case 0:
                return unknown;
            case 1:
                return play;
            case 2:
                return pause;
            case 3:
                return interrupt;
            case 4:
                return resume;
            case 5:
                return complete;
            case 6:
                return buffer_start;
            case 7:
                return buffer_complete;
            case 8:
                return seek_start;
            case 9:
                return seek_complete;
            case 10:
                return ad_start;
            case 11:
                return ad_end;
            case 12:
                return play_back_init;
            case 13:
                return preparing;
            case 14:
                return ad_break_start;
            case 15:
                return ad_break_end;
            default:
                return unknown;
        }
    }
}
