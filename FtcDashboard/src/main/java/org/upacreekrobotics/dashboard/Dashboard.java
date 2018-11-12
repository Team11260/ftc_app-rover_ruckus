package org.upacreekrobotics.dashboard;

import android.content.Context;
import android.util.Log;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.BatteryChecker;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeManagerImpl;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dashboard implements OpModeManagerImpl.Notifications, BatteryChecker.BatteryWatcher{
    public static final String TAG = "RobotDashboard";
    private Thread dataThread;
    private Thread dashboardThread;
    private Data data;
    private EventLoop eventLoop;
    private RobotStatus status;
    private dashboardtelemetry DashboardTelemtry;
    private String lastInputValue;
    private boolean connected = false;
    private VoltageSensor voltageSensor2;
    private VoltageSensor voltageSensor3;
    private VoltageSensor voltageSensor4;
    private boolean restartRequested = false;
    private boolean isRunning = false;
    private BatteryChecker.BatteryWatcher batteryWatcher;
    private BatteryChecker batteryChecker;
    private Date date;
    private String oldTelemetry = "";
    private String infoText = "";

    private OpModeManagerImpl opModeManager;
    private RobotStatus.OpModeStatus activeOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
    private RobotStatus.OpModeStatus requestedOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
    private List<String> opModeList;

    private static Dashboard dashboard;

    ////////////////Gradle Configuration////////////////
    private static final Set<String> IGNORED_PACKAGES = new HashSet<>(Arrays.asList(
            "java",
            "android",
            "com.sun",
            "com.vuforia",
            "com.google"
    ));

    ////////////////Start the dashboard, calls Dashboard constructor, called by "onCreate()" in "FtcRobotControllerActivity"////////////////
    public static void start() {

        if (dashboard == null) {
            dashboard = new Dashboard();
        }
    }

    ////////////////Passes the eventLoop, and android context to allow for opModeManager access////////////////
    ////////////////and phone battery level polling////////////////
    ////////////////called by "requestRobotSetup()" in "FtcRobotControllerActivity"////////////////
    public static void attachEventLoop(EventLoop eventLoop, Context context) {
        dashboard.internalAttachEventLoop(eventLoop,context);
    }

    ////////////////Stops the dashboard and socket connection////////////////
    ////////////////called by "onDestroy()" in "FtcRobotControllerActivity"////////////////
    public static void stop() {
        if (dashboard != null) {
            dashboard.isRunning = false;
            dashboard = null;
        }
    }

    ////////////////Returns current dashboard instance for use in user OpModes////////////////
    public static Dashboard getInstance() {
        return dashboard;
    }

    ////////////////Constructor called by "start()"////////////////
    private Dashboard() {

        ClasspathScanner scanner = new ClasspathScanner(new ClassFilter() {
            @Override
            public boolean shouldProcessClass(String className) {
                for (String packageName : IGNORED_PACKAGES) {
                    if (className.startsWith(packageName)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void processClass(Class klass) {
                if (klass.isAnnotationPresent(Config.class) && !klass.isAnnotationPresent(Disabled.class)) {
                    Log.i(TAG, String.format("Found config class %s", klass.getCanonicalName()));
                }
            }
        });
        scanner.scanClasspath();

        date = new Date();

        opModeList = new ArrayList<>();

        dataThread = new Thread(new DataHandler());
        dataThread.start();
        dashboardThread = new Thread(new DashboardHandler());
        dashboardThread.start();

        isRunning = true;
    }

    ////////////////Returns the current "RobotStatus", this may be useful in the future////////////////
    private void internalAttachEventLoop(EventLoop eventLoop, Context context) {
        opModeManager = eventLoop.getOpModeManager();

        this.eventLoop = eventLoop;
        if (opModeManager != null) {
            opModeManager.registerListener(this);
        }

        synchronized (opModeList) {
            opModeList.clear();
        }

        (new Thread() {
            @Override
            public void run() {
                RegisteredOpModes.getInstance().waitOpModesRegistered();
                synchronized (opModeList) {
                    for (OpModeMeta opModeMeta : RegisteredOpModes.getInstance().getOpModes()) {
                        opModeList.add(opModeMeta.name);
                    }
                }
            }
        }).start();

        batteryWatcher = this;
        batteryChecker = new BatteryChecker(batteryWatcher, 60000);
    }

    ////////////////Returns the current "RobotStatus", this may be useful in the future////////////////
    private RobotStatus getRobotStatus() {
        if (opModeManager == null) {
            return new RobotStatus();
        } else {
            return new RobotStatus(opModeManager.getActiveOpModeName(), activeOpModeStatus);
        }
    }

    ////////////////This is the main loop, called by the "dashboardThread"////////////////
    synchronized void receiveMessage() {
        if(data!=null) {
            ////////////////Receiving Message////////////////
            Message message = data.read();
            if(message!=null) {
                switch (message.getType()) {
                    case GET_OP_MODES: {
                        for (String mode : opModeList) {
                            data.write(new Message(MessageType.OP_MODES, mode));
                        }
                        try {
                            if (!dashboard.opModeManager.getActiveOpModeName().equals("$Stop$Robot$")) {
                                if (dashboard.activeOpModeStatus.equals(RobotStatus.OpModeStatus.INIT)) {
                                    data.write(new Message(MessageType.SELECT_OP_MODE, dashboard.opModeManager.getActiveOpModeName()));
                                    data.write(new Message(MessageType.ROBOT_STATUS, "INIT"));
                                } else if (dashboard.activeOpModeStatus.equals(RobotStatus.OpModeStatus.RUNNING)) {
                                    data.write(new Message(MessageType.SELECT_OP_MODE, dashboard.opModeManager.getActiveOpModeName()));
                                    data.write(new Message(MessageType.ROBOT_STATUS, "RUNNING"));
                                }
                            }
                        } catch (NullPointerException e){

                        }
                        String[] parts = oldTelemetry.split("`");
                        for(String part:parts){
                            if(data!=null&&!part.equals(""))data.write(new Message(MessageType.TELEMETRY,date.getDateTime()+"`"+part));
                        }
                        oldTelemetry = "";
                        if(batteryChecker!=null)batteryChecker.pollBatteryLevel(batteryWatcher);
                        break;
                    }

                    case SELECT_OP_MODE: {
                        status = new RobotStatus(message.getText(), activeOpModeStatus);
                        break;
                    }

                    case RESTART_ROBOT: {
                        activeOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
                        opModeManager.stopActiveOpMode();
                        restartRequested = true;
                        break;
                    }

                    case INIT_OP_MODE: {
                        activeOpModeStatus = RobotStatus.OpModeStatus.INIT;
                        requestedOpModeStatus = RobotStatus.OpModeStatus.INIT;
                        opModeManager.initActiveOpMode(status.getActiveOpMode());
                        break;
                    }

                    case RUN_OP_MODE: {
                        activeOpModeStatus = RobotStatus.OpModeStatus.RUNNING;
                        requestedOpModeStatus = RobotStatus.OpModeStatus.RUNNING;
                        opModeManager.startActiveOpMode();
                        break;
                    }

                    case STOP_OP_MODE: {
                        activeOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
                        requestedOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
                        opModeManager.stopActiveOpMode();
                        break;
                    }

                    case RETURN_VALUE: {
                        lastInputValue = message.getText();
                        break;
                    }

                    default: {
                        break;
                    }
                }
            }

            ////////////////Keeping track of the state of the active opMode////////////////
            switch (activeOpModeStatus){
                case INIT:
                    if(requestedOpModeStatus.equals(RobotStatus.OpModeStatus.RUNNING)) {
                        if (data != null) if (data.isConnected()) {
                            activeOpModeStatus = RobotStatus.OpModeStatus.RUNNING;
                            data.write(new Message(MessageType.ROBOT_STATUS, "RUNNING"));
                        }
                    }
                    if(requestedOpModeStatus.equals(RobotStatus.OpModeStatus.STOPPED)) {
                        if (data != null) if (data.isConnected()) {
                            activeOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
                            data.write(new Message(MessageType.ROBOT_STATUS, "STOPPED"));
                        }
                    }
                    break;
                case RUNNING:
                    if(requestedOpModeStatus.equals(RobotStatus.OpModeStatus.STOPPED)) {
                        if (data != null) if (data.isConnected()) {
                            activeOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
                            data.write(new Message(MessageType.ROBOT_STATUS, "STOPPED"));
                        }
                    }
                    break;
                case STOPPED:
                    if(requestedOpModeStatus.equals(RobotStatus.OpModeStatus.INIT)) {
                        if (data != null) if(data.isConnected()){
                            activeOpModeStatus = RobotStatus.OpModeStatus.INIT;
                            data.write(new Message(MessageType.SELECT_OP_MODE, dashboard.opModeManager.getActiveOpModeName()));
                            data.write(new Message(MessageType.ROBOT_STATUS, "INIT"));
                        }
                    }
                    break;
            }
        }
    }

    ////////////////Code to poll the robot battery voltage////////////////
    public String battery(){
        double sensors = 0;
        double voltage = 0;
        try {
            if (voltageSensor2.getVoltage() != 0) {
                sensors++;
                voltage += voltageSensor2.getVoltage();
            }
        } catch (NullPointerException e){
            try {
                HardwareMap hwMap = opModeManager.getHardwareMap();
                voltageSensor2 = hwMap.voltageSensor.get("Expansion Hub 2");
            } catch (RuntimeException ee){
            }
        } catch (RuntimeException e){
        } catch (AssertionError e){
        }
        try {
            if (voltageSensor3.getVoltage() != 0) {
                sensors++;
                voltage += voltageSensor3.getVoltage();
            }
        } catch (NullPointerException e){
            try {
                HardwareMap hwMap = opModeManager.getHardwareMap();
                voltageSensor3 = hwMap.voltageSensor.get("Expansion Hub 3");
            } catch (RuntimeException ee){
            }
        } catch (RuntimeException e){
        } catch (AssertionError e){
        }
        try {
            if (voltageSensor4.getVoltage() != 0) {
                sensors++;
                voltage += voltageSensor4.getVoltage();
            }
        } catch (NullPointerException e){
            try {
                HardwareMap hwMap = opModeManager.getHardwareMap();
                voltageSensor4 = hwMap.voltageSensor.get("Expansion Hub 4");
            } catch (RuntimeException ee){
            }
        } catch (RuntimeException e){
        } catch (AssertionError e){
        }
        if(sensors!=0) return String.format("%.2f",voltage/sensors);
        return String.valueOf(0);
    }

    ////////////////Returns an instance of dashboardtelemetry to be used in the user code////////////////
    public dashboardtelemetry getTelemetry(){
        return DashboardTelemtry;
    }

    ////////////////Called by the user code to request an input value from the dashboard, calls "internalGetInputValue()"////////////////
    public static double getInputValue(String caption){
        return dashboard.internalGetInputValue(caption);
    }

    ////////////////The code to ask the dashboard to input a value and wait for a response////////////////
    ////////////////This can also be used to block the user opMode to do autonomous step by step////////////////
    private double internalGetInputValue(String caption){
        if(data!=null)data.write(new Message(MessageType.GET_VALUE,caption));
        lastInputValue = null;
        requestedOpModeStatus = RobotStatus.OpModeStatus.INIT;
        while(activeOpModeStatus==RobotStatus.OpModeStatus.STOPPED&&requestedOpModeStatus!=RobotStatus.OpModeStatus.STOPPED);
        while(data!=null&&lastInputValue==null&&isRunning&&connected&&activeOpModeStatus!=RobotStatus.OpModeStatus.STOPPED);
        try{
            if(lastInputValue!=null)return Double.valueOf(lastInputValue);
        } catch (NumberFormatException e){}
        return 0;
    }

    ////////////////Called by the "RobotRestartChecker" thread in "FtcRobotControllerActivity" to see if the////////////////
    ////////////////restart button on the dashboard has been pressed, calls "internalRobotRestartRequested()"////////////////
    public static boolean robotRestartRequested(){
        return dashboard.internalRobotRestartRequested();
    }

    ////////////////The actual code to check if the restart button has been pressed////////////////
    private boolean internalRobotRestartRequested(){
        if(restartRequested) {
            restartRequested = false;
            return true;
        }
        return false;
    }

    ////////////////Called by "FtcRobotControllerActivity" when a restart has completed, calls "internalRestartComplete()"////////////////
    public static void restartComplete(){
        dashboard.internalRestartComplete();
    }

    ////////////////Code to refresh the dashboard when a restart is completed////////////////
    private void internalRestartComplete(){
        if(data!=null){
            data.write(new Message(MessageType.RESTART_ROBOT,"HI"));
            voltageSensor2 = null;
            voltageSensor3 = null;
            voltageSensor4 = null;
        }
    }

    ////////////////Called by "opModeManagerImpl"////////////////
    @Override
    public void onOpModePreInit(OpMode opMode) {
        if(!dashboard.opModeManager.getActiveOpModeName().equals("$Stop$Robot$"))requestedOpModeStatus = RobotStatus.OpModeStatus.INIT;
    }

    ////////////////Called by "opModeManagerImpl"////////////////
    @Override
    public void onOpModePreStart(OpMode opMode) {
        if(!dashboard.opModeManager.getActiveOpModeName().equals("$Stop$Robot$"))requestedOpModeStatus = RobotStatus.OpModeStatus.RUNNING;
    }

    ////////////////Called by "opModeManagerImpl"////////////////
    @Override
    public void onOpModePostStop(OpMode opMode) {
        if(!dashboard.opModeManager.getActiveOpModeName().equals("$Stop$Robot$")) requestedOpModeStatus = RobotStatus.OpModeStatus.STOPPED;
    }

    ////////////////Thread to start up dashboardtelemetry and then call "receiveMessage()" in a loop////////////////
    private class DashboardHandler implements Runnable{

        public DashboardHandler(){}

        @Override
        public void run() {
            DashboardTelemtry = new dashboardtelemetry();
            while (true) {
                receiveMessage();
            }
        }
    }

    ////////////////Starts a connection and continuously monitor it////////////////
    private class DataHandler implements Runnable{

        public DataHandler(){}
        int loop = 0;

        @Override
        public void run() {
            while (data==null) {
               data = new Data();
            }
            while (true){
                if(data.isConnected()){
                    if(data!=null&&loop%2==0&&!restartRequested)data.write(new Message(MessageType.BATTERY,battery()));
                    if(batteryChecker!=null&&loop==0)batteryChecker.pollBatteryLevel(batteryWatcher);
                    if(!connected) {
                        connected = true;
                    }
                }
                else{
                    if(connected) {
                        connected = false;
                        data = new Data();
                    }
                }
                loop++;
                if(loop>20)loop=0;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    ////////////////Used by user OpMode to display telemetry on the dashboard////////////////
    public class dashboardtelemetry{

        public dashboardtelemetry(){}

        public void write(String text){
            if(data!=null&&connected) {
                data.write(new Message(MessageType.TELEMETRY, date.getDateTime() + "`" + text));
            }
            else oldTelemetry = oldTelemetry + "`" + text;
        }

        public void write(int text){
            write(String.valueOf(text));
        }

        public void write(double text){
            write(String.valueOf(text));
        }

        public void updateInfo(){
            if(data!=null&&connected) {
                data.write(new Message(MessageType.INFO, infoText));
            }
            infoText = "";
        }

        public void info(String text){
            infoText = infoText + text + "!#@$";
        }

        public void info(int text){
            info(String.valueOf(text));
        }

        public void info(double text){
            info(String.valueOf(text));
        }
    }

    ////////////////Updates the phone battery level indicator on the dashboard////////////////
    ////////////////Called automatically by the driver station battery level code////////////////
    @Override
    public void updateBatteryStatus(BatteryChecker.BatteryStatus status) {
        if(data!=null&&status!=null) {
            data.write(new Message(MessageType.PHONE_BATTERY, String.valueOf(status.percent)));
        }
    }
}