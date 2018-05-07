package pt.advHUD;

import com.google.atap.tangoservice.TangoPoseData;

public class HUD_User {
    private TangoPoseData pose;

    public HUD_User() {
        pose = new TangoPoseData();
    }

    public void update_pose(TangoPoseData pose) {
        this.pose = pose;
    }

    TangoPoseData getPose() {
        return pose;
    }
}