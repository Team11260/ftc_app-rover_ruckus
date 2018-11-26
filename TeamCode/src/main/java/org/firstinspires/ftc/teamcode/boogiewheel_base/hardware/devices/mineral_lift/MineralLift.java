package org.firstinspires.ftc.teamcode.boogiewheel_base.hardware.devices.mineral_lift;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.framework.userHardware.outputs.SlewDcMotor;

public class MineralLift {

    SlewDcMotor liftMotor;

    Servo gateServo;

    public MineralLift(HardwareMap hardwareMap){
        liftMotor = new SlewDcMotor(hardwareMap.dcMotor.get("mineral_lift"));
        liftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setTargetPosition(0);
        liftMotor.setPower(0);

        gateServo = hardwareMap.servo.get("mineral_gate");
        gateServo.setDirection(Servo.Direction.FORWARD);
        gateServo.setPosition(0);

    }

    public void setPosition(int position){
        liftMotor.setPower(1);
        liftMotor.setTargetPosition(position);
    }

    public void setGateServoPosition(double position) {
        gateServo.setPosition(position);
    }

    public void stop(){
        liftMotor.stop();
    }
}