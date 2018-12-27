package org.firstinspires.ftc.teamcode.boogiewheel_base.opmodes.auton;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.boogiewheel_base.hardware.Constants;
import org.firstinspires.ftc.teamcode.boogiewheel_base.hardware.Robot;
import org.firstinspires.ftc.teamcode.boogiewheel_base.hardware.RobotState;
import org.firstinspires.ftc.teamcode.framework.opModes.AbstractAutonNew;
import org.firstinspires.ftc.teamcode.framework.userHardware.DoubleTelemetry;
import org.firstinspires.ftc.teamcode.framework.userHardware.inputs.sensors.vision.SamplePosition;
import org.firstinspires.ftc.teamcode.framework.userHardware.inputs.sensors.vision.TensorFlow;
import org.firstinspires.ftc.teamcode.framework.util.PathState;
import org.firstinspires.ftc.teamcode.framework.util.State;

@Autonomous(name = "BoogieWheel Auton Crater", group = "New")
//@Disabled

public class BoogieAutonCrater extends AbstractAutonNew {

    Robot robot;
    TensorFlow tensorFlow;

    @Override
    public void RegisterStates() {
        addState(new State("auton release wheels sequence", "start", robot.autonReleaseWheelsSequenceCallable()));
        addState(new State("auton mineral lift zero sequence", "start", robot.autonLowerMineralLiftSequenceCallable()));
        addState(new PathState("begin intaking", "turn to gold mineral", robot.beginIntakingCallable()));
        addState(new PathState("finish intaking", "back up from minerals", robot.finishIntakingCallable()));
        //addState(new PathState("drive to wall with distance", "turn to wall", robot.autonDriveToWallSequenceCallable()));
    }

    @Override
    public void Init() {
        robot = new Robot();
        tensorFlow = new TensorFlow(TensorFlow.CameraOrientation.VERTICAL, "Webcam 1", false);

    }

    @Override
    public void InitLoop(int loop) {
        if (loop % 5 == 0) {
            tensorFlow.pause();
            tensorFlow.start();
        }
        SamplePosition currentPosition = tensorFlow.getSamplePosition();
        if (currentPosition != SamplePosition.UNKNOWN)
            RobotState.currentSamplePosition = currentPosition;
        telemetry.addData(DoubleTelemetry.LogMode.INFO, currentPosition.toString());
        telemetry.update();
    }

    @Override
    public void Run() {
        robot.moveRobotLiftToBottom();
        robot.runDrivePaths(Constants.unlatchRobot);
        switch (RobotState.currentSamplePosition) {
            case RIGHT:
                robot.runDrivePaths(Constants.collectRightMineral);
                break;
            case LEFT:
                robot.runDrivePaths(Constants.collectLeftMineral);
                break;
            case CENTER:
                robot.runDrivePaths(Constants.collectCenterMineral);
                break;
            default:
                robot.runDrivePaths(Constants.collectCenterMineral);
                break;
        }
        robot.runDrivePaths(Constants.craterSideToCrater);
    }

    @Override
    public void Stop() {
        robot.stop();
        tensorFlow.stop();
    }
}