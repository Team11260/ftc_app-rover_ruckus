package org.firstinspires.ftc.teamcode.framework.util;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.framework.abstractopmodes.AbstractOpMode;
import org.firstinspires.ftc.teamcode.framework.userHardware.DoubleTelemetry;

public abstract class SubsystemController {

    public DoubleTelemetry telemetry;
    public HardwareMap hardwareMap;

    public abstract void init();

    public abstract void update();

    public abstract void stop();

    public void opModeSetup() {
        telemetry = AbstractOpMode.getTelemetry();
        hardwareMap = AbstractOpMode.getHardwareMap();
    }

    public void delay(int millis) {
        AbstractOpMode.delay(millis);
    }
}
