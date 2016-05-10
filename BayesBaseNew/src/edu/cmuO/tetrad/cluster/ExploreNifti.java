package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.cluster.niftijlib.Nifti1Dataset;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class ExploreNifti {
    private PrintWriter csvOut;

    public ExploreNifti() {
    }

    public void scenario1() {
        String prefix = "test_data";
        String file = "sub001_run001_bold.nii.gz";

        String[] strings = file.split("\\.");
        File csvFile = new File(prefix + "/" + strings[0] + ".csv");

        double data[][][];

        try {
            Nifti1Dataset nds = new Nifti1Dataset(prefix + "/" + file);
            nds.readHeader();

            csvOut = new PrintWriter(csvFile);

            System.out.println("Dimensions x = " + nds.XDIM + " y = " + nds.YDIM + " z = " + nds.ZDIM +
                    " t = " + nds.TDIM);

            data = nds.readDoubleVol((short) 0);

            for (short x = 0; x < nds.XDIM; x++) {
                for (short y = 0; y < nds.YDIM; y++) {
                    for (short z = 0; z < nds.ZDIM; z++) {
                        if (data[z][y][x] < 200) {
                            continue;
                        }

                        print(x + "," + y + "," + z + ",");
                        print(timeCourseString(file, nds, x, y, z) + "\n");

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            //
                        }
                    }
                }
            }
        } catch (IOException e) {
            //
        }
    }

    public void scenario2() {
        String prefix = "test_data";
        String file = "sub001_run001_bold.nii.gz";

        String[] strings = file.split("\\.");
        File csvFile = new File(prefix + "/" + strings[0] + ".csv");

        try {
            Nifti1Dataset nds = new Nifti1Dataset(prefix + "/" + file);
            nds.readHeader();

            csvOut = new PrintWriter(csvFile);

            System.out.println("Dimensions x = " + nds.XDIM + " y = " + nds.YDIM + " z = " + nds.ZDIM +
                    " t = " + nds.TDIM);

            double[][][][] data = new double[nds.TDIM][][][];

            for (short t = 0; t < nds.TDIM; t++) {
                double[][][] vol = nds.readDoubleVol(t);
                data[t] = vol;
                System.out.println("Read vol for time = " + t);
            }

            for (short x = 0; x < nds.XDIM; x++) {
                for (short y = 0; y < nds.YDIM; y++) {
                    for (short z = 0; z < nds.ZDIM; z++) {
                        if (data[0][z][y][x] < 200) {
                            continue;
                        }

                        print(x + "," + y + "," + z + ",");

                        for (short t = 0; t < nds.TDIM; t++) {
                            print("" + data[t][z][y][x]);

                            if (t < nds.TDIM - 1) {
                                print(",");
                            }
                        }

                        print("\n");

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {

                        }
                    }
                }
            }

            Nifti1Dataset nds2 = new Nifti1Dataset();
            nds2.setHeaderFilename(strings[0] + ".copy.hdr");
            nds2.setDataFilename(strings[0] + ".copy.img");
            nds2.setDatatype((short) 64);
            nds2.setDims((short) 4, nds.XDIM, nds.YDIM, nds.ZDIM, nds.TDIM, (short) 0, (short) 0, (short) 0);
            nds2.descrip = new StringBuffer("Created by Joe");
            nds2.writeHeader();

            for (short t = 0; t < nds.TDIM; t++) {
                nds2.writeVol(data[t], t);
            }
        } catch (IOException e) {
            //
        }
    }

    public void scenario3() {
        try {
            FmriData data = FmriData.loadNifti1Dataset("test_data/sub001_run001_bold.nii.gz", 200);

            System.out.println(data.getNumPoints());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void scenario4() {
        try {
            double[][][][] data = FmriData.loadNifti1DataCube("test_data/sub001_run001_bold.nii.gz", 200);

            DataParser parser = new DataParser();
            parser.setMaxIntegralDiscrete(0);
            parser.setVarNamesSupplied(false);
            RectangularDataSet clusterInfo = parser.parseTabular(new File("test_data/fsl_rev_t3_z2.3_p0.05_run001_losses.coord"));

            System.out.println(clusterInfo);


            DoubleMatrix2D flipped = new DenseDoubleMatrix2D(clusterInfo.getNumRows(), 1 + data.length);

            for (int i = 0; i < clusterInfo.getNumRows(); i++) {
                int cluster = (int) clusterInfo.getDouble(i, 0);
                int x = (int) clusterInfo.getDouble(i, 1);
                int y = (int) clusterInfo.getDouble(i, 2);
                int z = (int) clusterInfo.getDouble(i, 3);

                flipped.set(i, 0, cluster);

                for (int j = 0; j < data.length; j++) {
                    flipped.set(i, j + 1, data[j][z][y][x]);
                }
            }

            System.out.println(flipped);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void scenario5() {
        try {
            double[][][][] data = FmriData.loadNifti1DataCube("test_data/sub001_run001_bold.nii.gz", 200);

            DataParser parser = new DataParser();
            parser.setMaxIntegralDiscrete(0);
            parser.setVarNamesSupplied(false);
            RectangularDataSet clusterInfo = parser.parseTabular(new File("test_data/fsl_rev_t3_z2.3_p0.05_run001_losses.coord"));

            System.out.println(clusterInfo);

            int targetCluster = 6;

            DoubleMatrix2D maxes = new DenseDoubleMatrix2D(data.length, 4);

            for (short t = 0; t < data.length; t++) {
                int maxx = -1;
                int maxy = -1;
                int maxz = -1;
                double max = 0.0;

                for (int i = 0; i < clusterInfo.getNumRows(); i++) {
                    int cluster = (int) clusterInfo.getDouble(i, 0);

                    if (cluster != targetCluster) {
                        continue;
                    }

                    int x = (int) clusterInfo.getDouble(i, 1);
                    int y = (int) clusterInfo.getDouble(i, 2);
                    int z = (int) clusterInfo.getDouble(i, 3);

                    if (data[t][z][y][x] > max) {
                        maxx = x;
                        maxy = y;
                        maxz = z;
                        max = data[t][z][y][x];
                    }
                }

                maxes.set(t, 0, maxx);
                maxes.set(t, 1, maxy);
                maxes.set(t, 2, maxz);
                maxes.set(t, 3, max);
            }

            System.out.println(maxes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void scenario6() {
        try {
            double[][][][] data = FmriData.loadNifti1DataCube("test_data/sub001_run001_bold.nii.gz", 200);

            DataParser parser = new DataParser();
            parser.setMaxIntegralDiscrete(0);
            parser.setVarNamesSupplied(false);
            RectangularDataSet clusterInfo = parser.parseTabular(new File("test_data/fsl_rev_t3_z2.3_p0.05_run001_losses.coord"));

            System.out.println(clusterInfo);

            int numClusters = 0;

            for (int i = 0; i < clusterInfo.getNumRows(); i++) {
                int cluster = (int) clusterInfo.getDouble(i, 0);
                if (cluster > numClusters) {
                    numClusters = cluster;
                }
            }


            DoubleMatrix2D maxes = new DenseDoubleMatrix2D(data.length, numClusters);

            for (short t = 0; t < data.length; t++) {
//                int[] maxx = new int[numClusters];
//                int[] maxy = new int[numClusters];
//                int[] maxz = new int[numClusters];
                double[] max = new double[numClusters];

                for (int i = 0; i < clusterInfo.getNumRows(); i++) {
                    int cluster = (int) clusterInfo.getDouble(i, 0) - 1;

                    int x = (int) clusterInfo.getDouble(i, 1);
                    int y = (int) clusterInfo.getDouble(i, 2);
                    int z = (int) clusterInfo.getDouble(i, 3);

                    if (data[t][z][y][x] > max[cluster]) {
//                        maxx[cluster] = x;
//                        maxy[cluster] = y;
//                        maxz[cluster] = z;
                        max[cluster] = data[t][z][y][x];
                    }
                }

                for (int c = 0; c < numClusters; c++) {
                    maxes.set(t, c, max[c]);
                }
            }

            System.out.println(maxes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void print(String s) {
        System.out.print(s);
        csvOut.print(s);
    }

    private String timeCourseString(String file, Nifti1Dataset nds, short x, short y, short z) {
        StringBuffer buf = new StringBuffer();
        double[] tmcrs;

        try {
//            nds.readHeader();
            tmcrs = nds.readDoubleTmcrs(x, y, z);

            for (int m = 0; m < tmcrs.length; m++) {
                buf.append(tmcrs[m]);

                if (m < tmcrs.length - 1) {
                    buf.append(",");
                }
            }

//            nds.printDoubleTmcrs(tmcrs);
        }
        catch (IOException ex) {
            System.out.println("\nCould not read timecourse from " + file + ": " + ex.getMessage());
        }

        return buf.toString();
    }

    public void scenario7() {

//            String prefix = "/home/jdramsey/proj/other/fMRI/data/imgfiles20070911/";
        String prefix = "test_data";
        String file = "sub001_run001_bold.nii.gz";
//            String file = "bet-az61_ep2d_bold_ADI_MCFLRT8244_R1.img";

        String[] strings = file.split("\\.");
        File csvFile = new File(prefix + "/" + strings[0] + ".csv");


        double data[][][];
        double tmcrs[];
        int i, j, k, l;
        int icode;
        byte b[];

        try {
            Nifti1Dataset nds = new Nifti1Dataset(prefix + "/" + file);
            nds.readHeader();

            csvOut = new PrintWriter(csvFile);

            System.out.println("Dimensions x = " + nds.XDIM + " y = " + nds.YDIM + " z = " + nds.ZDIM +
                    " t = " + nds.TDIM);

            data = nds.readDoubleVol((short) 0);

            for (short x = 0; x < nds.XDIM; x++) {
                for (short y = 0; y < nds.YDIM; y++) {
                    for (short z = 0; z < nds.ZDIM; z++) {
                        if (data[z][y][x] < 200) {
                            continue;
                        }

                        print(x + "," + y + "," + z + ",");
                        print(timeCourseString(file, nds, x, y, z) + "\n");

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

//    private void print(String s) {
//        System.out.print(s);
//        csvOut.print(s);
//    }

//    private String timeCourseString(String file, Nifti1Dataset nds, short x, short y, short z) {
//        StringBuffer buf = new StringBuffer();
//        double[] tmcrs;
//
//        try {
////            nds.readHeader();
//            tmcrs = nds.readDoubleTmcrs(x, y, z);
//
//            for (int m = 0; m < tmcrs.length; m++) {
//                buf.append(tmcrs[m]);
//
//                if (m < tmcrs.length - 1) {
//                    buf.append(",");
//                }
//            }
//
////            nds.printDoubleTmcrs(tmcrs);
//        }
//        catch (IOException ex) {
//
//            System.out.println("\nCould not read timecourse from " + file + ": " + ex.getMessage());
//        }
//
//        return buf.toString();
//    }

    public static void main(String[] args) {
        new ExploreNifti().scenario1();
    }
}