package com.mattech.on_call.models;

public class Update {
    private boolean enabled;
    private String time;
    private boolean repeatable;

    public Update(boolean enabled, String time, boolean repeatable) {
        this.enabled = enabled;
        this.time = time;
        this.repeatable = repeatable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
