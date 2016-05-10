///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.IndTestCramerT;
import edu.cmu.tetrad.search.IndTestDSep;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.kernel.KernelGaussian;
import edu.cmu.tetrad.search.kernel.KernelUtils;
import edu.cmu.tetrad.search.kernel.Kernel;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.dist.Uniform;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.SemIm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.*;

import no.uib.cipr.matrix.*;

/**
 * Tests IndTestHsic class
 *
 * @author Robert Tillman
 */
public class TestIndTestHsic extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestIndTestHsic(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
        TetradLogger.getInstance().setLogging(true);
    }


    public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    public void testIncompleteCholesky() {
        int m = 500;
        double precision = 1e-8;
        Node A = new ContinuousVariable("A");
        Node B = new ContinuousVariable("B");
        Node C = new ContinuousVariable("C");
        Dag dag = new Dag(Arrays.asList(A, B, C));
        dag.addDirectedEdge(A, B);
        dag.addDirectedEdge(C, B);
        SemPm sem = new SemPm(dag);
        SemIm im = new SemIm(sem);
        DataSet data = im.simulateData(m, false);
        List<Kernel> kernels = new ArrayList<Kernel>();
        for (int i = 0; i < 3; i++) {
            Kernel k = new KernelGaussian(1);
            k.setDefaultBw(data, data.getVariable(i));
            kernels.add(k);
        }

        Matrix Kx = KernelUtils.constructGramMatrix(kernels, data, Arrays.asList(data.getVariable(0)));
        Matrix Gx = KernelUtils.incompleteCholeskyGramMatrix(kernels, data, Arrays.asList(data.getVariable(0)), precision);
        int nx = Gx.numColumns();
        Matrix Gxt = new DenseMatrix(nx, m);
        Gx.transpose(Gxt);
        Matrix GGx = new DenseMatrix(m, m);
        Gx.mult(Gxt, GGx);
        System.out.println("X true: " + trace(Kx, m));
        System.out.println("X appr: " + trace(GGx, m));
        Matrix Ky = KernelUtils.constructGramMatrix(kernels, data, Arrays.asList(data.getVariable(1)));
        Matrix Gy = KernelUtils.incompleteCholeskyGramMatrix(kernels, data, Arrays.asList(data.getVariable(1)), precision);
        int ny = Gy.numColumns();
        Matrix Gyt = new DenseMatrix(ny, m);
        Gy.transpose(Gyt);
        Matrix GGy = new DenseMatrix(m, m);
        Gy.mult(Gyt, GGy);
        System.out.println("Y true: " + trace(Ky, m));
        System.out.println("Y appr: " + trace(GGy, m));

        Matrix KxKy = new DenseMatrix(m, m);
        Kx.mult(Ky, KxKy);
        Matrix GxGy = new DenseMatrix(m, m);
        GGx.mult(GGy, GxGy);
        System.out.println("XY true: " + trace(KxKy, m));
        System.out.println("XY appr: " + trace(GxGy, m));
        Matrix H = KernelUtils.constructH(m);
        Matrix HKx = new DenseMatrix(m, m);
        H.mult(Kx, HKx);
        Matrix HKxH = new DenseMatrix(m, m);
        HKx.mult(H, HKxH);
        Matrix HKy = new DenseMatrix(m, m);
        H.mult(Ky, HKy);
        Matrix HKyH = new DenseMatrix(m, m);
        HKy.mult(H, HKyH);
        Matrix HGx = new DenseMatrix(m, nx);
        H.mult(Gx, HGx);
        Matrix HGxt = new DenseMatrix(nx, m);
        HGx.transpose(HGxt);
        Matrix HGxH = new DenseMatrix(m, m);
        HGx.mult(HGxt, HGxH);
        Matrix HGy = new DenseMatrix(m, ny);
        H.mult(Gy, HGy);
        Matrix HGyt = new DenseMatrix(ny, m);
        HGy.transpose(HGyt);
        Matrix HGyH = new DenseMatrix(m, m);
        HGy.mult(HGyt, HGyH);
        Matrix HKxHHKyH = new DenseMatrix(m, m);
        HKxH.mult(HKyH, HKxHHKyH);
        Matrix HGxHHGyH = new DenseMatrix(m, m);
        HGxH.mult(HGyH, HGxHHGyH);
        System.out.println("HXHHYH True: " + trace(HKxHHKyH, m));
        System.out.println("HXHHYH Appr: " + trace(HGxHHGyH, m));
        Matrix Kz = KernelUtils.constructGramMatrix(kernels, data, Arrays.asList(data.getVariable(2)));
        Matrix Gz = KernelUtils.incompleteCholeskyGramMatrix(kernels, data, Arrays.asList(data.getVariable(2)), precision);
        int nz = Gz.numColumns();
        Matrix Gzt = new DenseMatrix(nz, m);
        Gz.transpose(Gzt);
        Matrix GGz = new DenseMatrix(m, m);
        Gz.mult(Gzt, GGz);
        System.out.println("Z true: " + trace(Kz, m));
        System.out.println("Z appr: " + trace(GGz, m));
        // invert Kz
        double ep = 0.0001;
        Matrix Kzep = Kz.copy();
        for (int i = 0; i < m; i++) {
            Kzep.set(i, i, Kzep.get(i, i) + ep);
        }
        Matrix Kzinv = new DenseMatrix(m, m);
        Kzep.solve(Matrices.identity(m), Kzinv);
        System.out.println("Z-1 true: " + trace(Kzinv, m));
        Matrix GGzep = GGz.copy();
        for (int i = 0; i < m; i++) {
            GGzep.set(i, i, GGzep.get(i, i) + ep);
        }
        Matrix GGzinv = new DenseMatrix(m, m);
        GGzep.solve(Matrices.identity(m), GGzinv);
        System.out.println("Z-1 appr: " + trace(GGzinv, m));
    }

    /**
     * Unconditional independence test for sine transformation
     */
    public void testUnconditionalTest1() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        int m = 160;
        double[] dataX = {9.6097, 9.5734, 9.6119, 9.6500, 9.6467, 9.5675, 9.6127, 9.5662, 9.5577, 9.5815, 9.5905, 9.6488, 9.7119, 9.7658, 9.7814, 9.8079, 9.7450, 9.7470, 9.7644, 9.7007, 9.6996, 9.6530, 9.6794, 9.6753, 9.6472, 9.6502, 9.6724, 9.6427, 9.5945, 9.5925, 9.6271, 9.6066, 9.7105, 9.7205, 9.7491, 9.7005, 9.7174, 9.7699, 9.7305, 9.7580, 9.6953, 9.6892, 9.6939, 9.6392, 9.6838, 9.6438, 9.6749, 9.6771, 9.7014, 9.6352, 9.5624, 9.5488, 9.6184, 9.6343, 9.6572, 9.6600, 9.6483, 9.6785, 9.7317, 9.7123, 9.7244, 9.7259, 9.6708, 9.5890, 9.5632, 9.6279, 9.5785, 9.5950, 9.5867, 9.5896, 9.5637, 9.5849, 9.6503, 9.7249, 9.6835, 9.6981, 9.7252, 9.6965, 9.7029, 9.6870, 9.6454, 9.6177, 9.6176, 9.5261, 9.4988, 9.5184, 9.5190, 9.5164, 9.5755, 9.6533, 9.6525, 9.5359, 9.5871, 9.6765, 9.7384, 9.7407, 9.7726, 9.7488, 9.7440, 9.7602, 9.7204, 9.7315, 9.7178, 9.6471, 9.6016, 9.5629, 9.6143, 9.5382, 9.5847, 9.5789, 9.5852, 9.6261, 9.7179, 9.7832, 9.7592, 9.7289, 9.7363, 9.7234, 9.7751, 9.7010, 9.7126, 9.7155, 9.6535, 9.6309, 9.5462, 9.5149, 9.5107, 9.6194, 9.5682, 9.5390, 9.4894, 9.5402, 9.5876, 9.6345, 9.6908, 9.6962, 9.6318, 9.7280, 9.7331, 9.6725, 9.6949, 9.7017, 9.6830, 9.6156, 9.6932, 9.6450, 9.7147, 9.6494, 9.5806, 9.5469, 9.5560, 9.5647, 9.5961, 9.6443, 9.6466, 9.6939, 9.7393, 9.7166, 9.7222, 9.6618};
        double[] dataY = {-0.1154, -0.1328, -0.1498, -0.1663, -0.1821, -0.1972, -0.2115, -0.2250, -0.2377, -0.2497, -0.2601, -0.1686, 0.1334, 0.4161, 0.5641, 0.6074, 0.5952, 0.5617, 0.5267, 0.4981, 0.4760, 0.3586, 0.0369, -0.2604, -0.4208, -0.4765, -0.4770, -0.4545, -0.4282, -0.4074, -0.3938, -0.2855, 0.0284, 0.3201, 0.4755, 0.5252, 0.5191, 0.4912, 0.4615, 0.4378, 0.4203, 0.3073, -0.0103, -0.3038, -0.4605, -0.5128, -0.5101, -0.4846, -0.4556, -0.4323, -0.4164, -0.3059, 0.0099, 0.3033, 0.4604, 0.5116, 0.5068, 0.4801, 0.4515, 0.4289, 0.4123, 0.3001, -0.0168, -0.3096, -0.4658, -0.5175, -0.5143, -0.4884, -0.4590, -0.4353, -0.4191, -0.3084, 0.0078, 0.3015, 0.4588, 0.5104, 0.5058, 0.4794, 0.4510, 0.4285, 0.4121, 0.3000, -0.0167, -0.3094, -0.4656, -0.5174, -0.5142, -0.4883, -0.4589, -0.4353, -0.4191, -0.3084, 0.0078, 0.3015, 0.4588, 0.5104, 0.5058, 0.4794, 0.4510, 0.4285, 0.4122, 0.3002, -0.0165, -0.3093, -0.4655, -0.5174, -0.5143, -0.4886, -0.4594, -0.4359, -0.4199, -0.3094, 0.0066, 0.3002, 0.4574, 0.5089, 0.5042, 0.4776, 0.4491, 0.4264, 0.4098, 0.2974, -0.0197, -0.3128, -0.4695, -0.5219, -0.5194, -0.4943, -0.4657, -0.4430, -0.4278, -0.3182, -0.0033, 0.2891, 0.4449, 0.4948, 0.4885, 0.4600, 0.4294, 0.4046, 0.3855, 0.2705, -0.0494, -0.3455, -0.5054, -0.5611, -0.5622, -0.5411, -0.5169, -0.4989, -0.4888, -0.3846, -0.0757, 0.2100, 0.3585, 0.4003, 0.3852, 0.3475, 0.3070, 0.2716};
        DataSet dataSet = new ColtDataSet(m, Arrays.asList(X, Y));
        for (int i = 0; i < m; i++) {
            dataSet.setDouble(i, 0, dataX[i]);
            dataSet.setDouble(i, 1, dataY[i]);
        }
        IndTestHsic test = new IndTestHsic(dataSet, .05);
        test.isIndependent(X, Y, new ArrayList<Node>());
        System.out.println("HSIC P-value: " + test.getPValue());
        IndTestFisherZ test2 = new IndTestFisherZ(dataSet, .05);
        test2.isIndependent(X, Y, new ArrayList<Node>());
        System.out.println("Fisher Z P-value: " + test2.getPValue());
    }

    public void testConditionalTest1() {
        double start = System.currentTimeMillis();
        double precision = 1e-18;
        Node A = new ContinuousVariable("A");
        Node B = new ContinuousVariable("B");
        Node C = new ContinuousVariable("C");
        Dag dag = new Dag(Arrays.asList(A, B, C));
        dag.addDirectedEdge(A, B);
        dag.addDirectedEdge(C, B);
        SemPm sem = new SemPm(dag);
        SemIm im = new SemIm(sem);
        DataSet data = im.simulateData(500, false);
        IndTestHsic test = new IndTestHsic(data, .05);
        test.setPerms(100);
        test.setIncompleteCholesky(precision);
        test.isIndependent(A, C, Arrays.asList(B));
        System.out.println("HSIC P-value: " + test.getPValue());
        IndTestFisherZ test2 = new IndTestFisherZ(data, .05);
        test2.isIndependent(A, C, Arrays.asList(B));
        System.out.println("Fisher Z P-value: " + test2.getPValue());
    }

    private static double trace(Matrix A, int m) {
        double trace = 0.0;
        for (int i = 0; i < m; i++) {
            trace += A.get(i, i);
        }
        return trace;
    }

}
