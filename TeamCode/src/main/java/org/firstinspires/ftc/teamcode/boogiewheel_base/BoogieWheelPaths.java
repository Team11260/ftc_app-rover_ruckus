package org.firstinspires.ftc.teamcode.boogiewheel_base;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.boogiewheel_base.hardware.Robot;
import org.firstinspires.ftc.teamcode.framework.AbstractAuton;
import org.upacreekrobotics.dashboard.Dashboard;

@Autonomous(name="boggiewheel_paths", group="New")
//@Disabled

public class BoogieWheelPaths extends AbstractAuton {
    private Robot robot;


    @Override
    public void Init() {
        robot = new Robot();
    }

    @Override
    public void Run() {
        switch ((int)Dashboard.getInputValue("Create(0) of Run(1)?")) {
            case 0:{
                createPath();
                break;
            }
            case 1:{
                runPath();
                break;
            }
        }
    }

    public void createPath() {
        Dashboard.getInputValue("Press Enter to continue");

        printArrays(robot.recordPath(500,15));
    }

    public void runPath(){
        int[] left = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,6,12,19,25,31,38,43,50,59,68,79,90,101,112,125,137,151,165,181,197,215,232,251,270,291,312,334,358,383,407,433,459,485,511,537,562,588,612,637,661,687,712,739,766,794,824,853,882,911,939,967,995,1023,1054,1086,1119,1152,1186,1220,1252,1284,1315,1346,1376,1406,1438,1469,1500,1531,1559,1587,1612,1636,1660,1684,1709,1737,1764,1792,1818,1842,1866,1890,1912,1936,1962,1988,2016,2043,2070,2096,2122,2147,2176,2200,2224,2248,2272,2296,2320,2343,2365,2386,2406,2426,2449,2472,2495,2521,2548,2576,2604,2632,2658,2685,2711,2736,2762,2787,2812,2838,2867,2893,2918,2943,2974,2998,3021,3048,3073,3097,3123,3157,3186,3215,3245,3274,3302,3330,3358,3386,3414,3441,3467,3493,3519,3545,3572,3599,3626,3652,3677,3702,3725,3748,3771,3792,3814,3834,3855,3876,3898,3920,3942,3966,3991,4015,4038,4060,4081,4103,4125,4146,4168,4192,4215,4237,4258,4278,4298,4316,4333,4350,4368,4387,4409,4431,4454,4474,4492,4510,4528,4546,4567,4587,4609,4631,4652,4674,4696,4719,4741,4764,4786,4809,4831,4854,4874,4895,4916,4936,4958,4979,5000,5023,5046,5068,5089,5113,5136,5160,5184,5209,5234,5258,5282,5305,5328,5350,5374,5396,5424,5448,5472,5496,5519,5542,5564,5586,5607,5628,5651,5674,5695,5720,5744,5770,5795,5819,5843,5867,5892,5918,5944,5971,5998,6023,6048,6072,6098,6123,6149,6176,6203,6229,6258,6286,6312,6338,6364,6392,6416,6440,6464,6487,6510,6530,6552,6574,6599,6624,6649,6674,6698,6719,6740,6759,6777,6794,6812,6828,6845,6860,6875,6888,6901,6911,6920,6928,6935,6940,6945,6948,6952,6953,6953,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952,6952};
        int[] right = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,4,9,15,21,27,35,45,54,65,77,88,99,110,121,130,140,151,162,173,185,196,209,223,238,254,271,288,306,326,345,366,388,410,431,453,476,499,523,546,569,592,614,638,662,685,708,731,756,780,806,833,863,893,924,956,989,1021,1055,1089,1125,1160,1194,1228,1261,1294,1326,1357,1387,1418,1447,1477,1506,1536,1565,1597,1626,1658,1689,1719,1754,1788,1822,1861,1899,1935,1971,2008,2046,2084,2122,2158,2198,2233,2268,2303,2337,2373,2408,2445,2480,2517,2553,2591,2632,2673,2712,2752,2791,2831,2871,2912,2952,2992,3031,3070,3107,3144,3180,3217,3259,3295,3330,3371,3412,3449,3485,3523,3556,3589,3631,3663,3697,3731,3764,3797,3830,3863,3897,3933,3969,4004,4039,4073,4107,4141,4175,4209,4243,4276,4310,4345,4382,4419,4457,4495,4534,4574,4614,4655,4695,4734,4773,4811,4851,4889,4927,4962,4997,5035,5073,5111,5150,5189,5229,5269,5306,5346,5385,5424,5460,5498,5537,5576,5617,5661,5701,5738,5775,5810,5845,5880,5915,5949,5982,6014,6044,6074,6104,6132,6161,6190,6219,6249,6280,6314,6345,6377,6406,6437,6468,6499,6533,6565,6599,6632,6663,6693,6723,6751,6781,6810,6839,6867,6893,6919,6944,6969,6994,7018,7046,7070,7094,7118,7141,7164,7188,7211,7233,7255,7277,7299,7321,7344,7370,7395,7420,7445,7469,7494,7519,7544,7569,7593,7619,7643,7667,7692,7718,7746,7773,7801,7828,7856,7884,7915,7944,7976,8008,8041,8073,8105,8137,8168,8197,8225,8252,8276,8301,8323,8345,8367,8387,8407,8427,8446,8465,8484,8503,8519,8535,8549,8562,8574,8585,8595,8603,8610,8616,8621,8626,8629,8631,8632,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633,8633};

        robot.runPath(left, right, 10);
    }

    public void printArrays(int[][] values){
        String message = "";
        message = message + "int[] left = {";
        for (int i = 0; i < values[0].length-1; i++) {
            message = message + values[0][i] + ",";
        }
        message = message + values[0][values[0].length-1] + "};";
        telemetry.addData(message);

        message = "";
        message = message + "int[] right = {";
        for (int i = 0; i < values[1].length-1; i++) {
            message = message + values[1][i] + ",";
        }
        message = message + values[1][values[1].length-1] + "};";
        telemetry.addData(message);
    }
}